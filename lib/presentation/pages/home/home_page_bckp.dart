// lib/presentation/pages/home/home_page.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/constants/app_colors.dart';
import '../../../core/constants/app_strings.dart';
import '../../../core/services/notification_storage.dart';
import '../../blocs/daily_verse/daily_verse_bloc.dart';
import '../../blocs/issues/issues_bloc.dart';
import '../../blocs/random_verse/random_verse_bloc.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/verses/verses_bloc.dart';
import '../../widgets/ad_banner_widget.dart';
import '../../widgets/daily_verse_card.dart';
import '../../widgets/issue_card.dart';
import '../../widgets/random_verse_dialog.dart';
import '../../widgets/community_strip.dart';
import '../all_issues_page.dart';
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
    return Scaffold(
      appBar: AppBar(
        title: const Text(AppStrings.appName),
        actions: [
          // Notifications icon (only for authenticated users)
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
                        // Refresh to update badge
                        if (mounted) setState(() {});
                      },
                    );
                  },
                );
              }
              return const SizedBox.shrink();
            },
          ),

          // Profile icon (always visible)
          IconButton(
            icon: const Icon(Icons.person),
            tooltip: 'Profile',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => const ProfilePage(),
                ),
              );
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          context.read<DailyVerseBloc>().add(LoadDailyVerseEvent());
          context.read<IssuesBloc>().add(LoadIssuesEvent());
        },
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Greeting Section
              Padding(
                padding: const EdgeInsets.all(16),
                child: BlocBuilder<AuthBloc, AuthState>(
                  builder: (context, authState) {
                    if (authState is Authenticated) {
                      return Text(
                        'Hello, ${authState.user.name}',
                        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      );
                    }
                    return Text(
                      'Hello,',
                      style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                    );
                  },
                ),
              ),

              // Daily Verse Section
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

              // Community Strip (only for authenticated users)
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

              // Random Verse Button
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: () => _showRandomVerse(context),
                    icon: const Icon(Icons.shuffle),
                    label: const Text('Get Random Verse'),
                    style: ElevatedButton.styleFrom(
                      padding: const EdgeInsets.all(16),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                  ),
                ),
              ),

              const SizedBox(height: 32),

              // Issues Section Header
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      AppStrings.issues,
                      style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
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

              // Issues Horizontal List
              BlocBuilder<IssuesBloc, IssuesState>(
                builder: (context, state) {
                  if (state is IssuesLoading) {
                    return _buildLoadingIssues();
                  } else if (state is IssuesLoaded) {
                    return SizedBox(
                      height: 140,
                      child: ListView.builder(
                        scrollDirection: Axis.horizontal,
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                        itemCount: state.issues.length,
                        itemBuilder: (context, index) {
                          return IssueCard(
                              issue: state.issues[index],
                              index: index,
                              isGridView: false
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

              const AdBannerWidget(),
            ],
          ),
        ),
      ),
    );
  }

  void _showRandomVerse(BuildContext context) {
    // Load a random verse and show it
    context.read<VersesBloc>().add(LoadRandomVerseForHomeEvent());

    // Show dialog with the random verse
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
          child: const Center(
            child: CircularProgressIndicator(),
          ),
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
      height: 180,
      child: ListView.builder(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemCount: 5,
        itemBuilder: (context, index) {
          return Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4),
            child: Card(
              child: Container(
                width: 140,
                padding: const EdgeInsets.all(16),
                child: const Center(
                  child: CircularProgressIndicator(),
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