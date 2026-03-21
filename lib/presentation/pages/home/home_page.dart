// lib/presentation/pages/home/home_page.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/constants/app_strings.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/services/notification_storage.dart';
import '../../blocs/daily_verse/daily_verse_bloc.dart';
import '../../blocs/issues/issues_bloc.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_state.dart';
import '../../blocs/verses/verses_bloc.dart';
import '../../widgets/ad_banner_widget.dart';
import '../../widgets/daily_verse_card.dart';
import '../../widgets/issue_card.dart';
import '../../widgets/random_verse_dialog.dart';
import '../../widgets/community_strip.dart';
import '../favorites/favorites_page.dart';
import '../notifications/notifications_page.dart';
import '../../../core/services/notification_handler.dart';

class HomePage extends StatefulWidget {
  /// Called with the bottom nav tab index to switch to.
  final void Function(int)? onNavigateToTab;

  const HomePage({Key? key, this.onNavigateToTab}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  void initState() {
    super.initState();
    // Request notification permission after the home page has rendered
    WidgetsBinding.instance.addPostFrameCallback((_) {
      NotificationHandler().requestPermission();
    });
  }

  Future<int> _getUnreadCount() async {
    final prefs = await SharedPreferences.getInstance();
    final storage = NotificationStorage(prefs);
    return await storage.getUnreadCount();
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      body: Stack(
        children: [
          RefreshIndicator(
            onRefresh: () async {
              context.read<DailyVerseBloc>().add(LoadDailyVerseEvent());
              context.read<IssuesBloc>().add(LoadIssuesEvent());
            },
            child: CustomScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              slivers: [
                // ── Gradient SliverAppBar ─────────────────────────────────
                SliverAppBar(
                  expandedHeight: 160,
                  pinned: true,
                  backgroundColor: AppTheme.primarySeed,
                  foregroundColor: Colors.white,
                  title: Text(
                    AppStrings.appName +" - Scripture | Prayer | Testimony" ,
                    style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 18,
                    ),
                  ),
                  actions: [
                    // Notifications bell (authenticated only)
                    BlocBuilder<AuthBloc, AuthState>(
                      builder: (context, authState) {
                        if (authState is Authenticated) {
                          return FutureBuilder<int>(
                            future: _getUnreadCount(),
                            builder: (context, snapshot) {
                              final unread = snapshot.data ?? 0;
                              return IconButton(
                                icon: unread > 0
                                    ? Badge(
                                        label: Text('$unread'),
                                        child: const Icon(
                                            Icons.notifications,
                                            color: Colors.white),
                                      )
                                    : const Icon(Icons.notifications,
                                        color: Colors.white),
                                tooltip: 'Notifications',
                                onPressed: () async {
                                  await Navigator.push(
                                    context,
                                    MaterialPageRoute(
                                      builder: (_) =>
                                          const NotificationsPage(),
                                    ),
                                  );
                                  if (mounted) setState(() {});
                                },
                              );
                            },
                          );
                        }
                        return const SizedBox.shrink();
                      },
                    ),
                  ],
                  flexibleSpace: FlexibleSpaceBar(
                    collapseMode: CollapseMode.pin,
                    background: Container(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topLeft,
                          end: Alignment.bottomRight,
                          colors: [
                            AppTheme.primarySeed,
                            AppTheme.secondarySeed,
                          ],
                        ),
                      ),
                      child: SafeArea(
                        child: Padding(
                          padding: const EdgeInsets.fromLTRB(20, 8, 16, 16),
                          child: Row(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              // ── Greeting (left) ──────────────────────
                              Expanded(
                                child: BlocBuilder<AuthBloc, AuthState>(
                                  builder: (context, authState) {
                                    final firstName = authState
                                            is Authenticated
                                        ? authState.user.name
                                            .split(' ')
                                            .first
                                        : null;
                                    return Column(
                                      mainAxisAlignment:
                                          MainAxisAlignment.end,
                                      crossAxisAlignment:
                                          CrossAxisAlignment.start,
                                      children: [
                                        Text(
                                          firstName != null
                                              ? 'Hello, $firstName 👋'
                                              : 'Hello! 👋',
                                          style: const TextStyle(
                                            color: Colors.white,
                                            fontSize: 22,
                                            fontWeight: FontWeight.bold,
                                          ),
                                        ),
                                        const SizedBox(height: 4),
                                        Text(
                                          'Wait with expectation',
                                          style: TextStyle(
                                            color: Colors.white
                                                .withOpacity(0.8),
                                            fontSize: 13,
                                          ),
                                        ),
                                      ],
                                    );
                                  },
                                ),
                              ),

                              // ── Profile display (right) ───────────────
                              BlocBuilder<AuthBloc, AuthState>(
                                builder: (context, authState) {
                                  return GestureDetector(
                                    onTap: () =>
                                        widget.onNavigateToTab?.call(4),
                                    child: Container(
                                      padding: const EdgeInsets.symmetric(
                                          horizontal: 12, vertical: 8),
                                      decoration: BoxDecoration(
                                        color:
                                            Colors.white.withOpacity(0.2),
                                        borderRadius:
                                            BorderRadius.circular(30),
                                      ),
                                      child: Row(
                                        mainAxisSize: MainAxisSize.min,
                                        children: [
                                          if (authState is Authenticated)
                                            ...[
                                            CircleAvatar(
                                              radius: 16,
                                              backgroundColor:
                                                  Colors.white,
                                              backgroundImage: (authState.user.profileImageUrl != null &&
                                                      authState.user.profileImageUrl!.isNotEmpty)
                                                  ? NetworkImage(authState.user.profileImageUrl!)
                                                  : null,
                                              child: (authState.user.profileImageUrl == null ||
                                                      authState.user.profileImageUrl!.isEmpty)
                                                  ? Text(
                                                      authState.user.name[0]
                                                          .toUpperCase(),
                                                      style: TextStyle(
                                                        color:
                                                            AppTheme.primarySeed,
                                                        fontWeight:
                                                            FontWeight.bold,
                                                        fontSize: 14,
                                                      ),
                                                    )
                                                  : null,
                                            ),
                                            const SizedBox(width: 8),
                                            Text(
                                              authState.user.name
                                                  .split(' ')
                                                  .first,
                                              style: const TextStyle(
                                                color: Colors.white,
                                                fontWeight: FontWeight.w600,
                                                fontSize: 14,
                                              ),
                                            ),
                                            // Premium badge
                                            BlocBuilder<SubscriptionBloc,
                                                SubscriptionState>(
                                              builder: (context, subState) {
                                                final isPremium =
                                                    subState
                                                            is SubscriptionLoaded &&
                                                        subState.canPost;
                                                if (!isPremium) {
                                                  return const SizedBox
                                                      .shrink();
                                                }
                                                return const Padding(
                                                  padding: EdgeInsets.only(
                                                      left: 4),
                                                  child: Icon(
                                                    Icons
                                                        .workspace_premium,
                                                    color: Colors.amber,
                                                    size: 16,
                                                  ),
                                                );
                                              },
                                            ),
                                          ] else ...[
                                            const Icon(
                                              Icons.person_outline,
                                              color: Colors.white,
                                              size: 18,
                                            ),
                                            const SizedBox(width: 6),
                                            const Text(
                                              'Guest',
                                              style: TextStyle(
                                                color: Colors.white,
                                                fontSize: 14,
                                              ),
                                            ),
                                          ],
                                        ],
                                      ),
                                    ),
                                  );
                                },
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ),
                ),

                // ── Page content ─────────────────────────────────────────
                SliverToBoxAdapter(
                  child: Padding(
                    padding: const EdgeInsets.only(bottom: 90),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const SizedBox(height: 16),

                        // Daily verse card
                        BlocBuilder<DailyVerseBloc, DailyVerseState>(
                          builder: (context, state) {
                            if (state is DailyVerseLoading) {
                              return _buildLoadingCard();
                            } else if (state is DailyVerseLoaded) {
                              return DailyVerseCard(verse: state.verse);
                            } else if (state is DailyVerseError) {
                              return _buildErrorCard(
                                  context, state.message);
                            }
                            return const SizedBox.shrink();
                          },
                        ),

                        const SizedBox(height: 18),

                        // Two-button row
                        Padding(
                          padding:
                              const EdgeInsets.symmetric(horizontal: 16),
                          child: Row(
                            children: [
                              Expanded(
                                child: ElevatedButton.icon(
                                  onPressed: () =>
                                      _showRandomVerse(context),
                                  icon: const Icon(Icons.shuffle, size: 18),
                                  label: const Text('Random Verse'),
                                  style: ElevatedButton.styleFrom(
                                    padding: const EdgeInsets.symmetric(
                                        vertical: 14),
                                    shape: RoundedRectangleBorder(
                                      borderRadius:
                                          BorderRadius.circular(12),
                                    ),
                                    backgroundColor: cs.primaryContainer,
                                    foregroundColor: cs.onPrimaryContainer,
                                    elevation: 4,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: ElevatedButton.icon(
                                  onPressed: () {
                                    Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder: (_) =>
                                            const FavoritesPage(),
                                      ),
                                    );
                                  },
                                  icon: const Icon(Icons.favorite, size: 18),
                                  label: const Text('Favourite verses'),
                                  style: ElevatedButton.styleFrom(
                                    padding: const EdgeInsets.symmetric(
                                        vertical: 14),
                                    shape: RoundedRectangleBorder(
                                      borderRadius:
                                          BorderRadius.circular(12),
                                    ),
                                    backgroundColor: cs.secondaryContainer,
                                    foregroundColor:
                                        cs.onSecondaryContainer,
                                    elevation: 4,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ),

                        const SizedBox(height: 24),

                        // Community strip (authenticated only)
                        BlocBuilder<AuthBloc, AuthState>(
                          builder: (context, authState) {
                            if (authState is Authenticated) {
                              return Padding(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 16),
                                child: CommunityStrip(
                                  onNavigateToTab: widget.onNavigateToTab,
                                ),
                              );
                            }
                            return const SizedBox.shrink();
                          },
                        ),

                        const SizedBox(height: 32),

                        // Issues section header
                        Padding(
                          padding:
                              const EdgeInsets.symmetric(horizontal: 16),
                          child: Row(
                            mainAxisAlignment:
                                MainAxisAlignment.spaceBetween,
                            children: [
                              Text(
                                AppStrings.issues,
                                style: Theme.of(context)
                                    .textTheme
                                    .headlineSmall
                                    ?.copyWith(
                                        fontWeight: FontWeight.bold),
                              ),
                              TextButton(
                                onPressed: () =>
                                    widget.onNavigateToTab?.call(1),
                                child: const Text('View All'),
                              ),
                            ],
                          ),
                        ),

                        const SizedBox(height: 16),

                        // Horizontal issues strip
                        BlocBuilder<IssuesBloc, IssuesState>(
                          builder: (context, state) {
                            if (state is IssuesLoading) {
                              return _buildLoadingIssues();
                            } else if (state is IssuesLoaded) {
                              return SizedBox(
                                height: 140,
                                child: ListView.builder(
                                  scrollDirection: Axis.horizontal,
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 12),
                                  itemCount: state.issues.length,
                                  itemBuilder: (context, index) {
                                    return Padding(
                                      padding: const EdgeInsets.symmetric(
                                          horizontal: 4),
                                      child: SizedBox(
                                        width: 160,
                                        child: IssueCard(
                                          issue: state.issues[index],
                                          index: index,
                                          isGridView: false,
                                          heroTagPrefix: 'home',
                                        ),
                                      ),
                                    );
                                  },
                                ),
                              );
                            } else if (state is IssuesError) {
                              return _buildErrorIssues(
                                  context, state.message);
                            }
                            return const SizedBox.shrink();
                          },
                        ),

                        const SizedBox(height: 24),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),

          // Bottom-pinned ad banner
          BlocBuilder<AuthBloc, AuthState>(
            builder: (context, authState) {
              return BlocBuilder<SubscriptionBloc, SubscriptionState>(
                builder: (context, subState) {
                  final showAd = authState is! Authenticated ||
                      !(subState is SubscriptionLoaded && subState.canPost);
                  if (!showAd) return const SizedBox.shrink();
                  return const Positioned(
                    left: 0,
                    right: 0,
                    bottom: 0,
                    child: AdBannerWidget(),
                  );
                },
              );
            },
          ),
        ],
      ),
    );
  }

  void _showRandomVerse(BuildContext context) {
    context.read<VersesBloc>().add(LoadRandomVerseForHomeEvent());
    showDialog(
      context: context,
      builder: (dialogContext) => BlocBuilder<VersesBloc, VersesState>(
        builder: (context, state) {
          if (state is RandomVerseForHomeLoaded && state.isRandom) {
            return RandomVerseDialog(verse: state.verse);
          }
          return const AlertDialog(
            content: SizedBox(
              height: 100,
              child: Center(child: CircularProgressIndicator()),
            ),
          );
        },
      ),
    );
  }

  Widget _buildLoadingCard() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Card(
        child: Container(
          height: 200,
          padding: const EdgeInsets.all(16),
          child: const Center(child: CircularProgressIndicator()),
        ),
      ),
    );
  }

  Widget _buildErrorCard(BuildContext context, String message) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Card(
        color: AppColors.error.withOpacity(0.1),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              const Icon(Icons.error_outline, color: AppColors.error, size: 48),
              const SizedBox(height: 8),
              Text(
                message,
                style: const TextStyle(color: AppColors.error),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () {
                  context.read<DailyVerseBloc>().add(LoadDailyVerseEvent());
                },
                child: const Text(AppStrings.retry),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildLoadingIssues() {
    return SizedBox(
      height: 140,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemCount: 5,
        itemBuilder: (context, index) {
          return Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: SizedBox(
              width: 160,
              child: Card(
                child: Container(
                  padding: const EdgeInsets.all(16),
                  child: const Center(child: CircularProgressIndicator()),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildErrorIssues(BuildContext context, String message) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Card(
        color: AppColors.error.withOpacity(0.1),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            children: [
              const Icon(Icons.error_outline, color: AppColors.error, size: 48),
              const SizedBox(height: 8),
              Text(
                message,
                style: const TextStyle(color: AppColors.error),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () {
                  context.read<IssuesBloc>().add(LoadIssuesEvent());
                },
                child: const Text(AppStrings.retry),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
