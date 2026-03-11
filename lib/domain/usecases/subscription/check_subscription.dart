// lib/domain/usecases/subscription/check_subscription.dart

import '../../repositories/subscription_repository.dart';

class CheckSubscription {
  final SubscriptionRepository repository;

  CheckSubscription(this.repository);

  /// Returns true if user is subscribed and can post prayers/testimonies
  Future<bool> call() async {
    try {
      final subscription = await repository.getSubscriptionStatus();
      return subscription.isValid; // Uses the isValid getter from Subscription entity
    } catch (e) {
      // If error checking subscription, assume not subscribed
      return false;
    }
  }
}