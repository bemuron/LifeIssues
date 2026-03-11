// lib/presentation/pages/testimonies/testimony_feed_page.dart

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
import '../../widgets/testimony_card.dart';
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
        title: const Text('Testimonies'),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: () => _showCategoryFilter(context),
          ),
        ],
      ),
      body: BlocConsumer<TestimonyBloc, TestimonyState>(
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
            if (state.testimonies.isEmpty) {
              return _buildEmptyState(context);
            }

            return RefreshIndicator(
              onRefresh: () async {
                context.read<TestimonyBloc>().add(
                  LoadTestimoniesEvent(
                    isRefresh: true,
                    category: state.currentCategory,
                  ),
                );
              },
              child: ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.all(16),
                itemCount: state.testimonies.length + (state.hasMore ? 1 : 0),
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
                        TogglePraiseEvent(state.testimonies[index].id),
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
            'No Testimonies Yet',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(
            'Be the first to share how God moved in your life',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Theme.of(context).textTheme.bodySmall?.color,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  void _showCategoryFilter(BuildContext context) {
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