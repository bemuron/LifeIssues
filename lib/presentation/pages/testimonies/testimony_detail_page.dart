// lib/presentation/pages/testimonies/testimony_detail_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:intl/intl.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../../domain/entities/testimony.dart';
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/testimony/testimony_bloc.dart';
import '../../blocs/testimony/testimony_event.dart';
import '../../blocs/testimony/testimony_state.dart';
import '../auth/login_page.dart';
import '../prayers/prayer_detail_page.dart';

class TestimonyDetailPage extends StatelessWidget {
  final int testimonyId;

  const TestimonyDetailPage({
    Key? key,
    required this.testimonyId,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<TestimonyBloc>()
        ..add(LoadTestimonyByIdEvent(testimonyId)),
      child: const TestimonyDetailView(),
    );
  }
}

class TestimonyDetailView extends StatefulWidget {
  const TestimonyDetailView({Key? key}) : super(key: key);

  @override
  State<TestimonyDetailView> createState() => _TestimonyDetailViewState();
}

class _TestimonyDetailViewState extends State<TestimonyDetailView> {
  Testimony? _lastLoadedTestimony;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Testimony'),
      ),
      body: BlocConsumer<TestimonyBloc, TestimonyState>(
        listener: (context, state) {
          if (state is TestimonyPraiseToggled) {
            final msg = state.alreadyPraised
                ? 'You have already praised God for this testimony'
                : 'Praise God! Your praise has been recorded';
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(msg)),
            );
          }
        },
        builder: (context, state) {
          if (state is TestimonyDetailLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is TestimonyDetailLoaded) {
            _lastLoadedTestimony = state.testimony;
            return _TestimonyDetailBody(testimony: state.testimony);
          }

          // While toggling praise, keep showing the last known testimony detail
          if (state is TestimonyTogglingPraise && _lastLoadedTestimony != null) {
            return Stack(
              children: [
                _TestimonyDetailBody(testimony: _lastLoadedTestimony!),
                const Positioned.fill(
                  child: ColoredBox(
                    color: Colors.black12,
                    child: Center(child: CircularProgressIndicator()),
                  ),
                ),
              ],
            );
          }

          if (state is TestimonyError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 64, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(state.message),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Go Back'),
                  ),
                ],
              ),
            );
          }

          return const SizedBox();
        },
      ),
    );
  }
}

class _TestimonyDetailBody extends StatelessWidget {
  final Testimony testimony;

  const _TestimonyDetailBody({required this.testimony});

  /// Shows an auth-required dialog with login / register action.
  void _showAuthDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        icon: const Icon(Icons.lock_outline, size: 36),
        title: const Text('Sign in required'),
        content: const Text(
          'Only registered users can praise God for testimonies. '
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
                // Avatar — show profile image when available
                CircleAvatar(
                  radius: 22,
                  backgroundColor: cs.tertiaryContainer,
                  backgroundImage: (testimony.profileImageUrl != null &&
                          testimony.profileImageUrl!.isNotEmpty)
                      ? NetworkImage(testimony.profileImageUrl!)
                      : null,
                  child: (testimony.profileImageUrl != null &&
                          testimony.profileImageUrl!.isNotEmpty)
                      ? null
                      : Icon(
                          Icons.person,
                          size: 20,
                          color: cs.onTertiaryContainer,
                        ),
                ),
                const SizedBox(width: 12),

                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        testimony.posterName.isNotEmpty
                            ? testimony.posterName
                            : 'Anonymous',
                        style: tt.titleSmall?.copyWith(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        DateFormat('MMM d, yyyy').format(testimony.createdAt),
                        style: tt.bodySmall?.copyWith(
                          color: cs.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),

              ],
            ),
          ),

          const SizedBox(height: 12),

          // ── Category chip ────────────────────────────────────────────────
          if (testimony.category != null)
            Align(
              alignment: Alignment.centerLeft,
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
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
                      testimony.category!,
                      style: tt.labelSmall?.copyWith(
                        color: cs.onSecondaryContainer,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ),

          if (testimony.category != null) const SizedBox(height: 12),

          // ── Title ────────────────────────────────────────────────────────
          Text(
            testimony.title,
            style: tt.titleLarge?.copyWith(
              fontWeight: FontWeight.w700,
              height: 1.3,
            ),
          ),

          const SizedBox(height: 12),

          // ── Testimony body ───────────────────────────────────────────────
          _SectionCard(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 3,
                  constraints: const BoxConstraints(minHeight: 40),
                  decoration: BoxDecoration(
                    color: cs.tertiary.withOpacity(0.5),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    testimony.body,
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

          // ── Praise count ─────────────────────────────────────────────────
          Container(
            padding: const EdgeInsets.symmetric(vertical: 14),
            decoration: BoxDecoration(
              color: cs.surfaceContainerLow,
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                color: cs.outlineVariant.withOpacity(0.5),
              ),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.auto_awesome_rounded,
                    size: 18, color: Colors.amber.shade600),
                const SizedBox(width: 8),
                Text(
                  '${testimony.praiseCount} '
                      '${testimony.praiseCount == 1 ? 'person praised' : 'people praised'} '
                      'God',
                  style: tt.titleSmall?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 12),

          // ── Praise button ────────────────────────────────────────────────
          BlocBuilder<AuthBloc, AuthState>(
            builder: (ctx, authState) {
              if (testimony.hasPraised) {
                return OutlinedButton.icon(
                  onPressed: null,
                  icon: const Icon(Icons.check_circle_outline, size: 18),
                  label: const Text('You praised God'),
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
                      .read<TestimonyBloc>()
                      .add(TogglePraiseEvent(testimony.id));
                },
                icon: const Icon(Icons.auto_awesome_outlined, size: 18),
                label: const Text('Praise God'),
                style: FilledButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
              );
            },
          ),

          // ── Linked prayer ────────────────────────────────────────────────
          if (testimony.linkedPrayer != null) ...[
            const SizedBox(height: 24),
            Row(
              children: [
                Text(
                  'ANSWERS THIS PRAYER',
                  style: tt.labelSmall?.copyWith(
                    color: cs.onSurfaceVariant,
                    letterSpacing: 0.8,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Divider(
                    color: cs.outlineVariant.withOpacity(0.5),
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
                    builder: (_) => PrayerDetailPage(
                      prayerId: testimony.linkedPrayer!.id,
                    ),
                  ),
                );
              },
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Prayer header
                  Row(
                    children: [
                      Container(
                        width: 32,
                        height: 32,
                        decoration: BoxDecoration(
                          color: cs.primaryContainer,
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Icon(Icons.favorite_outline,
                            size: 16, color: cs.onPrimaryContainer),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          testimony.linkedPrayer!.isAnonymous
                              ? 'Anonymous prayer'
                              : 'Prayer request',
                          style: tt.labelLarge?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: cs.surfaceContainerHighest,
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Icon(Icons.favorite_rounded,
                                size: 11, color: Colors.red.shade400),
                            const SizedBox(width: 4),
                            Text(
                              '${testimony.linkedPrayer!.prayCount}',
                              style: tt.labelSmall?.copyWith(
                                color: cs.onSurfaceVariant,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 10),

                  // Prayer excerpt with left accent
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        width: 3,
                        constraints: const BoxConstraints(minHeight: 32),
                        decoration: BoxDecoration(
                          color: cs.primary.withOpacity(0.4),
                          borderRadius: BorderRadius.circular(2),
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Text(
                          testimony.linkedPrayer!.bodyExcerpt,
                          style: tt.bodySmall?.copyWith(
                            color: cs.onSurfaceVariant,
                            height: 1.6,
                          ),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 8),

                  // View prayer link
                  Align(
                    alignment: Alignment.centerRight,
                    child: TextButton.icon(
                      onPressed: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => PrayerDetailPage(
                              prayerId: testimony.linkedPrayer!.id,
                            ),
                          ),
                        );
                      },
                      icon: const Icon(Icons.arrow_forward, size: 14),
                      label: const Text('View Prayer'),
                      style: TextButton.styleFrom(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 4, vertical: 4),
                        visualDensity: VisualDensity.compact,
                      ),
                    ),
                  ),
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

// ── Shared card wrapper ──────────────────────────────────────────────────────

class _SectionCard extends StatelessWidget {
  final Widget child;
  final VoidCallback? onTap;

  const _SectionCard({required this.child, this.onTap});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return Material(
      color: cs.surface,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: cs.outlineVariant.withOpacity(0.4),
            ),
          ),
          child: child,
        ),
      ),
    );
  }
}
