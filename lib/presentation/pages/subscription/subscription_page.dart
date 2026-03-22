// lib/presentation/pages/subscription/subscription_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:qonversion_flutter/qonversion_flutter.dart';
import 'package:url_launcher/url_launcher.dart';
import '../../../core/di/injection_container.dart' as di;
import '../../blocs/auth/auth_bloc.dart';
import '../../blocs/auth/auth_state.dart';
import '../../blocs/subscription/subscription_bloc.dart';
import '../../blocs/subscription/subscription_event.dart';
import '../../blocs/subscription/subscription_state.dart';

// ─── Qonversion IDs ──────────────────────────────────────────────────────────
// These must match the IDs configured in the Qonversion dashboard.
const _kEntitlementId = 'premium';
const _kMonthlyProductId = 'monthly';
const _kAnnualProductId = 'annual';

// ─── Page ────────────────────────────────────────────────────────────────────

class SubscriptionPage extends StatelessWidget {
  const SubscriptionPage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      create: (_) =>
          di.sl<SubscriptionBloc>()..add(LoadSubscriptionStatusEvent()),
      child: const _SubscriptionView(),
    );
  }
}

// ─── Inner stateful view ─────────────────────────────────────────────────────

class _SubscriptionView extends StatefulWidget {
  const _SubscriptionView({Key? key}) : super(key: key);

  @override
  State<_SubscriptionView> createState() => _SubscriptionViewState();
}

class _SubscriptionViewState extends State<_SubscriptionView> {
  Map<String, QProduct> _products = {};
  bool _loadingProducts = true;
  bool _isPurchasing = false;
  bool _isRestoring = false;

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    await _identifyUser();
    await _loadProducts();
  }

  Future<void> _identifyUser() async {
    try {
      final authState = context.read<AuthBloc>().state;
      if (authState is Authenticated) {
        await Qonversion.getSharedInstance()
            .identify(authState.user.id.toString());
      }
    } catch (_) {}
  }

  Future<void> _loadProducts() async {
    try {
      final products = await Qonversion.getSharedInstance().products();
      if (mounted) {
        setState(() {
          _products = products;
          _loadingProducts = false;
        });
      }
    } catch (_) {
      if (mounted) setState(() => _loadingProducts = false);
    }
  }

  Future<void> _purchase(QProduct product) async {
    if (_isPurchasing) return;
    setState(() => _isPurchasing = true);
    try {
      final result =
          await Qonversion.getSharedInstance().purchaseWithResult(product);
      if (result.isSuccess && result.entitlements != null) {
        _syncToBackend(result.entitlements!, status: 'active');
      } else if (result.isError && mounted) {
        _showSnackBar(result.error?.message ?? 'Purchase failed. Please try again.');
      }
      // isCanceled and isPending: do nothing
    } on QPurchaseException catch (e) {
      if (!e.isUserCancelled && mounted) {
        _showSnackBar(e.message);
      }
    } catch (_) {
      if (mounted) _showSnackBar('Purchase failed. Please try again.');
    } finally {
      if (mounted) setState(() => _isPurchasing = false);
    }
  }

  Future<void> _restore() async {
    if (_isRestoring) return;
    setState(() => _isRestoring = true);
    try {
      final entitlements = await Qonversion.getSharedInstance().restore();
      final premium = entitlements[_kEntitlementId];
      if (premium != null && premium.isActive) {
        _syncToBackend(entitlements, status: 'restored');
      } else if (mounted) {
        _showSnackBar('No active subscription found to restore.');
      }
    } catch (_) {
      if (mounted) _showSnackBar('Restore failed. Please try again.');
    } finally {
      if (mounted) setState(() => _isRestoring = false);
    }
  }

  void _syncToBackend(
    Map<String, QEntitlement> entitlements, {
    required String status,
  }) {
    final entitlement = entitlements[_kEntitlementId];
    if (entitlement == null || !mounted) return;
    context.read<SubscriptionBloc>().add(SyncQonversionEvent({
      'entitlement_id': entitlement.id,
      'product_id': entitlement.productId,
      'is_active': entitlement.isActive,
      'expires_at': entitlement.expirationDate?.toIso8601String(),
      'renew_state': entitlement.renewState.name,
      'status': status,
    }));
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Premium')),
      body: BlocConsumer<SubscriptionBloc, SubscriptionState>(
        listener: (context, state) {
          if (state is SubscriptionSynced) {
            _showSnackBar('Subscription activated successfully!');
          } else if (state is SubscriptionError) {
            _showSnackBar(state.message);
          }
        },
        builder: (context, state) {
          if (state is SubscriptionLoading || state is SubscriptionSyncing) {
            return const Center(child: CircularProgressIndicator());
          }
          if (state is SubscriptionLoaded && state.canPost) {
            return _ActiveView(state: state);
          }
          return _PlansView(
            products: _products,
            loadingProducts: _loadingProducts,
            isPurchasing: _isPurchasing,
            isRestoring: _isRestoring,
            onPurchase: _purchase,
            onRestore: _restore,
          );
        },
      ),
    );
  }
}

// ─── Active subscription view ────────────────────────────────────────────────

class _ActiveView extends StatelessWidget {
  final SubscriptionLoaded state;
  const _ActiveView({Key? key, required this.state}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Column(
        children: [
          const SizedBox(height: 16),
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
            Text(_productLabel(state.subscription.productId!),
                style: tt.titleMedium?.copyWith(color: cs.primary)),
          const SizedBox(height: 4),
          if (state.subscription.expiresAt != null)
            Text(
              'Renews ${_formatDate(state.subscription.expiresAt!)}',
              style: tt.bodyMedium?.copyWith(color: cs.onSurfaceVariant),
            ),
          const SizedBox(height: 32),
          const _BenefitsTiles(),
          const SizedBox(height: 24),
          OutlinedButton.icon(
            icon: const Icon(Icons.open_in_new_rounded, size: 18),
            label: const Text('Manage Subscription'),
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                  content: Text('Manage your subscription in the Play Store')),
            ),
          ),
        ],
      ),
    );
  }

  String _productLabel(String id) {
    if (id.contains('monthly')) return 'Monthly Plan';
    if (id.contains('annual') || id.contains('yearly')) return 'Annual Plan';
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

// ─── Plans (upsell) view ─────────────────────────────────────────────────────

class _PlansView extends StatelessWidget {
  final Map<String, QProduct> products;
  final bool loadingProducts;
  final bool isPurchasing;
  final bool isRestoring;
  final Future<void> Function(QProduct) onPurchase;
  final Future<void> Function() onRestore;

  const _PlansView({
    Key? key,
    required this.products,
    required this.loadingProducts,
    required this.isPurchasing,
    required this.isRestoring,
    required this.onPurchase,
    required this.onRestore,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    final monthly = products[_kMonthlyProductId];
    final annual = products[_kAnnualProductId];

    return Stack(
      children: [
        SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // ── Hero banner ────────────────────────────────────────────────
              Container(
                width: double.infinity,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [cs.primary, cs.tertiary],
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
                      style: tt.bodyMedium
                          ?.copyWith(color: cs.onPrimary.withOpacity(0.85)),
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
                    // ── Benefits ───────────────────────────────────────────
                    Text(
                      'WHAT YOU GET',
                      style: tt.labelMedium?.copyWith(
                        color: cs.primary,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 1.2,
                      ),
                    ),
                    const SizedBox(height: 12),
                    const _BenefitsTiles(),

                    const SizedBox(height: 32),

                    // ── Plans ─────────────────────────────────────────────
                    Text(
                      'CHOOSE A PLAN',
                      style: tt.labelMedium?.copyWith(
                        color: cs.primary,
                        fontWeight: FontWeight.bold,
                        letterSpacing: 1.2,
                      ),
                    ),
                    const SizedBox(height: 12),

                    if (loadingProducts)
                      const Center(
                        child: Padding(
                          padding: EdgeInsets.symmetric(vertical: 32),
                          child: CircularProgressIndicator(),
                        ),
                      )
                    else ...[
                      _PlanCard(
                        title: 'Monthly',
                        price: monthly?.prettyPrice ?? '\$1.99',
                        period: 'per month',
                        product: monthly,
                        isPurchasing: isPurchasing,
                        onPurchase: onPurchase,
                      ),
                      const SizedBox(height: 12),
                      _PlanCard(
                        title: 'Annual',
                        price: annual?.prettyPrice ?? '\$9.99',
                        period: 'per year',
                        savingsLabel: 'Save 58% · Best Value',
                        product: annual,
                        isFeatured: true,
                        isPurchasing: isPurchasing,
                        onPurchase: onPurchase,
                      ),
                    ],

                    const SizedBox(height: 24),

                    // ── Footer ─────────────────────────────────────────────
                    Center(
                      child: TextButton(
                        onPressed: (isRestoring || isPurchasing)
                            ? null
                            : onRestore,
                        child: isRestoring
                            ? const SizedBox(
                                width: 18,
                                height: 18,
                                child:
                                    CircularProgressIndicator(strokeWidth: 2),
                              )
                            : const Text('Restore Purchases'),
                      ),
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        TextButton(
                          onPressed: _openTerms,
                          child: const Text('Terms of Service'),
                        ),
                        Text(' · ', style: tt.bodySmall),
                        TextButton(
                          onPressed: _openPrivacy,
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
        ),

        // ── Full-screen purchase overlay ───────────────────────────────────
        if (isPurchasing)
          const Positioned.fill(
            child: ColoredBox(
              color: Colors.black26,
              child: Center(child: CircularProgressIndicator()),
            ),
          ),
      ],
    );
  }
}

// ─── Benefits ────────────────────────────────────────────────────────────────

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

  const _BenefitsTiles({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      children: _benefits.map((b) => _BenefitTile(benefit: b)).toList(),
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
            child: Icon(benefit.icon, size: 22, color: cs.onPrimaryContainer),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(benefit.title,
                    style:
                        tt.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                const SizedBox(height: 2),
                Text(benefit.description,
                    style:
                        tt.bodySmall?.copyWith(color: cs.onSurfaceVariant)),
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
  final QProduct? product;
  final bool isFeatured;
  final bool isPurchasing;
  final Future<void> Function(QProduct) onPurchase;

  const _PlanCard({
    Key? key,
    required this.title,
    required this.price,
    required this.period,
    this.savingsLabel,
    required this.product,
    this.isFeatured = false,
    required this.isPurchasing,
    required this.onPurchase,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    final bgColor =
        isFeatured ? cs.primaryContainer : cs.surfaceContainerHigh;
    final textColor = isFeatured ? cs.onPrimaryContainer : cs.onSurface;
    final subTextColor = isFeatured
        ? cs.onPrimaryContainer.withOpacity(0.7)
        : cs.onSurfaceVariant;

    return Container(
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(16),
        border:
            isFeatured ? Border.all(color: cs.primary, width: 2) : null,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
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
                          Text(
                            price,
                            style: tt.headlineSmall?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: isFeatured
                                  ? cs.onPrimaryContainer
                                  : cs.primary,
                            ),
                          ),
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
                        Text(
                          savingsLabel!,
                          style: tt.labelSmall?.copyWith(
                            color: isFeatured
                                ? cs.onPrimaryContainer.withOpacity(0.8)
                                : cs.primary,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ],
                  ),
                ),

                FilledButton(
                  onPressed: (product == null || isPurchasing)
                      ? null
                      : () => onPurchase(product!),
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
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

Future<void> _openPrivacy() async {
  final uri = Uri.parse('https://yachalapp.emtechint.com/privacy');
  if (await canLaunchUrl(uri)) await launchUrl(uri, mode: LaunchMode.externalApplication);
}

Future<void> _openTerms() async {
  final uri = Uri.parse('https://yachalapp.emtechint.com/terms');
  if (await canLaunchUrl(uri)) await launchUrl(uri, mode: LaunchMode.externalApplication);
}
