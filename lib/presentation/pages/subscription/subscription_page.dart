// lib/presentation/pages/subscription/subscription_page.dart

import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
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
      child: const SubscriptionView(),
    );
  }
}

class SubscriptionView extends StatelessWidget {
  const SubscriptionView({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Subscription'),
      ),
      body: BlocBuilder<SubscriptionBloc, SubscriptionState>(
        builder: (context, state) {
          if (state is SubscriptionLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (state is SubscriptionLoaded) {
            if (state.canPost) {
              return _buildActiveSubscription(context, state);
            }
          }

          return _buildSubscriptionPlans(context);
        },
      ),
    );
  }

  Widget _buildActiveSubscription(
      BuildContext context,
      SubscriptionLoaded state,
      ) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.verified,
              size: 80,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(height: 24),
            Text(
              'Active Subscription',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 8),
            if (state.subscription.productId != null)
              Text(
                _getProductName(state.subscription.productId!),
                style: Theme.of(context).textTheme.titleLarge,
              ),
            const SizedBox(height: 16),
            if (state.subscription.expiresAt != null) ...[
              Text(
                'Expires on ${_formatDate(state.subscription.expiresAt!)}',
                style: Theme.of(context).textTheme.bodyLarge,
              ),
            ] else ...[
              Text(
                'Lifetime Access',
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                  color: Theme.of(context).colorScheme.primary,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
            const SizedBox(height: 32),
            _buildBenefitsList(context),
            const SizedBox(height: 32),
            OutlinedButton(
              onPressed: () {
                // TODO: Open platform subscription management
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Manage subscription in your app store'),
                  ),
                );
              },
              child: const Text('Manage Subscription'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSubscriptionPlans(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // Header
          Text(
            'Join Our Community',
            style: Theme.of(context).textTheme.headlineMedium,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 8),
          Text(
            'Support the mission and unlock premium features',
            style: Theme.of(context).textTheme.bodyLarge,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 32),

          // Benefits
          _buildBenefitsList(context),
          const SizedBox(height: 32),

          // Plans
          _buildPlanCard(
            context,
            title: 'Monthly',
            price: '\$1.99',
            period: '/month',
            productId: 'com.lifeissues.monthly',
            isBestValue: false,
          ),
          const SizedBox(height: 16),
          _buildPlanCard(
            context,
            title: 'Annual',
            price: '\$9.99',
            period: '/year',
            subtitle: 'Save \$14',
            productId: 'com.lifeissues.annual',
            isBestValue: true,
          ),
          const SizedBox(height: 16),
          _buildPlanCard(
            context,
            title: 'Lifetime',
            price: '\$24.99',
            period: 'one-time',
            subtitle: 'Never pay again',
            productId: 'com.lifeissues.lifetime',
            isBestValue: false,
          ),
          const SizedBox(height: 24),

          // Restore purchases
          TextButton(
            onPressed: () {
              // TODO: Restore purchases
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Restore purchases coming soon')),
              );
            },
            child: const Text('Restore Purchases'),
          ),
          const SizedBox(height: 16),

          // Terms & Privacy
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              TextButton(
                onPressed: () {
                  // TODO: Open Terms of Service
                },
                child: const Text('Terms of Service'),
              ),
              const Text(' • '),
              TextButton(
                onPressed: () {
                  // TODO: Open Privacy Policy
                },
                child: const Text('Privacy Policy'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildBenefitsList(BuildContext context) {
    final benefits = [
      'Post prayer requests',
      'Share your testimonies',
      'No advertisements',
      'Support the mission',
    ];

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Premium Benefits',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            ...benefits.map(
                  (benefit) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  children: [
                    Icon(
                      Icons.check_circle,
                      color: Theme.of(context).colorScheme.primary,
                      size: 20,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        benefit,
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPlanCard(
      BuildContext context, {
        required String title,
        required String price,
        required String period,
        String? subtitle,
        required String productId,
        bool isBestValue = false,
      }) {
    return Card(
      elevation: isBestValue ? 4 : 1,
      color: isBestValue
          ? Theme.of(context).colorScheme.primaryContainer
          : null,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (isBestValue)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.primary,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: const Text(
                  'BEST VALUE',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
            if (isBestValue) const SizedBox(height: 12),
            Text(
              title,
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 4),
            Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  price,
                  style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
                const SizedBox(width: 4),
                Padding(
                  padding: const EdgeInsets.only(bottom: 4),
                  child: Text(
                    period,
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ),
              ],
            ),
            if (subtitle != null) ...[
              const SizedBox(height: 4),
              Text(
                subtitle,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
            ],
            const SizedBox(height: 16),
            FilledButton(
              onPressed: () => _handlePurchase(context, productId),
              child: const Text('Subscribe'),
            ),
          ],
        ),
      ),
    );
  }

  String _getProductName(String productId) {
    if (productId.contains('monthly')) return 'Monthly Plan';
    if (productId.contains('annual')) return 'Annual Plan';
    if (productId.contains('lifetime')) return 'Lifetime Access';
    return 'Premium';
  }

  String _formatDate(DateTime date) {
    final months = [
      'Jan',
      'Feb',
      'Mar',
      'Apr',
      'May',
      'Jun',
      'Jul',
      'Aug',
      'Sep',
      'Oct',
      'Nov',
      'Dec'
    ];
    return '${months[date.month - 1]} ${date.day}, ${date.year}';
  }

  void _handlePurchase(BuildContext context, String productId) {
    // TODO: Implement Qonversion purchase
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('Purchase $productId - Coming soon')),
    );
  }
}