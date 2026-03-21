import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../data/datasources/issue_local_datasource.dart';
import '../../blocs/prayer/prayer_bloc.dart';
import '../../blocs/prayer/prayer_event.dart';
import '../../blocs/prayer/prayer_state.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_state.dart';
import '../../widgets/ad_banner_widget.dart';
import '../../widgets/prayer_card.dart';
import '../../widgets/prayer/prayer_filter_sheet.dart';
import '../subscription/subscription_page.dart';
import 'prayer_submission_page.dart';
import '../auth/login_page.dart';

class PrayerFeedPage extends StatelessWidget {
  const PrayerFeedPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<PrayerBloc>()..add(LoadPrayersEvent()),
      child: const PrayerFeedView(),
    );
  }
}

class PrayerFeedView extends StatefulWidget {
  const PrayerFeedView({Key? key}) : super(key: key);

  @override
  State<PrayerFeedView> createState() => _PrayerFeedViewState();
}

class _PrayerFeedViewState extends State<PrayerFeedView> {
  final ScrollController _scrollController = ScrollController();
  List<String> _categories = [];
  // Caches the last PrayerLoaded so the list stays visible while refreshing.
  PrayerLoaded? _lastLoaded;

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
      final state = context.read<PrayerBloc>().state;
      if (state is PrayerLoaded && state.hasMore) {
        context.read<PrayerBloc>().add(
          LoadMorePrayersEvent(
            page: state.currentPage + 1,
            category: state.currentCategory,
            sortBy: state.currentSortBy,
            hasPrayers: state.currentHasPrayers,
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
    final state = context.read<PrayerBloc>().state;
    String? currentSort;
    bool? currentHasPrayers;
    String? currentCategory;

    if (state is PrayerLoaded) {
      currentSort = state.currentSortBy;
      currentHasPrayers = state.currentHasPrayers;
      currentCategory = state.currentCategory;
    }

    final result = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      isScrollControlled: true,
      builder: (context) => PrayerFilterSheet(
        currentSortBy: currentSort,
        currentHasPrayers: currentHasPrayers,
        currentCategory: currentCategory,
        categories: _categories,
      ),
    );

    if (result != null && mounted) {
      context.read<PrayerBloc>().add(
        ApplyPrayerFiltersEvent(
          sortBy: result['sortBy'],
          hasPrayers: result['hasPrayers'],
          category: result['category'],
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Prayer Requests'),
        actions: [
          // Filter icon with badge if filters are active
          BlocBuilder<PrayerBloc, PrayerState>(
            builder: (context, state) {
              bool hasActiveFilters = false;
              if (state is PrayerLoaded) {
                hasActiveFilters = state.currentSortBy != null ||
                    state.currentHasPrayers != null ||
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

              final content = BlocConsumer<PrayerBloc, PrayerState>(
                listener: (context, state) {
                  if (state is PrayerError) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text(state.message),
                        backgroundColor: Colors.red,
                      ),
                    );
                  }
                },
                builder: (context, state) {
                  // During refresh, PrayerLoading fires but we keep the old
                  // list visible — the RefreshIndicator spinner overlays on top.
                  if (state is PrayerLoading && _lastLoaded == null) {
                    return const Center(child: CircularProgressIndicator());
                  }

                  final displayState = state is PrayerLoaded
                      ? (_lastLoaded = state)
                      : _lastLoaded;

                  if (displayState != null) {
                    final state = displayState; // shadow for the block below
                    return Column(
                      children: [
                        // Active filters chip row
                        if (state.currentSortBy != null ||
                            state.currentHasPrayers != null ||
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
                                          context.read<PrayerBloc>().add(
                                            ApplyPrayerFiltersEvent(
                                              sortBy: null,
                                              hasPrayers:
                                              state.currentHasPrayers,
                                              category:
                                              state.currentCategory,
                                            ),
                                          );
                                        },
                                      ),
                                    ),
                                  if (state.currentHasPrayers != null)
                                    Padding(
                                      padding: const EdgeInsets.only(right: 8),
                                      child: Chip(
                                        label: Text(state.currentHasPrayers!
                                            ? 'Has Prayers'
                                            : 'No Prayers Yet'),
                                        onDeleted: () {
                                          context.read<PrayerBloc>().add(
                                            ApplyPrayerFiltersEvent(
                                              sortBy: state.currentSortBy,
                                              hasPrayers: null,
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
                                          context.read<PrayerBloc>().add(
                                            ApplyPrayerFiltersEvent(
                                              sortBy: state.currentSortBy,
                                              hasPrayers:
                                              state.currentHasPrayers,
                                              category: null,
                                            ),
                                          );
                                        },
                                      ),
                                    ),
                                  TextButton(
                                    onPressed: () {
                                      context
                                          .read<PrayerBloc>()
                                          .add(ClearPrayerFiltersEvent());
                                    },
                                    child: const Text('Clear All'),
                                  ),
                                ],
                              ),
                            ),
                          ),

                        // Prayer list
                        Expanded(
                          child: RefreshIndicator(
                            onRefresh: () => _onRefresh(context),
                            child: state.prayers.isEmpty
                                ? ListView(
                                    physics: const AlwaysScrollableScrollPhysics(),
                                    children: [_buildEmptyState(context)],
                                  )
                                : ListView.builder(
                              controller: _scrollController,
                              padding: EdgeInsets.fromLTRB(
                                  16, 16, 16, showAd ? 90 : 16),
                              itemCount: state.prayers.length +
                                  (state.hasMore ? 1 : 0),
                              itemBuilder: (context, index) {
                                if (index >= state.prayers.length) {
                                  return const Center(
                                    child: Padding(
                                      padding: EdgeInsets.all(16),
                                      child: CircularProgressIndicator(),
                                    ),
                                  );
                                }
                                return PrayerCard(
                                  prayer: state.prayers[index],
                                  onTapPraying: () {
                                    context.read<PrayerBloc>().add(
                                      TogglePrayingEvent(
                                          state.prayers[index].id),
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
              heroTag: 'prayer_fab_login',
              onPressed: () => _navigateToLogin(context),
              icon: const Icon(Icons.login),
              label: const Text('Login to Post'),
            );
          }
          return BlocBuilder<SubscriptionBloc, SubscriptionState>(
            builder: (context, subState) {
              final canPost = subState is SubscriptionLoaded && subState.canPost;
              return FloatingActionButton.extended(
                heroTag: 'prayer_fab_post',
                onPressed: () => _handlePostPrayer(context, canPost),
                icon: const Icon(Icons.add),
                label: const Text('Post Prayer'),
              );
            },
          );
        },
      ),
    );
  }

  /// Pull-to-refresh handler.
  ///
  /// Does NOT pass isRefresh:true so that PrayerLoading is emitted first.
  /// This guarantees a state transition (PrayerLoaded → PrayerLoading → PrayerLoaded)
  /// that firstWhere can always detect, even if the returned data is identical.
  Future<void> _onRefresh(BuildContext context) async {
    final bloc = context.read<PrayerBloc>();
    final s = _lastLoaded;
    final future = bloc.stream
        .firstWhere((st) => st is PrayerLoaded || st is PrayerError)
        .timeout(const Duration(seconds: 15), onTimeout: () => PrayerInitial());
    bloc.add(LoadPrayersEvent(
      category: s?.currentCategory,
      sortBy: s?.currentSortBy,
      hasPrayers: s?.currentHasPrayers,
    ));
    await future;
  }

  String _getSortLabel(String sort) {
    switch (sort) {
      case 'newest':
        return 'Newest';
      case 'oldest':
        return 'Oldest';
      case 'needs_prayer':
        return 'Needs Prayer';
      default:
        return sort;
    }
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          FaIcon(FontAwesomeIcons.personPraying,
              size: 64,
              color: Theme.of(context).colorScheme.primary.withOpacity(0.5)
          ),
          const SizedBox(height: 16),
          Text(
            'No Prayer Requests Found',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(
            'Try adjusting your filters',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Theme.of(context).textTheme.bodySmall?.color,
            ),
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

  void _handlePostPrayer(BuildContext context, bool canPost) {
    /*if (!canPost) {
      _showSubscriptionPaywall(context);
      return;
    }*/

    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const PrayerSubmissionPage()),
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