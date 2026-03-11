// lib/presentation/pages/profile/profile_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/auth/auth_event.dart';
import '../../blocs/prayer/prayer_bloc.dart';
import '../../blocs/prayer/prayer_event.dart';
import '../../blocs/prayer/prayer_state.dart';
import '../../blocs/testimony/testimony_bloc.dart';
import '../../blocs/testimony/testimony_event.dart';
import '../../blocs/testimony/testimony_state.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_state.dart';
import '../../widgets/prayer_card.dart';
import '../../widgets/testimony_card.dart';
import '../auth/login_page.dart';
import '../settings/settings_page.dart';

class ProfilePage extends StatelessWidget {
  const ProfilePage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AuthBloc, AuthState>(
      builder: (context, state) {
        if (state is! Authenticated) {
          return _buildUnauthenticatedView(context);
        }

        return MultiBlocProvider(
          providers: [
            BlocProvider(
              create: (_) => di.sl<PrayerBloc>()..add(LoadMyPrayersEvent()),
            ),
            BlocProvider(
              create: (_) => di.sl<TestimonyBloc>()..add(LoadMyTestimoniesEvent()),
            ),
          ],
          child: ProfileView(user: state.user),
        );
      },
    );
  }

  Widget _buildUnauthenticatedView(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.person_outline,
              size: 64,
              color: Theme.of(context).colorScheme.primary.withOpacity(0.5),
            ),
            const SizedBox(height: 16),
            Text(
              'Login to view your profile',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (_) => const LoginPage()),
                );
              },
              child: const Text('Login'),
            ),
          ],
        ),
      ),
    );
  }
}

class ProfileView extends StatefulWidget {
  final dynamic user; // User entity

  const ProfileView({
    Key? key,
    required this.user,
  }) : super(key: key);

  @override
  State<ProfileView> createState() => _ProfileViewState();
}

class _ProfileViewState extends State<ProfileView> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('My Profile'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const SettingsPage()),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => _showLogoutDialog(context),
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'My Prayers', icon: Icon(Icons.favorite)),
            Tab(text: 'My Testimonies', icon: Icon(Icons.auto_awesome)),
          ],
        ),
      ),
      body: Column(
        children: [
          // Profile header
          Container(
            padding: const EdgeInsets.all(16),
            child: Column(
              children: [
                CircleAvatar(
                  radius: 40,
                  child: Text(
                    widget.user.name[0].toUpperCase(),
                    style: Theme.of(context).textTheme.headlineMedium,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  widget.user.name,
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                Text(
                  widget.user.email,
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 12),
                BlocBuilder<SubscriptionBloc, SubscriptionState>(
                  builder: (context, state) {
                    if (state is SubscriptionLoaded && state.canPost) {
                      return Chip(
                        label: const Text('Subscribed'),
                        avatar: const Icon(Icons.stars, size: 16),
                        backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                      );
                    }
                    return Chip(
                      label: const Text('Free'),
                      avatar: const Icon(Icons.person, size: 16),
                    );
                  },
                ),
              ],
            ),
          ),
          const Divider(),

          // Tabs content
          Expanded(
            child: TabBarView(
              controller: _tabController,
              children: [
                _buildMyPrayers(),
                _buildMyTestimonies(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMyPrayers() {
    return BlocBuilder<PrayerBloc, PrayerState>(
      builder: (context, state) {
        if (state is MyPrayersLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (state is MyPrayersLoaded) {
          if (state.myPrayers.isEmpty) {
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
                  const Text('No prayers yet'),
                  const SizedBox(height: 8),
                  const Text('Post your first prayer request'),
                ],
              ),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: state.myPrayers.length,
            itemBuilder: (context, index) {
              final prayer = state.myPrayers[index];
              return Column(
                children: [
                  PrayerCard(
                    prayer: prayer,
                    onTapPraying: () {
                      context
                          .read<PrayerBloc>()
                          .add(TogglePrayingEvent(prayer.id));
                    },
                  ),
                  // Status badge
                  if (prayer.status != 'approved')
                    Padding(
                      padding: const EdgeInsets.only(bottom: 16),
                      child: Chip(
                        label: Text(_getStatusText(prayer.status)),
                        backgroundColor: _getStatusColor(context, prayer.status),
                      ),
                    ),
                ],
              );
            },
          );
        }

        if (state is PrayerError) {
          return Center(child: Text(state.message));
        }

        return const SizedBox();
      },
    );
  }

  Widget _buildMyTestimonies() {
    return BlocBuilder<TestimonyBloc, TestimonyState>(
      builder: (context, state) {
        if (state is MyTestimoniesLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (state is MyTestimoniesLoaded) {
          if (state.myTestimonies.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.auto_awesome_outlined,
                    size: 64,
                    color: Theme.of(context).colorScheme.primary.withOpacity(0.5),
                  ),
                  const SizedBox(height: 16),
                  const Text('No testimonies yet'),
                  const SizedBox(height: 8),
                  const Text('Share how God moved in your life'),
                ],
              ),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: state.myTestimonies.length,
            itemBuilder: (context, index) {
              final testimony = state.myTestimonies[index];
              return Column(
                children: [
                  TestimonyCard(
                    testimony: testimony,
                    onTapPraise: () {
                      context
                          .read<TestimonyBloc>()
                          .add(TogglePraiseEvent(testimony.id));
                    },
                  ),
                  // Status badge
                  if (testimony.status != 'approved')
                    Padding(
                      padding: const EdgeInsets.only(bottom: 16),
                      child: Chip(
                        label: Text(_getStatusText(testimony.status)),
                        backgroundColor: _getStatusColor(context, testimony.status),
                      ),
                    ),
                ],
              );
            },
          );
        }

        if (state is TestimonyError) {
          return Center(child: Text(state.message));
        }

        return const SizedBox();
      },
    );
  }

  String _getStatusText(String status) {
    switch (status) {
      case 'pending':
        return 'Under Review';
      case 'approved':
        return 'Published';
      case 'rejected':
        return 'Not Published';
      default:
        return status;
    }
  }

  Color _getStatusColor(BuildContext context, String status) {
    switch (status) {
      case 'pending':
        return Colors.orange.withOpacity(0.2);
      case 'approved':
        return Colors.green.withOpacity(0.2);
      case 'rejected':
        return Colors.red.withOpacity(0.2);
      default:
        return Theme.of(context).colorScheme.surfaceVariant;
    }
  }

  void _showLogoutDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Logout'),
        content: const Text('Are you sure you want to logout?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(dialogContext);
              context.read<AuthBloc>().add(LogoutEvent());
            },
            child: const Text('Logout'),
          ),
        ],
      ),
    );
  }
}