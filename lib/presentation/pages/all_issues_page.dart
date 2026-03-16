import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_staggered_grid_view/flutter_staggered_grid_view.dart';
import '../../domain/entities/issue.dart';
import '../blocs/issues/issues_bloc.dart';
import '../widgets/ad_banner_widget.dart';
import '../widgets/issue_card.dart';

class AllIssuesPage extends StatefulWidget {
  const AllIssuesPage({super.key});

  @override
  State<AllIssuesPage> createState() => _AllIssuesPageState();
}

class _AllIssuesPageState extends State<AllIssuesPage> {
  String _searchQuery = '';
  final TextEditingController _searchController = TextEditingController();
  bool _isGridView = true;

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  List<Issue> _filterIssues(List<Issue> issues) {
    if (_searchQuery.isEmpty) return issues;
    return issues
        .where((issue) =>
    issue.name.toLowerCase().contains(_searchQuery.toLowerCase()) ||
        issue.description
            .toLowerCase()
            .contains(_searchQuery.toLowerCase()))
        .toList();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('All Issues'),
        actions: [
          IconButton(
            icon: Icon(_isGridView ? Icons.view_list : Icons.grid_view),
            onPressed: () {
              setState(() {
                _isGridView = !_isGridView;
              });
            },
            tooltip: _isGridView ? 'List View' : 'Grid View',
          ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(64),
          child: Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
            child: SearchBar(
              controller: _searchController,
              hintText: 'Search issues...',
              leading: const Icon(Icons.search),
              trailing: _searchQuery.isNotEmpty
                  ? [
                IconButton(
                  icon: const Icon(Icons.clear),
                  onPressed: () {
                    setState(() {
                      _searchQuery = '';
                      _searchController.clear();
                    });
                  },
                ),
              ]
                  : null,
              onChanged: (value) {
                setState(() {
                  _searchQuery = value;
                });
              },
            ),
          ),
        ),
      ),
      body: Column(
        children: [
          Expanded(
            child: BlocBuilder<IssuesBloc, IssuesState>(
              builder: (context, state) {
                if (state is IssuesLoading) {
                  return const Center(child: CircularProgressIndicator());
                } else if (state is IssuesLoaded) {
                  final filteredIssues = _filterIssues(state.issues);

                  if (filteredIssues.isEmpty) {
                    return Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.search_off,
                            size: 64,
                            color: Theme.of(context).colorScheme.secondary,
                          ),
                          const SizedBox(height: 16),
                          Text(
                            'No issues found',
                            style: Theme.of(context).textTheme.titleLarge,
                          ),
                          const SizedBox(height: 8),
                          Text(
                            'Try a different search term',
                            style: Theme.of(context)
                                .textTheme
                                .bodyMedium
                                ?.copyWith(
                              color: Theme.of(context)
                                  .colorScheme
                                  .onSurfaceVariant,
                            ),
                          ),
                        ],
                      ),
                    );
                  }

                  return RefreshIndicator(
                    onRefresh: () async {
                      context.read<IssuesBloc>().add(LoadIssuesEvent());
                    },
                    child: _isGridView
                        ? _buildGridView(filteredIssues)
                        : _buildListView(filteredIssues),
                  );
                } else if (state is IssuesError) {
                  return Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.error_outline,
                            size: 64, color: Colors.red),
                        const SizedBox(height: 16),
                        Text(state.message),
                        const SizedBox(height: 16),
                        ElevatedButton(
                          onPressed: () {
                            context
                                .read<IssuesBloc>()
                                .add(LoadIssuesEvent());
                          },
                          child: const Text('Retry'),
                        ),
                      ],
                    ),
                  );
                }
                return const SizedBox.shrink();
              },
            ),
          ),
          const AdBannerWidget(),
        ],
      ),
    );
  }

  Widget _buildGridView(List<Issue> issues) {
    return MasonryGridView.count(
      crossAxisCount: 2,
      mainAxisSpacing: 8,
      crossAxisSpacing: 8,
      padding: const EdgeInsets.only(
        left: 16,
        right: 16,
        top: 8,
        bottom: 72,
      ),
      itemCount: issues.length,
      itemBuilder: (context, index) {
        // Alternate tile heights so the stagger is visible even before images load.
        // Odd-indexed cards are ~20 % taller, giving a natural Pinterest-style rhythm.
        final double height = index.isOdd ? 180.0 : 148.0;

        return SizedBox(
          height: height,
          child: IssueCard(
            issue: issues[index],
            index: index,
            isGridView: true,
          ),
        );
      },
    );
  }

  Widget _buildListView(List<Issue> issues) {
    return ListView.builder(
      padding: const EdgeInsets.only(
        left: 16,
        right: 16,
        top: 8,
        bottom: 72,
      ),
      itemCount: issues.length,
      itemBuilder: (context, index) {
        return Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: SizedBox(
            height: 120,
            child: IssueCard(
              issue: issues[index],
              index: index,
              isGridView: false,
            ),
          ),
        );
      },
    );
  }
}