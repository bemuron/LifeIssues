// lib/presentation/pages/home/home_page.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/constants/app_strings.dart';
import '../../../core/services/notification_storage.dart';
import '../../blocs/daily_verse/daily_verse_bloc.dart';
import '../../blocs/issues/issues_bloc.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/verses/verses_bloc.dart';
import '../../widgets/ad_banner_widget.dart';
import '../../widgets/daily_verse_card.dart';
import '../../widgets/issue_card.dart';
import '../../widgets/random_verse_dialog.dart';
import '../../widgets/community_strip.dart';
import '../all_issues_page.dart';
import '../favorites/favorites_page.dart';
import '../profile/profile_page.dart';
import '../notifications/notifications_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  Future<int> _getUnreadCount() async {
    final prefs = await SharedPreferences.getInstance();
    final storage = NotificationStorage(prefs);
    return await storage.getUnreadCount();
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text(AppStrings.appName),
        actions: [
          BlocBuilder<AuthBloc, AuthState>(
            builder: (context, authState) {
              if (authState is Authenticated) {
                return FutureBuilder<int>(
                  future: _getUnreadCount(),
                  builder: (context, snapshot) {
                    final unreadCount = snapshot.data ?? 0;
                    return IconButton(
                      icon: unreadCount > 0
                          ? Badge(
                        label: Text('$unreadCount'),
                        child: const Icon(Icons.notifications),
                      )
                          : const Icon(Icons.notifications),
                      tooltip: 'Notifications',
                      onPressed: () async {
                        await Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => const NotificationsPage(),
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
          IconButton(
            icon: const Icon(Icons.person),
            tooltip: 'Profile',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const ProfilePage()),
              );
            },
          ),
        ],
      ),

      body: Stack(
        children: [
          // Scrollable content with bottom padding so the ad never covers it
          RefreshIndicator(
            onRefresh: () async {
              context.read<DailyVerseBloc>().add(LoadDailyVerseEvent());
              context.read<IssuesBloc>().add(LoadIssuesEvent());
            },
            child: SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: const EdgeInsets.only(bottom: 90),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Greeting
                  Padding(
                    padding: const EdgeInsets.all(16),
                    child: BlocBuilder<AuthBloc, AuthState>(
                      builder: (context, authState) {
                        final greeting = authState is Authenticated
                            ? 'Hello, ${authState.user.name}'
                            : 'Hello,';
                        return Text(
                          greeting,
                          style: Theme.of(context)
                              .textTheme
                              .headlineSmall
                              ?.copyWith(fontWeight: FontWeight.bold),
                        );
                      },
                    ),
                  ),

                  // Daily verse card
                  BlocBuilder<DailyVerseBloc, DailyVerseState>(
                    builder: (context, state) {
                      if (state is DailyVerseLoading) {
                        return _buildLoadingCard();
                      } else if (state is DailyVerseLoaded) {
                        return DailyVerseCard(verse: state.verse);
                      } else if (state is DailyVerseError) {
                        return _buildErrorCard(context, state.message);
                      }
                      return const SizedBox.shrink();
                    },
                  ),

                  const SizedBox(height: 24),

                  // Community strip (authenticated only)
                  BlocBuilder<AuthBloc, AuthState>(
                    builder: (context, authState) {
                      if (authState is Authenticated) {
                        return const Padding(
                          padding: EdgeInsets.symmetric(horizontal: 16),
                          child: CommunityStrip(),
                        );
                      }
                      return const SizedBox.shrink();
                    },
                  ),

                  const SizedBox(height: 24),

                  // ── Two-button row ──────────────────────────────────────
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Row(
                      children: [
                        // Random verse button
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: () => _showRandomVerse(context),
                            icon: const Icon(Icons.shuffle, size: 18),
                            label: const Text('Random Verse'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(
                                  vertical: 14),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                              backgroundColor: cs.primaryContainer,
                              foregroundColor: cs.onPrimaryContainer,
                              elevation: 0,
                            ),
                          ),
                        ),

                        const SizedBox(width: 12),

                        // Favorites button
                        Expanded(
                          child: ElevatedButton.icon(
                            onPressed: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (_) => const FavoritesPage(),
                                ),
                              );
                            },
                            icon: const Icon(Icons.favorite, size: 18),
                            label: const Text('Favourites'),
                            style: ElevatedButton.styleFrom(
                              padding: const EdgeInsets.symmetric(
                                  vertical: 14),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                              backgroundColor: cs.secondaryContainer,
                              foregroundColor: cs.onSecondaryContainer,
                              elevation: 0,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(height: 32),

                  // Issues section header
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          AppStrings.issues,
                          style: Theme.of(context)
                              .textTheme
                              .headlineSmall
                              ?.copyWith(fontWeight: FontWeight.bold),
                        ),
                        TextButton(
                          onPressed: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => const AllIssuesPage(),
                              ),
                            );
                          },
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
                                  ),
                                ),
                              );
                            },
                          ),
                        );
                      } else if (state is IssuesError) {
                        return _buildErrorIssues(context, state.message);
                      }
                      return const SizedBox.shrink();
                    },
                  ),

                  const SizedBox(height: 24),
                ],
              ),
            ),
          ),

          // Bottom-pinned ad banner
          const Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: AdBannerWidget(),
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