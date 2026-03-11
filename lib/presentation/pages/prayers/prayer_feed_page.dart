// lib/presentation/pages/prayers/prayer_feed_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../blocs/prayer/prayer_bloc.dart';
import '../../blocs/prayer/prayer_event.dart';
import '../../blocs/prayer/prayer_state.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_state.dart';
import '../../widgets/prayer_card.dart';
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
      final state = context.read<PrayerBloc>().state;
      if (state is PrayerLoaded && state.hasMore) {
        context.read<PrayerBloc>().add(
          LoadMorePrayersEvent(
            page: state.currentPage + 1,
            category: state.currentCategory,
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Prayer Requests'),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: () => _showCategoryFilter(context),
          ),
        ],
      ),
      body: BlocConsumer<PrayerBloc, PrayerState>(
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
          if (state is PrayerLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is PrayerLoaded) {
            if (state.prayers.isEmpty) {
              return _buildEmptyState(context);
            }

            return RefreshIndicator(
              onRefresh: () async {
                context.read<PrayerBloc>().add(
                  LoadPrayersEvent(
                    isRefresh: true,
                    category: state.currentCategory,
                  ),
                );
              },
              child: ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.all(16),
                itemCount: state.prayers.length + (state.hasMore ? 1 : 0),
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
                        TogglePrayingEvent(state.prayers[index].id),
                      );
                    },
                  );
                },
              ),
            );
          }

          return const SizedBox();
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

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.favorite_border,
            size: 64,
            color: Theme.of(context).colorScheme.primary.withOpacity(0.5),
          ),
          const SizedBox(height: 16),
          Text(
            'No Prayer Requests Yet',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(
            'Be the first to share a prayer request',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Theme.of(context).textTheme.bodySmall?.color,
            ),
          ),
        ],
      ),
    );
  }

  void _showCategoryFilter(BuildContext context) {
    // TODO: Show category filter bottom sheet
    // For now, just show a simple dialog
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Filter by Category'),
        content: const Text('Category filter coming soon'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
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
    if (!canPost) {
      _showSubscriptionPaywall(context);
      return;
    }

    Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const PrayerSubmissionPage()),
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