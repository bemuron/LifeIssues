// lib/domain/usecases/subscription/get_subscription_status.dart

import '../../entities/subscription.dart';
import '../../repositories/subscription_repository.dart';

class GetSubscriptionStatus {
  final SubscriptionRepository repository;

  GetSubscriptionStatus(this.repository);

  Future<Subscription> call() async {
    return await repository.getSubscriptionStatus();
  }
}