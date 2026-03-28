// lib/presentation/pages/profile/profile_page.dart

import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
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
import '../../widgets/ad_banner_widget.dart';
import '../../widgets/prayer_card.dart';
import '../prayers/prayer_detail_page.dart';
import '../prayers/edit_prayer_page.dart';
import '../../widgets/testimony_card.dart';
import '../testimonies/testimony_detail_page.dart';
import '../testimonies/edit_testimony_page.dart';
import '../auth/login_page.dart';
import '../settings/settings_page.dart';
import '../prayers/prayer_submission_page.dart';
import '../testimonies/testimony_submission_page.dart';
import '../subscription/subscription_page.dart';
import 'edit_profile_page.dart';

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
    const showAd = true;

    final content = Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            width: 96,
            height: 96,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: Theme.of(context).colorScheme.primaryContainer,
            ),
            child: Icon(
              Icons.person_outline,
              size: 52,
              color: Theme.of(context).colorScheme.onPrimaryContainer,
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'Welcome!',
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Login to view your profile,\nprayers and testimonies',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
              color: Theme.of(context).colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 32),
          FilledButton.icon(
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const LoginPage()),
              );
            },
            icon: const Icon(Icons.login),
            label: const Text('Login to Continue'),
          ),
        ],
      ),
    );

    return Scaffold(
      appBar: AppBar(
        title: const Text('Profile'),
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
        ],
      ),
      body: Stack(
        children: [
          Positioned.fill(
            child: Padding(
              padding: EdgeInsets.only(bottom: showAd ? 70 : 0),
              child: content,
            ),
          ),
          if (showAd)
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
    final colorScheme = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('My Profile'),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit_outlined),
            tooltip: 'Edit Profile',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => EditProfilePage(user: widget.user),
                ),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.settings_outlined),
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
      ),
      body: Column(
        children: [
          // Profile header card
          Container(
            width: double.infinity,
            margin: const EdgeInsets.fromLTRB(16, 12, 16, 0),
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: colorScheme.surfaceContainerHigh,
              borderRadius: BorderRadius.circular(16),
            ),
            child: Column(
              children: [
                CircleAvatar(
                  radius: 36,
                  backgroundColor: colorScheme.primaryContainer,
                  backgroundImage: widget.user.profileImageUrl != null &&
                          widget.user.profileImageUrl!.isNotEmpty
                      ? CachedNetworkImageProvider(
                              widget.user.profileImageUrl!)
                          as ImageProvider
                      : null,
                  child: widget.user.profileImageUrl == null ||
                          widget.user.profileImageUrl!.isEmpty
                      ? Text(
                          widget.user.name[0].toUpperCase(),
                          style: Theme.of(context)
                              .textTheme
                              .headlineMedium
                              ?.copyWith(
                                color: colorScheme.onPrimaryContainer,
                                fontWeight: FontWeight.bold,
                              ),
                        )
                      : null,
                ),
                const SizedBox(height: 12),
                Text(
                  widget.user.name,
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  widget.user.email,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                    color: colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(height: 12),
                BlocBuilder<SubscriptionBloc, SubscriptionState>(
                  builder: (context, state) {
                    final isSubscribed =
                        state is SubscriptionLoaded && state.canPost;

                    if (isSubscribed) {
                      return Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 12, vertical: 6),
                        decoration: BoxDecoration(
                          color: colorScheme.primaryContainer,
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Icons.stars_rounded,
                                size: 16,
                                color: colorScheme.onPrimaryContainer),
                            const SizedBox(width: 6),
                            Text(
                              'Subscribed',
                              style: Theme.of(context)
                                  .textTheme
                                  .labelMedium
                                  ?.copyWith(
                                    color: colorScheme.onPrimaryContainer,
                                    fontWeight: FontWeight.w600,
                                  ),
                            ),
                          ],
                        ),
                      );
                    }

                    // Free plan — show badge + upgrade button side by side
                    return Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 12, vertical: 6),
                          decoration: BoxDecoration(
                            color: colorScheme.surfaceContainerHighest,
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.person_outline,
                                  size: 16,
                                  color: colorScheme.onSurfaceVariant),
                              const SizedBox(width: 6),
                              Text(
                                'Free Plan',
                                style: Theme.of(context)
                                    .textTheme
                                    .labelMedium
                                    ?.copyWith(
                                      color: colorScheme.onSurfaceVariant,
                                      fontWeight: FontWeight.w600,
                                    ),
                              ),
                            ],
                          ),
                        ),
                        const SizedBox(width: 8),
                        ElevatedButton.icon(
                          onPressed: () => Navigator.push(
                            context,
                            MaterialPageRoute(
                                builder: (_) => const SubscriptionPage()),
                          ),
                          icon: const Icon(Icons.upgrade, size: 16),
                          label: const Text('Upgrade'),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.amber.shade700,
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(
                                horizontal: 14, vertical: 6),
                            minimumSize: Size.zero,
                            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                            visualDensity: VisualDensity.compact,
                            elevation: 0,
                          ),
                        ),
                      ],
                    );
                  },
                ),
              ],
            ),
          ),

          const SizedBox(height: 12),

          // Tab bar
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            decoration: BoxDecoration(
              color: colorScheme.surfaceContainerHigh,
              borderRadius: BorderRadius.circular(12),
            ),
            child: TabBar(
              controller: _tabController,
              indicator: BoxDecoration(
                color: colorScheme.primary,
                borderRadius: BorderRadius.circular(10),
              ),
              indicatorSize: TabBarIndicatorSize.tab,
              dividerColor: Colors.transparent,
              labelColor: colorScheme.onPrimary,
              unselectedLabelColor: colorScheme.onSurfaceVariant,
              labelStyle: Theme.of(context).textTheme.labelLarge?.copyWith(
                fontWeight: FontWeight.w600,
              ),
              unselectedLabelStyle: Theme.of(context).textTheme.labelLarge,
              tabs: const [
                Tab(text: 'My Prayers', icon: FaIcon(FontAwesomeIcons.personPraying, size: 18)),
                Tab(text: 'My Testimonies', icon: Icon(Icons.auto_awesome, size: 18)),
              ],
            ),
          ),

          const SizedBox(height: 8),

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
    return BlocConsumer<PrayerBloc, PrayerState>(
      listener: (context, state) {
        if (state is PrayerDeleted) {
          context.read<PrayerBloc>().add(LoadMyPrayersEvent());
        }
        if (state is PrayerError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.message),
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
          );
        }
      },
      builder: (context, state) {
        if (state is MyPrayersLoading || state is PrayerDeleting) {
          return const Center(child: CircularProgressIndicator());
        }

        if (state is MyPrayersLoaded) {
          if (state.myPrayers.isEmpty) {
            return RefreshIndicator(
              onRefresh: () async =>
                  context.read<PrayerBloc>().add(LoadMyPrayersEvent()),
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                child: _buildEmptyPrayers(context),
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: () async =>
                context.read<PrayerBloc>().add(LoadMyPrayersEvent()),
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: state.myPrayers.length,
              itemBuilder: (context, index) {
                final prayer = state.myPrayers[index];
                // For the user's own prayers the API omits the nested user
                // object, so posterName and profileImageUrl arrive as null.
                // Fill them in from the authenticated user's profile.
                final displayPrayer = prayer.isAnonymous
                    ? prayer
                    : prayer.copyWith(
                        posterName: (prayer.posterName != null &&
                                prayer.posterName!.isNotEmpty)
                            ? prayer.posterName
                            : widget.user.name as String,
                        profileImageUrl: prayer.profileImageUrl ??
                            widget.user.profileImageUrl as String?,
                      );
                final canEdit = prayer.status == 'pending' ||
                    (prayer.status == 'approved' && prayer.prayCount == 0);

                return Stack(
                  children: [
                    Column(
                      children: [
                        PrayerCard(
                          prayer: displayPrayer,
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => PrayerDetailPage(
                                  prayerId: displayPrayer.id,
                                  initialPrayer: displayPrayer,
                                ),
                              ),
                            ).then((_) => context
                                .read<PrayerBloc>()
                                .add(LoadMyPrayersEvent()));
                          },
                          onTapPraying: () {
                            context
                                .read<PrayerBloc>()
                                .add(TogglePrayingEvent(prayer.id));
                          },
                        ),
                        if (prayer.status != 'approved')
                          Padding(
                            padding: const EdgeInsets.only(bottom: 16),
                            child: Chip(
                              label: Text(_getStatusText(prayer.status)),
                              backgroundColor:
                                  _getStatusColor(context, prayer.status),
                            ),
                          ),
                      ],
                    ),
                    Positioned(
                      top: 4,
                      right: 4,
                      child: PopupMenuButton<String>(
                        icon: const Icon(Icons.more_vert, size: 20),
                        onSelected: (value) {
                          if (value == 'edit') {
                            if (!canEdit) {
                              _showCannotEditPrayerDialog(context, prayer.prayCount);
                              return;
                            }
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => EditPrayerPage(prayer: prayer),
                              ),
                            ).then((_) => context
                                .read<PrayerBloc>()
                                .add(LoadMyPrayersEvent()));
                          } else if (value == 'delete') {
                            _showDeletePrayerDialog(context, prayer.id, prayer.prayCount);
                          }
                        },
                        itemBuilder: (_) => [
                          PopupMenuItem(
                            value: 'edit',
                            child: Row(
                              children: [
                                Icon(
                                  Icons.edit_outlined,
                                  size: 18,
                                  color: canEdit ? null : Theme.of(context).colorScheme.onSurfaceVariant,
                                ),
                                const SizedBox(width: 8),
                                Text(
                                  'Edit',
                                  style: TextStyle(
                                    color: canEdit ? null : Theme.of(context).colorScheme.onSurfaceVariant,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          PopupMenuItem(
                            value: 'delete',
                            child: Row(
                              children: [
                                Icon(Icons.delete_outline,
                                    size: 18,
                                    color: Theme.of(context).colorScheme.error),
                                const SizedBox(width: 8),
                                Text(
                                  'Delete',
                                  style: TextStyle(
                                      color: Theme.of(context).colorScheme.error),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                );
              },
            ),
          );
        }

        if (state is PrayerError) {
          return _buildErrorState(
            context,
            message: state.message,
            onRetry: () =>
                context.read<PrayerBloc>().add(LoadMyPrayersEvent()),
          );
        }

        return const SizedBox();
      },
    );
  }

  void _showCannotEditPrayerDialog(BuildContext context, int prayCount) {
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Cannot Edit'),
        content: Text(
          prayCount > 0
              ? 'This prayer has $prayCount ${prayCount == 1 ? 'person' : 'people'} praying for it and cannot be edited.'
              : 'This prayer cannot be edited in its current state.',
        ),
        actions: [
          FilledButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  void _showDeletePrayerDialog(BuildContext context, int prayerId, int prayCount) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Delete Prayer'),
        content: Text(
          prayCount > 0
              ? 'This prayer has $prayCount ${prayCount == 1 ? 'person' : 'people'} praying for it. Are you sure you want to delete it?'
              : 'Are you sure you want to delete this prayer?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            onPressed: () {
              Navigator.pop(dialogContext);
              context.read<PrayerBloc>().add(DeletePrayerEvent(prayerId));
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyPrayers(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 80,
              height: 80,
              alignment: Alignment.center,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: colorScheme.primaryContainer,
              ),
              child: FaIcon(
                FontAwesomeIcons.personPraying,
                size: 40,
                color: colorScheme.onPrimaryContainer,
              ),
              //FaIcon(FontAwesomeIcons.personPraying, size: 18)
            ),
            const SizedBox(height: 20),
            Text(
              'No Prayers Yet',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Share your heart with the community.\nYour prayer request will be lifted up by others.',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 24),
            BlocBuilder<SubscriptionBloc, SubscriptionState>(
              builder: (context, subState) {
                final canPost = subState is SubscriptionLoaded && subState.canPost;
                return OutlinedButton.icon(
                  onPressed: () {
                    if (!canPost) {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (_) => const SubscriptionPage()),
                      );
                    } else {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (_) => const PrayerSubmissionPage()),
                      );
                    }
                  },
                  icon: const Icon(Icons.add),
                  label: Text(canPost ? 'Post a Prayer' : 'Subscribe to Post'),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildMyTestimonies() {
    return BlocConsumer<TestimonyBloc, TestimonyState>(
      listener: (context, state) {
        if (state is TestimonyDeleted) {
          context.read<TestimonyBloc>().add(LoadMyTestimoniesEvent());
        }
        if (state is TestimonyError) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(state.message),
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
          );
        }
      },
      builder: (context, state) {
        if (state is MyTestimoniesLoading || state is TestimonyDeleting) {
          return const Center(child: CircularProgressIndicator());
        }

        if (state is MyTestimoniesLoaded) {
          if (state.myTestimonies.isEmpty) {
            return RefreshIndicator(
              onRefresh: () async =>
                  context.read<TestimonyBloc>().add(LoadMyTestimoniesEvent()),
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                child: _buildEmptyTestimonies(context),
              ),
            );
          }

          return RefreshIndicator(
            onRefresh: () async =>
                context.read<TestimonyBloc>().add(LoadMyTestimoniesEvent()),
            child: ListView.builder(
              padding: const EdgeInsets.all(16),
              itemCount: state.myTestimonies.length,
              itemBuilder: (context, index) {
                final testimony = state.myTestimonies[index];
                // Same as prayers — fill in poster info from logged-in user.
                final displayTestimony = testimony.copyWith(
                  posterName: testimony.posterName.isNotEmpty
                      ? testimony.posterName
                      : widget.user.name as String,
                  profileImageUrl: testimony.profileImageUrl ??
                      widget.user.profileImageUrl as String?,
                );
                final canEdit = testimony.status == 'pending' ||
                    (testimony.status == 'approved' &&
                        testimony.praiseCount == 0 &&
                        testimony.linkedPrayer == null);

                return Stack(
                  children: [
                    Column(
                      children: [
                        TestimonyCard(
                          testimony: displayTestimony,
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) => TestimonyDetailPage(
                                  testimonyId: displayTestimony.id,
                                ),
                              ),
                            ).then((_) => context
                                .read<TestimonyBloc>()
                                .add(LoadMyTestimoniesEvent()));
                          },
                          onTapPraise: () {
                            context
                                .read<TestimonyBloc>()
                                .add(TogglePraiseEvent(testimony.id));
                          },
                        ),
                        if (testimony.status != 'approved')
                          Padding(
                            padding: const EdgeInsets.only(bottom: 16),
                            child: Chip(
                              label: Text(_getStatusText(testimony.status)),
                              backgroundColor:
                                  _getStatusColor(context, testimony.status),
                            ),
                          ),
                      ],
                    ),
                    Positioned(
                      top: 4,
                      right: 4,
                      child: PopupMenuButton<String>(
                        icon: const Icon(Icons.more_vert, size: 20),
                        onSelected: (value) {
                          if (value == 'edit') {
                            if (!canEdit) {
                              _showCannotEditTestimonyDialog(
                                  context, testimony.praiseCount, testimony.linkedPrayer != null);
                              return;
                            }
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) =>
                                    EditTestimonyPage(testimony: testimony),
                              ),
                            ).then((_) => context
                                .read<TestimonyBloc>()
                                .add(LoadMyTestimoniesEvent()));
                          } else if (value == 'delete') {
                            _showDeleteTestimonyDialog(
                                context, testimony.id, testimony.linkedPrayer != null);
                          }
                        },
                        itemBuilder: (_) => [
                          PopupMenuItem(
                            value: 'edit',
                            child: Row(
                              children: [
                                Icon(
                                  Icons.edit_outlined,
                                  size: 18,
                                  color: canEdit ? null : Theme.of(context).colorScheme.onSurfaceVariant,
                                ),
                                const SizedBox(width: 8),
                                Text(
                                  'Edit',
                                  style: TextStyle(
                                    color: canEdit ? null : Theme.of(context).colorScheme.onSurfaceVariant,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          PopupMenuItem(
                            value: 'delete',
                            child: Row(
                              children: [
                                Icon(Icons.delete_outline,
                                    size: 18,
                                    color: Theme.of(context).colorScheme.error),
                                const SizedBox(width: 8),
                                Text(
                                  'Delete',
                                  style: TextStyle(
                                      color: Theme.of(context).colorScheme.error),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                );
              },
            ),
          );
        }

        if (state is TestimonyError) {
          return _buildErrorState(
            context,
            message: state.message,
            onRetry: () =>
                context.read<TestimonyBloc>().add(LoadMyTestimoniesEvent()),
          );
        }

        return const SizedBox();
      },
    );
  }

  void _showCannotEditTestimonyDialog(
      BuildContext context, int praiseCount, bool hasLinkedPrayer) {
    final reason = hasLinkedPrayer
        ? 'This testimony is linked to an answered prayer and cannot be edited.'
        : 'This testimony has $praiseCount ${praiseCount == 1 ? 'praise' : 'praises'} and cannot be edited.';
    showDialog(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('Cannot Edit'),
        content: Text(reason),
        actions: [
          FilledButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  void _showDeleteTestimonyDialog(
      BuildContext context, int testimonyId, bool hasLinkedPrayer) {
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Delete Testimony'),
        content: Text(
          hasLinkedPrayer
              ? 'This testimony is linked to a prayer. Deleting it will unlink it from that prayer. Are you sure?'
              : 'Are you sure you want to delete this testimony?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            onPressed: () {
              Navigator.pop(dialogContext);
              context
                  .read<TestimonyBloc>()
                  .add(DeleteTestimonyEvent(testimonyId));
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(
    BuildContext context, {
    required String message,
    required VoidCallback onRetry,
  }) {
    final colorScheme = Theme.of(context).colorScheme;
    final isOffline = message.toLowerCase().contains('internet') ||
        message.toLowerCase().contains('network') ||
        message.toLowerCase().contains('connection') ||
        message.toLowerCase().contains('offline');

    return RefreshIndicator(
      onRefresh: () async => onRetry(),
      child: SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const SizedBox(height: 48),
              Icon(
                isOffline ? Icons.wifi_off_rounded : Icons.error_outline_rounded,
                size: 64,
                color: colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
              ),
              const SizedBox(height: 16),
              Text(
                isOffline ? 'You\'re Offline' : 'Something Went Wrong',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 8),
              Text(
                isOffline
                    ? 'Check your internet connection\nand pull down to refresh.'
                    : message,
                textAlign: TextAlign.center,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: colorScheme.onSurfaceVariant,
                    ),
              ),
              const SizedBox(height: 24),
              OutlinedButton.icon(
                onPressed: onRetry,
                icon: const Icon(Icons.refresh),
                label: const Text('Try Again'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyTestimonies(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 80,
              height: 80,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: colorScheme.secondaryContainer,
              ),
              child: Icon(
                Icons.auto_awesome_outlined,
                size: 40,
                color: colorScheme.onSecondaryContainer,
              ),
            ),
            const SizedBox(height: 20),
            Text(
              'No Testimonies Yet',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'Has God done something amazing in your life?\nShare your testimony and inspire others!',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 24),
            BlocBuilder<SubscriptionBloc, SubscriptionState>(
              builder: (context, subState) {
                final canPost = subState is SubscriptionLoaded && subState.canPost;
                return OutlinedButton.icon(
                  onPressed: () {
                    if (!canPost) {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (_) => const SubscriptionPage()),
                      );
                    } else {
                      Navigator.push(
                        context,
                        MaterialPageRoute(builder: (_) => const TestimonySubmissionPage()),
                      );
                    }
                  },
                  icon: const Icon(Icons.add),
                  label: Text(canPost ? 'Share a Testimony' : 'Subscribe to Post'),
                );
              },
            ),
          ],
        ),
      ),
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
        return Colors.orange.withValues(alpha: 0.2);
      case 'approved':
        return Colors.green.withValues(alpha: 0.2);
      case 'rejected':
        return Colors.red.withValues(alpha: 0.2);
      default:
        return Theme.of(context).colorScheme.surfaceContainerHighest;
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
