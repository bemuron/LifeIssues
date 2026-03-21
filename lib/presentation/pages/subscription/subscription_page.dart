// lib/presentation/pages/subscription/subscription_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_event.dart';
import '../../blocs/subscription/subscription_state.dart';

class SubscriptionPage extends StatelessWidget {
  const SubscriptionPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) => di.sl<SubscriptionBloc>()..add(LoadSubscriptionStatusEvent()),
      child: const _SubscriptionView(),
    );
  }
}

class _SubscriptionView extends StatelessWidget {
  const _SubscriptionView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Premium')),
      body: BlocBuilder<SubscriptionBloc, SubscriptionState>(
        builder: (context, state) {
          if (state is SubscriptionLoading) {
            return const Center(child: CircularProgressIndicator());
          }
          if (state is SubscriptionLoaded && state.canPost) {
            return _ActiveSubscriptionView(state: state);
          }
          return const _PlansView();
        },
      ),
    );
  }
}

// ─── Active subscription ───────────────────────────────────────────────────

class _ActiveSubscriptionView extends StatelessWidget {
  final SubscriptionLoaded state;
  const _ActiveSubscriptionView({Key? key, required this.state})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        children: [
          const SizedBox(height: 16),
          // Badge
          Container(
            width: 96,
            height: 96,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: cs.primaryContainer,
            ),
            child: Icon(Icons.verified_rounded,
                size: 52, color: cs.onPrimaryContainer),
          ),
          const SizedBox(height: 20),
          Text('You\'re a Member!',
              style: tt.headlineSmall?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 6),
          if (state.subscription.productId != null)
            Text(_productName(state.subscription.productId!),
                style: tt.titleMedium?.copyWith(color: cs.primary)),
          const SizedBox(height: 4),
          if (state.subscription.expiresAt != null)
            Text(
              'Renews ${_formatDate(state.subscription.expiresAt!)}',
              style: tt.bodyMedium?.copyWith(color: cs.onSurfaceVariant),
            )
          else
            Text('Lifetime Access — never expires',
                style: tt.bodyMedium?.copyWith(
                    color: cs.primary, fontWeight: FontWeight.w600)),
          const SizedBox(height: 32),
          _BenefitsTiles(),
          const SizedBox(height: 24),
          OutlinedButton.icon(
            icon: const Icon(Icons.settings_outlined, size: 18),
            label: const Text('Manage Subscription'),
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                  content: Text('Manage subscription in your app store')),
            ),
          ),
        ],
      ),
    );
  }

  String _productName(String id) {
    if (id.contains('monthly')) return 'Monthly Plan';
    if (id.contains('annual')) return 'Annual Plan';
    if (id.contains('lifetime')) return 'Lifetime Access';
    return 'Premium';
  }

  String _formatDate(DateTime date) {
    const months = [
      'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
    ];
    return '${months[date.month - 1]} ${date.day}, ${date.year}';
  }
}

// ─── Plans (upsell) view ────────────────────────────────────────────────────

class _PlansView extends StatelessWidget {
  const _PlansView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return SingleChildScrollView(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // ── Hero banner ─────────────────────────────────────────────────
          Container(
            width: double.infinity,
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  cs.primary,
                  cs.tertiary,
                ],
              ),
            ),
            padding: const EdgeInsets.fromLTRB(24, 40, 24, 40),
            child: Column(
              children: [
                Icon(Icons.stars_rounded, size: 56, color: cs.onPrimary),
                const SizedBox(height: 16),
                Text(
                  'Unlock Full Access',
                  style: tt.headlineSmall?.copyWith(
                    color: cs.onPrimary,
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 8),
                Text(
                  'Join the community. Share your faith.\nSupport the mission.',
                  style: tt.bodyMedium?.copyWith(
                    color: cs.onPrimary.withOpacity(0.85),
                  ),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),

          Padding(
            padding: const EdgeInsets.fromLTRB(16, 28, 16, 0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // ── Benefits ──────────────────────────────────────────────
                Text(
                  'WHAT YOU GET',
                  style: tt.labelMedium?.copyWith(
                    color: cs.primary,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 1.2,
                  ),
                ),
                const SizedBox(height: 12),
                _BenefitsTiles(),

                const SizedBox(height: 32),

                // ── Plans ─────────────────────────────────────────────────
                Text(
                  'CHOOSE A PLAN',
                  style: tt.labelMedium?.copyWith(
                    color: cs.primary,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 1.2,
                  ),
                ),
                const SizedBox(height: 12),

                _PlanCard(
                  title: 'Monthly',
                  price: '\$1.99',
                  period: 'per month',
                  productId: 'com.lifeissues.monthly',
                ),
                const SizedBox(height: 12),
                _PlanCard(
                  title: 'Annual',
                  price: '\$9.99',
                  period: 'per year',
                  savingsLabel: 'Save \$14 · Best Value',
                  productId: 'com.lifeissues.annual',
                  isFeatured: true,
                ),
                const SizedBox(height: 12),
                _PlanCard(
                  title: 'Lifetime',
                  price: '\$24.99',
                  period: 'one-time',
                  savingsLabel: 'Pay once, never again',
                  productId: 'com.lifeissues.lifetime',
                ),

                const SizedBox(height: 24),

                // ── Footer links ─────────────────────────────────────────
                Center(
                  child: TextButton(
                    onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(
                          content: Text('Restore purchases coming soon')),
                    ),
                    child: const Text('Restore Purchases'),
                  ),
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    TextButton(
                      onPressed: () {_openTerms();},
                      child: const Text('Terms of Service'),
                    ),
                    Text(' · ', style: tt.bodySmall),
                    TextButton(
                      onPressed: () {_openPrivacyPolicy();},
                      child: const Text('Privacy Policy'),
                    ),
                  ],
                ),
                const SizedBox(height: 24),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

Future<void> _openPrivacyPolicy() async {
  final Uri url = Uri.parse('https://yachalapp.emtechint.com/privacy');
  if (await canLaunchUrl(url)) {
    await launchUrl(url, mode: LaunchMode.externalApplication);
  }
}

Future<void> _openTerms() async {
  final Uri url = Uri.parse('https://yachalapp.emtechint.com/terms');
  if (await canLaunchUrl(url)) {
    await launchUrl(url, mode: LaunchMode.externalApplication);
  }
}

// ─── Benefits tiles ─────────────────────────────────────────────────────────

class _BenefitsTiles extends StatelessWidget {
  static const _benefits = [
    _Benefit(
      icon: Icons.block_rounded,
      title: 'No Ads',
      description:
          'Enjoy a clean, distraction-free experience with zero advertisements.',
    ),
    _Benefit(
      icon: Icons.favorite_rounded,
      title: 'Post Prayer Requests',
      description:
          'Share your prayer needs with the community and receive intercession from brothers and sisters in faith.',
    ),
    _Benefit(
      icon: Icons.auto_awesome_rounded,
      title: 'Share Testimonies',
      description:
          'Declare what God has done in your life and inspire others with your story of faith.',
    ),
    _Benefit(
      icon: Icons.volunteer_activism_rounded,
      title: 'Support the Mission',
      description:
          'Your subscription helps keep this ministry running and reaching more people.',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Column(
      children: _benefits
          .map((b) => _BenefitTile(benefit: b))
          .toList(),
    );
  }
}

class _Benefit {
  final IconData icon;
  final String title;
  final String description;
  const _Benefit(
      {required this.icon, required this.title, required this.description});
}

class _BenefitTile extends StatelessWidget {
  final _Benefit benefit;
  const _BenefitTile({Key? key, required this.benefit}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: cs.primaryContainer,
              borderRadius: BorderRadius.circular(12),
            ),
            child:
                Icon(benefit.icon, size: 22, color: cs.onPrimaryContainer),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(benefit.title,
                    style: tt.titleSmall
                        ?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 2),
                Text(benefit.description,
                    style: tt.bodySmall
                        ?.copyWith(color: cs.onSurfaceVariant)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

// ─── Plan card ───────────────────────────────────────────────────────────────

class _PlanCard extends StatelessWidget {
  final String title;
  final String price;
  final String period;
  final String? savingsLabel;
  final String productId;
  final bool isFeatured;

  const _PlanCard({
    Key? key,
    required this.title,
    required this.price,
    required this.period,
    this.savingsLabel,
    required this.productId,
    this.isFeatured = false,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    final bgColor =
        isFeatured ? cs.primaryContainer : cs.surfaceContainerHigh;
    final textColor =
        isFeatured ? cs.onPrimaryContainer : cs.onSurface;
    final subTextColor =
        isFeatured ? cs.onPrimaryContainer.withOpacity(0.7) : cs.onSurfaceVariant;

    return Container(
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(16),
        border: isFeatured
            ? Border.all(color: cs.primary, width: 2)
            : null,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Featured label
          if (isFeatured)
            Container(
              decoration: BoxDecoration(
                color: cs.primary,
                borderRadius: const BorderRadius.only(
                  topLeft: Radius.circular(14),
                  topRight: Radius.circular(14),
                ),
              ),
              padding: const EdgeInsets.symmetric(vertical: 6),
              child: Text(
                'BEST VALUE',
                textAlign: TextAlign.center,
                style: tt.labelMedium?.copyWith(
                  color: cs.onPrimary,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 1,
                ),
              ),
            ),

          Padding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
            child: Row(
              children: [
                // Price info
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(title,
                          style: tt.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: textColor)),
                      const SizedBox(height: 4),
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.end,
                        children: [
                          Text(price,
                              style: tt.headlineSmall?.copyWith(
                                fontWeight: FontWeight.bold,
                                color: isFeatured ? cs.onPrimaryContainer : cs.primary,
                              )),
                          const SizedBox(width: 4),
                          Padding(
                            padding: const EdgeInsets.only(bottom: 3),
                            child: Text(period,
                                style: tt.bodySmall
                                    ?.copyWith(color: subTextColor)),
                          ),
                        ],
                      ),
                      if (savingsLabel != null) ...[
                        const SizedBox(height: 2),
                        Text(savingsLabel!,
                            style: tt.labelSmall?.copyWith(
                              color: isFeatured
                                  ? cs.onPrimaryContainer.withOpacity(0.8)
                                  : cs.primary,
                              fontWeight: FontWeight.w600,
                            )),
                      ],
                    ],
                  ),
                ),

                // CTA button
                FilledButton(
                  onPressed: () => _handlePurchase(context, productId),
                  style: isFeatured
                      ? FilledButton.styleFrom(
                          backgroundColor: cs.primary,
                          foregroundColor: cs.onPrimary,
                        )
                      : null,
                  child: const Text('Subscribe'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _handlePurchase(BuildContext context, String id) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Purchase $id — coming soon')),
    );
  }
}
