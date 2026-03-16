import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/di/injection_container.dart' as di;
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
  final List<String> _categories = [
    'Anxiety',
    'Depression',
    'Fear',
    'Loneliness',
    'Forgiveness',
    'Health',
    'Family',
    'Work',
    'Relationships',
  ];

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
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
                  if (state is TestimonyLoading) {
                    return const Center(child: CircularProgressIndicator());
                  }

                  if (state is TestimonyLoaded) {
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
                          child: state.testimonies.isEmpty
                              ? _buildEmptyState(context)
                              : RefreshIndicator(
                            onRefresh: () async {
                              context.read<TestimonyBloc>().add(
                                LoadTestimoniesEvent(
                                  isRefresh: true,
                                  category: state.currentCategory,
                                  sortBy: state.currentSortBy,
                                  linkedToPrayer:
                                  state.currentLinkedToPrayer,
                                  hasPraise: state.currentHasPraise,
                                ),
                              );
                            },
                            child: ListView.builder(
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
              onPressed: () => _navigateToLogin(context),
              icon: const Icon(Icons.login),
              label: const Text('Login to Post'),
            );
          }
          return BlocBuilder<SubscriptionBloc, SubscriptionState>(
            builder: (context, subState) {
              final canPost = subState is SubscriptionLoaded && subState.canPost;
              return FloatingActionButton.extended(
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
    showModalBottomSheet(
      context: context,
      builder: (context) => Container(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.stars,
              size: 64,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(height: 16),
            Text(
              'Subscription Required',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            const Text(
              'Subscribe to post prayer requests and testimonies',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: () {
                Navigator.pop(context);
                // TODO: Navigate to subscription page
              },
              child: const Text('View Plans'),
            ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Maybe Later'),
            ),
          ],
        ),
      ),
    );
  }
}