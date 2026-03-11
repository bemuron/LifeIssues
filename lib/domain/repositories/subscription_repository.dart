// lib/domain/repositories/subscription_repository.dart

import '../entities/subscription.dart';

abstract class SubscriptionRepository {
  Future<Subscription> getSubscriptionStatus();
  Future<void> syncQonversionSubscription(Map<String, dynamic> qonversionData);
}