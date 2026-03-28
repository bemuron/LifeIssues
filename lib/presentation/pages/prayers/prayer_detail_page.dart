// lib/presentation/pages/prayers/prayer_detail_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../domain/entities/prayer.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/prayer/prayer_bloc.dart';
import '../../blocs/prayer/prayer_event.dart';
import '../../blocs/prayer/prayer_state.dart';
import '../auth/login_page.dart';
import '../testimonies/testimony_detail_page.dart';
import 'edit_prayer_page.dart';

class PrayerDetailPage extends StatelessWidget {
  final int prayerId;
  final Prayer? initialPrayer;

  const PrayerDetailPage({
    Key? key,
    required this.prayerId,
    this.initialPrayer,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) {
        final bloc = di.sl<PrayerBloc>();
        if (initialPrayer != null) {
          bloc.add(LoadPrayerByIdEvent(prayerId, preloaded: initialPrayer));
        } else {
          bloc.add(LoadPrayerByIdEvent(prayerId));
        }
        return bloc;
      },
      child: const PrayerDetailView(),
    );
  }
}

class PrayerDetailView extends StatefulWidget {
  const PrayerDetailView({Key? key}) : super(key: key);

  @override
  State<PrayerDetailView> createState() => _PrayerDetailViewState();
}

class _PrayerDetailViewState extends State<PrayerDetailView> {
  Prayer? _lastLoadedPrayer;

  /// Returns true when the current auth user owns this prayer.
  bool _isOwner(AuthState authState, Prayer prayer) {
    if (authState is! Authenticated) return false;
    return authState.user.id == prayer.userId;
  }

  /// Whether the prayer can still be edited per community rules.
  bool _canEdit(Prayer prayer) {
    if (prayer.status == 'pending') return true;
    if (prayer.status == 'approved' && prayer.prayCount == 0) return true;
    return false;
  }

  void _showDeleteDialog(BuildContext context, Prayer prayer) {
    final prayCount = prayer.prayCount;
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Delete Prayer?'),
        content: Text(
          prayCount > 0
              ? '$prayCount ${prayCount == 1 ? 'person has' : 'people have'} prayed for this. '
                  'Deleting will remove it from their activity too.\n\nThis cannot be undone.'
              : 'Are you sure you want to delete this prayer?\nThis cannot be undone.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            onPressed: () {
              Navigator.pop(ctx);
              context.read<PrayerBloc>().add(DeletePrayerEvent(prayer.id));
            },
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  void _showCannotEditDialog(BuildContext context, Prayer prayer) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Cannot Edit'),
        content: Text(
          prayer.prayCount > 0
              ? 'This prayer has received responses. Editing it would be misleading to the '
                  '${prayer.prayCount} ${prayer.prayCount == 1 ? 'person' : 'people'} already praying.'
              : 'This prayer cannot be edited at this stage.',
        ),
        actions: [
          FilledButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authState = context.watch<AuthBloc>().state;

    return BlocConsumer<PrayerBloc, PrayerState>(
      listener: (context, state) {
        if (state is PrayerPrayingToggled) {
          final msg = state.alreadyPrayed
              ? 'You are already praying for this request'
              : 'You are now praying for this request';
          ScaffoldMessenger.of(context)
              .showSnackBar(SnackBar(content: Text(msg)));
        }
        if (state is PrayerEdited) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('Prayer updated and resubmitted for review'),
          ));
        }
        if (state is PrayerDeleted) {
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('Your prayer has been deleted'),
          ));
          Navigator.pop(context);
        }
        if (state is PrayerError) {
          ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            content: Text(state.message),
            backgroundColor: Theme.of(context).colorScheme.error,
          ));
        }
      },
      builder: (context, state) {
        if (state is PrayerDetailLoading) {
          return Scaffold(
            appBar: AppBar(title: const Text('Prayer Request')),
            body: const Center(child: CircularProgressIndicator()),
          );
        }

        if (state is PrayerDetailLoaded) {
          _lastLoadedPrayer = state.prayer;
        }

        final prayer = _lastLoadedPrayer;

        if (state is PrayerTogglingPraying && prayer != null) {
          return Scaffold(
            appBar: AppBar(title: const Text('Prayer Request')),
            body: Stack(
              children: [
                _PrayerDetailBody(prayer: prayer),
                const Positioned.fill(
                  child: ColoredBox(
                    color: Colors.black12,
                    child: Center(child: CircularProgressIndicator()),
                  ),
                ),
              ],
            ),
          );
        }

        if (state is PrayerDeleting) {
          return Scaffold(
            appBar: AppBar(title: const Text('Prayer Request')),
            body: const Center(child: CircularProgressIndicator()),
          );
        }

        if (prayer != null) {
          final isOwner = _isOwner(authState, prayer);
          return Scaffold(
            appBar: AppBar(
              title: const Text('Prayer Request'),
              actions: [
                if (isOwner)
                  PopupMenuButton<String>(
                    icon: const Icon(Icons.more_vert),
                    onSelected: (value) {
                      if (value == 'edit') {
                        if (_canEdit(prayer)) {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => EditPrayerPage(prayer: prayer),
                            ),
                          );
                        } else {
                          _showCannotEditDialog(context, prayer);
                        }
                      } else if (value == 'delete') {
                        _showDeleteDialog(context, prayer);
                      }
                    },
                    itemBuilder: (_) => [
                      PopupMenuItem(
                        value: 'edit',
                        child: Row(children: [
                          Icon(
                            Icons.edit_outlined,
                            size: 18,
                            color: _canEdit(prayer) ? null : Colors.grey,
                          ),
                          const SizedBox(width: 10),
                          Text(
                            'Edit',
                            style: _canEdit(prayer)
                                ? null
                                : const TextStyle(color: Colors.grey),
                          ),
                        ]),
                      ),
                      const PopupMenuItem(
                        value: 'delete',
                        child: Row(children: [
                          Icon(Icons.delete_outline,
                              size: 18, color: Colors.red),
                          SizedBox(width: 10),
                          Text('Delete',
                              style: TextStyle(color: Colors.red)),
                        ]),
                      ),
                    ],
                  ),
              ],
            ),
            body: _PrayerDetailBody(prayer: prayer),
          );
        }

        if (state is PrayerError) {
          return Scaffold(
            appBar: AppBar(title: const Text('Prayer Request')),
            body: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline,
                      size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(state.message),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Go Back'),
                  ),
                ],
              ),
            ),
          );
        }

        return Scaffold(
          appBar: AppBar(title: const Text('Prayer Request')),
          body: const SizedBox(),
        );
      },
    );
  }
}

class _PrayerDetailBody extends StatelessWidget {
  final Prayer prayer;

  const _PrayerDetailBody({required this.prayer});

  void _showAuthDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        icon: const Icon(Icons.lock_outline, size: 36),
        title: const Text('Sign in required'),
        content: const Text(
          'Only registered users can pray for requests. '
          'Sign in or create a free account to continue.',
          textAlign: TextAlign.center,
        ),
        actionsAlignment: MainAxisAlignment.center,
        actions: [
          OutlinedButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(ctx);
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const LoginPage()),
              );
            },
            child: const Text('Sign In / Register'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // ── Poster card ──────────────────────────────────────────────────
          _SectionCard(
            child: Row(
              children: [
                CircleAvatar(
                  radius: 22,
                  backgroundColor: cs.primaryContainer,
                  backgroundImage: (!prayer.isAnonymous &&
                          prayer.profileImageUrl != null &&
                          prayer.profileImageUrl!.isNotEmpty)
                      ? NetworkImage(prayer.profileImageUrl!)
                      : null,
                  child: (!prayer.isAnonymous &&
                          prayer.profileImageUrl != null &&
                          prayer.profileImageUrl!.isNotEmpty)
                      ? null
                      : Icon(
                          prayer.isAnonymous
                              ? Icons.person_off
                              : Icons.person,
                          size: 20,
                          color: cs.onPrimaryContainer,
                        ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        prayer.isAnonymous
                            ? 'Anonymous'
                            : (prayer.posterName ?? 'Unknown'),
                        style: tt.titleSmall
                            ?.copyWith(fontWeight: FontWeight.w600),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        DateFormat('MMM d, yyyy').format(prayer.createdAt),
                        style: tt.bodySmall
                            ?.copyWith(color: cs.onSurfaceVariant),
                      ),
                    ],
                  ),
                ),
                // Status badges
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    if (prayer.isEdited)
                      _StatusBadge(
                        label: 'Edited',
                        color: cs.tertiary,
                        icon: Icons.edit_outlined,
                      ),
                    if (prayer.status == 'pending') ...[
                      if (prayer.isEdited) const SizedBox(width: 6),
                      _StatusBadge(
                        label: 'Under Review',
                        color: Colors.orange,
                        icon: Icons.hourglass_empty,
                      ),
                    ],
                    if (prayer.answered) ...[
                      if (prayer.isEdited || prayer.status == 'pending')
                        const SizedBox(width: 6),
                      _StatusBadge(
                        label: 'Answered',
                        color: Colors.green,
                        icon: Icons.check_circle_outline,
                      ),
                    ],
                  ],
                ),
              ],
            ),
          ),

          const SizedBox(height: 12),

          // ── Category chip ────────────────────────────────────────────────
          if (prayer.category != null)
            Align(
              alignment: Alignment.centerLeft,
              child: Container(
                padding: const EdgeInsets.symmetric(
                    horizontal: 10, vertical: 5),
                decoration: BoxDecoration(
                  color: cs.secondaryContainer,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(Icons.category_outlined,
                        size: 13, color: cs.onSecondaryContainer),
                    const SizedBox(width: 5),
                    Text(
                      prayer.category!,
                      style: tt.labelSmall?.copyWith(
                        color: cs.onSecondaryContainer,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ),

          if (prayer.category != null) const SizedBox(height: 12),

          // ── Prayer body ──────────────────────────────────────────────────
          _SectionCard(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 3,
                  constraints: const BoxConstraints(minHeight: 40),
                  decoration: BoxDecoration(
                    color: cs.primary.withValues(alpha: 0.5),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    prayer.body,
                    style: tt.bodyMedium?.copyWith(
                      height: 1.7,
                      color: cs.onSurface,
                    ),
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 12),

          // ── Pray count ───────────────────────────────────────────────────
          Container(
            padding: const EdgeInsets.symmetric(vertical: 14),
            decoration: BoxDecoration(
              color: cs.surfaceContainerLow,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                  color: cs.outlineVariant.withValues(alpha: 0.5)),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.favorite_rounded,
                    size: 18, color: Colors.red.shade400),
                const SizedBox(width: 8),
                Text(
                  '${prayer.prayCount} '
                  '${prayer.prayCount == 1 ? 'person is' : 'people are'} '
                  'praying',
                  style: tt.titleSmall
                      ?.copyWith(fontWeight: FontWeight.w600),
                ),
              ],
            ),
          ),

          const SizedBox(height: 12),

          // ── Praying button ───────────────────────────────────────────────
          BlocBuilder<AuthBloc, AuthState>(
            builder: (ctx, authState) {
              if (prayer.hasPrayed) {
                return OutlinedButton.icon(
                  onPressed: null,
                  icon: const Icon(Icons.check_circle_outline, size: 18),
                  label: const Text('You are praying'),
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                );
              }

              return FilledButton.icon(
                onPressed: () {
                  if (authState is! Authenticated) {
                    _showAuthDialog(ctx);
                    return;
                  }
                  ctx
                      .read<PrayerBloc>()
                      .add(TogglePrayingEvent(prayer.id));
                },
                icon: const Icon(Icons.favorite_outline, size: 18),
                label: const Text("I'm praying"),
                style: FilledButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
              );
            },
          ),

          // ── Linked testimony ─────────────────────────────────────────────
          if (prayer.linkedTestimony != null) ...[
            const SizedBox(height: 24),
            Row(
              children: [
                Text(
                  'LINKED TESTIMONY',
                  style: tt.labelSmall?.copyWith(
                    color: cs.onSurfaceVariant,
                    letterSpacing: 0.8,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Divider(
                    color: cs.outlineVariant.withValues(alpha: 0.5),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            _SectionCard(
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (_) => TestimonyDetailPage(
                      testimonyId: prayer.linkedTestimony!.id,
                    ),
                  ),
                );
              },
              child: Row(
                children: [
                  Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: cs.tertiaryContainer,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Icon(Icons.auto_awesome,
                        size: 18, color: cs.onTertiaryContainer),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          prayer.linkedTestimony!.title,
                          style: tt.bodyMedium
                              ?.copyWith(fontWeight: FontWeight.w600),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          '${prayer.linkedTestimony!.praiseCount} people praised God',
                          style: tt.bodySmall
                              ?.copyWith(color: cs.onSurfaceVariant),
                        ),
                      ],
                    ),
                  ),
                  Icon(Icons.chevron_right,
                      size: 20, color: cs.onSurfaceVariant),
                ],
              ),
            ),
          ],

          const SizedBox(height: 24),
        ],
      ),
    );
  }
}

// ── Status badge ─────────────────────────────────────────────────────────────

class _StatusBadge extends StatelessWidget {
  final String label;
  final Color color;
  final IconData icon;

  const _StatusBadge(
      {required this.label, required this.color, required this.icon});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.35)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 12, color: color),
          const SizedBox(width: 4),
          Text(
            label,
            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                  color: color,
                  fontWeight: FontWeight.w600,
                ),
          ),
        ],
      ),
    );
  }
}

// ── Shared card wrapper ───────────────────────────────────────────────────────

class _SectionCard extends StatelessWidget {
  final Widget child;
  final VoidCallback? onTap;

  const _SectionCard({required this.child, this.onTap});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return Material(
      color: Theme.of(context).colorScheme.surface,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: cs.outlineVariant.withValues(alpha: 0.4),
            ),
          ),
          child: child,
        ),
      ),
    );
  }
}
