import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../data/datasources/issue_local_datasource.dart';
import '../../blocs/testimony/testimony_bloc.dart';
import '../../blocs/testimony/testimony_event.dart';
import '../../blocs/testimony/testimony_state.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_state.dart';
import '../../widgets/ad_banner_widget.dart';
import '../../widgets/testimony_card.dart';
import '../../widgets/testimony/testimony_filter_sheet.dart';
import '../subscription/subscription_page.dart';
import 'testimony_submission_page.dart';
import '../auth/login_page.dart';

class TestimonyFeedPage extends StatelessWidget {
  const TestimonyFeedPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<TestimonyBloc>()..add(LoadTestimoniesEvent()),
      child: const TestimonyFeedView(),
    );
  }
}

class TestimonyFeedView extends StatefulWidget {
  const TestimonyFeedView({Key? key}) : super(key: key);

  @override
  State<TestimonyFeedView> createState() => _TestimonyFeedViewState();
}

class _TestimonyFeedViewState extends State<TestimonyFeedView> {
  final ScrollController _scrollController = ScrollController();
  List<String> _categories = [];
  // Caches the last TestimonyLoaded so the list stays visible while refreshing.
  TestimonyLoaded? _lastLoaded;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    try {
      final issues = await di.sl<IssueLocalDataSource>().getAllIssues();
      if (!mounted || issues.isEmpty) return;
      final rng = Random();
      final count = rng.nextInt(5) + 5; // 5–9 items
      final shuffled = List.of(issues)..shuffle(rng);
      setState(() {
        _categories = shuffled.take(count).map((i) => i.name).toList();
      });
    } catch (_) {}
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_isBottom) {
      final state = context.read<TestimonyBloc>().state;
      if (state is TestimonyLoaded && state.hasMore) {
        context.read<TestimonyBloc>().add(
          LoadMoreTestimoniesEvent(
            page: state.currentPage + 1,
            category: state.currentCategory,
            sortBy: state.currentSortBy,
            linkedToPrayer: state.currentLinkedToPrayer,
            hasPraise: state.currentHasPraise,
          ),
        );
      }
    }
  }

  bool get _isBottom {
    if (!_scrollController.hasClients) return false;
    final maxScroll = _scrollController.position.maxScrollExtent;
    final currentScroll = _scrollController.offset;
    return currentScroll >= (maxScroll * 0.9);
  }

  bool _showAd(SubscriptionState subState, AuthState authState) {
    if (authState is! Authenticated) return true;
    if (subState is SubscriptionLoaded) return !subState.canPost;
    return true;
  }

  void _showFilterSheet() async {
    final state = context.read<TestimonyBloc>().state;
    String? currentSort;
    bool? currentLinkedToPrayer;
    bool? currentHasPraise;
    String? currentCategory;

    if (state is TestimonyLoaded) {
      currentSort = state.currentSortBy;
      currentLinkedToPrayer = state.currentLinkedToPrayer;
      currentHasPraise = state.currentHasPraise;
      currentCategory = state.currentCategory;
    }

    final result = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      isScrollControlled: true,
      builder: (context) => TestimonyFilterSheet(
        currentSortBy: currentSort,
        currentLinkedToPrayer: currentLinkedToPrayer,
        currentHasPraise: currentHasPraise,
        currentCategory: currentCategory,
        categories: _categories,
      ),
    );

    if (result != null && mounted) {
      context.read<TestimonyBloc>().add(
        ApplyTestimonyFiltersEvent(
          sortBy: result['sortBy'],
          linkedToPrayer: result['linkedToPrayer'],
          hasPraise: result['hasPraise'],
          category: result['category'],
        ),
      );
    }
  }

  Future<void> _onRefresh(BuildContext context) async {
    final bloc = context.read<TestimonyBloc>();
    final s = _lastLoaded;
    final future = bloc.stream
        .firstWhere((st) => st is TestimonyLoaded || st is TestimonyError)
        .timeout(const Duration(seconds: 15), onTimeout: () => TestimonyInitial());
    bloc.add(LoadTestimoniesEvent(
      category: s?.currentCategory,
      sortBy: s?.currentSortBy,
      linkedToPrayer: s?.currentLinkedToPrayer,
      hasPraise: s?.currentHasPraise,
    ));
    await future;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Testimonies'),
        actions: [
          // Filter icon with badge if filters are active
          BlocBuilder<TestimonyBloc, TestimonyState>(
            builder: (context, state) {
              bool hasActiveFilters = false;
              if (state is TestimonyLoaded) {
                hasActiveFilters = state.currentSortBy != null ||
                    state.currentLinkedToPrayer != null ||
                    state.currentHasPraise != null ||
                    state.currentCategory != null;
              }

              return IconButton(
                icon: hasActiveFilters
                    ? Badge(
                  label: const Text('!'),
                  child: const Icon(Icons.filter_list),
                )
                    : const Icon(Icons.filter_list),
                onPressed: _showFilterSheet,
                tooltip: 'Filter',
              );
            },
          ),
        ],
      ),
      body: BlocBuilder<AuthBloc, AuthState>(
        builder: (context, authState) {
          return BlocBuilder<SubscriptionBloc, SubscriptionState>(
            builder: (context, subState) {
              final showAd = _showAd(subState, authState);

              final content = BlocConsumer<TestimonyBloc, TestimonyState>(
                listener: (context, state) {
                  if (state is TestimonyError) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text(state.message),
                        backgroundColor: Colors.red,
                      ),
                    );
                  }
                },
                builder: (context, state) {
                  // During refresh, TestimonyLoading fires but we keep the old
                  // list visible — the RefreshIndicator spinner overlays on top.
                  if (state is TestimonyLoading && _lastLoaded == null) {
                    return const Center(child: CircularProgressIndicator());
                  }

                  final displayState = state is TestimonyLoaded
                      ? (_lastLoaded = state)
                      : _lastLoaded;

                  if (displayState != null) {
                    final state = displayState; // shadow for the block below
                    return Column(
                      children: [
                        // Active filters chip row
                        if (state.currentSortBy != null ||
                            state.currentLinkedToPrayer != null ||
                            state.currentHasPraise != null ||
                            state.currentCategory != null)
                          Container(
                            width: double.infinity,
                            padding: const EdgeInsets.all(8),
                            color: Theme.of(context)
                                .colorScheme
                                .primaryContainer
                                .withOpacity(0.3),
                            child: SingleChildScrollView(
                              scrollDirection: Axis.horizontal,
                              child: Row(
                                children: [
                                  const Icon(Icons.filter_alt, size: 16),
                                  const SizedBox(width: 8),
                                  if (state.currentSortBy != null)
                                    Padding(
                                      padding: const EdgeInsets.only(right: 8),
                                      child: Chip(
                                        label: Text(_getSortLabel(
                                            state.currentSortBy!)),
                                        onDeleted: () {
                                          context.read<TestimonyBloc>().add(
                                            ApplyTestimonyFiltersEvent(
                                              sortBy: null,
                                              linkedToPrayer: state
                                                  .currentLinkedToPrayer,
                                              hasPraise:
                                              state.currentHasPraise,
                                              category:
                                              state.currentCategory,
                                            ),
                                          );
                                        },
                                      ),
                                    ),
                                  if (state.currentLinkedToPrayer != null)
                                    Padding(
                                      padding: const EdgeInsets.only(right: 8),
                                      child: Chip(
                                        label: Text(
                                            state.currentLinkedToPrayer!
                                                ? 'Linked to Prayer'
                                                : 'Standalone'),
                                        onDeleted: () {
                                          context.read<TestimonyBloc>().add(
                                            ApplyTestimonyFiltersEvent(
                                              sortBy: state.currentSortBy,
                                              linkedToPrayer: null,
                                              hasPraise:
                                              state.currentHasPraise,
                                              category:
                                              state.currentCategory,
                                            ),
                                          );
                                        },
                                      ),
                                    ),
                                  if (state.currentHasPraise != null)
                                    Padding(
                                      padding: const EdgeInsets.only(right: 8),
                                      child: Chip(
                                        label: Text(state.currentHasPraise!
                                            ? 'Has Praise'
                                            : 'No Praise Yet'),
                                        onDeleted: () {
                                          context.read<TestimonyBloc>().add(
                                            ApplyTestimonyFiltersEvent(
                                              sortBy: state.currentSortBy,
                                              linkedToPrayer: state
                                                  .currentLinkedToPrayer,
                                              hasPraise: null,
                                              category:
                                              state.currentCategory,
                                            ),
                                          );
                                        },
                                      ),
                                    ),
                                  if (state.currentCategory != null)
                                    Padding(
                                      padding: const EdgeInsets.only(right: 8),
                                      child: Chip(
                                        label: Text(state.currentCategory!),
                                        onDeleted: () {
                                          context.read<TestimonyBloc>().add(
                                            ApplyTestimonyFiltersEvent(
                                              sortBy: state.currentSortBy,
                                              linkedToPrayer: state
                                                  .currentLinkedToPrayer,
                                              hasPraise:
                                              state.currentHasPraise,
                                              category: null,
                                            ),
                                          );
                                        },
                                      ),
                                    ),
                                  TextButton(
                                    onPressed: () {
                                      context
                                          .read<TestimonyBloc>()
                                          .add(ClearTestimonyFiltersEvent());
                                    },
                                    child: const Text('Clear All'),
                                  ),
                                ],
                              ),
                            ),
                          ),

                        // Testimony list
                        Expanded(
                          child: RefreshIndicator(
                            onRefresh: () => _onRefresh(context),
                            child: state.testimonies.isEmpty
                                ? ListView(
                                    physics: const AlwaysScrollableScrollPhysics(),
                                    children: [_buildEmptyState(context)],
                                  )
                                : ListView.builder(
                              controller: _scrollController,
                              padding: EdgeInsets.fromLTRB(
                                  16, 16, 16, showAd ? 90 : 16),
                              itemCount: state.testimonies.length +
                                  (state.hasMore ? 1 : 0),
                              itemBuilder: (context, index) {
                                if (index >= state.testimonies.length) {
                                  return const Center(
                                    child: Padding(
                                      padding: EdgeInsets.all(16),
                                      child: CircularProgressIndicator(),
                                    ),
                                  );
                                }
                                return TestimonyCard(
                                  testimony: state.testimonies[index],
                                  onTapPraise: () {
                                    context.read<TestimonyBloc>().add(
                                      TogglePraiseEvent(state
                                          .testimonies[index].id),
                                    );
                                  },
                                );
                              },
                            ),
                          ),
                        ),
                      ],
                    );
                  }

                  return const SizedBox();
                },
              );

              return Stack(
                children: [
                  Positioned.fill(child: content),
                  if (showAd)
                    const Positioned(
                      left: 0,
                      right: 0,
                      bottom: 0,
                      child: AdBannerWidget(),
                    ),
                ],
              );
            },
          );
        },
      ),
      floatingActionButton: BlocBuilder<AuthBloc, AuthState>(
        builder: (context, authState) {
          if (authState is! Authenticated) {
            return FloatingActionButton.extended(
              heroTag: 'testimony_fab_login',
              onPressed: () => _navigateToLogin(context),
              icon: const Icon(Icons.login),
              label: const Text('Login to Post'),
            );
          }
          return BlocBuilder<SubscriptionBloc, SubscriptionState>(
            builder: (context, subState) {
              final canPost = subState is SubscriptionLoaded && subState.canPost;
              return FloatingActionButton.extended(
                heroTag: 'testimony_fab_post',
                onPressed: () => _handlePostTestimony(context, canPost),
                icon: const Icon(Icons.add),
                label: const Text('Share Testimony'),
              );
            },
          );
        },
      ),
    );
  }

  String _getSortLabel(String sort) {
    switch (sort) {
      case 'newest':
        return 'Newest';
      case 'oldest':
        return 'Oldest';
      case 'most_praised':
        return 'Most Praised';
      default:
        return sort;
    }
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.auto_awesome,
            size: 64,
            color: Theme.of(context).colorScheme.primary.withOpacity(0.5),
          ),
          const SizedBox(height: 16),
          Text(
            'No Testimonies Found',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(
            'Try adjusting your filters',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Theme.of(context).textTheme.bodySmall?.color,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  void _navigateToLogin(BuildContext context) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const LoginPage()),
    );
  }

  void _handlePostTestimony(BuildContext context, bool canPost) {
    if (!canPost) {
      _showSubscriptionPaywall(context);
      return;
    }

    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const TestimonySubmissionPage()),
    );
  }

  void _showSubscriptionPaywall(BuildContext context) {
    final outerContext = context;
    showModalBottomSheet(
      context: context,
      builder: (sheetContext) => Container(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.stars,
              size: 64,
              color: Theme.of(sheetContext).colorScheme.primary,
            ),
            const SizedBox(height: 16),
            Text(
              'Subscription Required',
              style: Theme.of(sheetContext).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            const Text(
              'Subscribe to post prayer requests and testimonies',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: () {
                Navigator.pop(sheetContext);
                Navigator.push(
                  outerContext,
                  MaterialPageRoute(builder: (_) => const SubscriptionPage()),
                );
              },
              child: const Text('View Plans'),
            ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: () => Navigator.pop(sheetContext),
              child: const Text('Maybe Later'),
            ),
          ],
        ),
      ),
    );
  }
}