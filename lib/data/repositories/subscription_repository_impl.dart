// lib/data/repositories/subscription_repository_impl.dart

import '../../domain/entities/subscription.dart';
import '../../domain/repositories/subscription_repository.dart';
import '../datasources/subscription_remote_datasource.dart';

class SubscriptionRepositoryImpl implements SubscriptionRepository {
  final SubscriptionRemoteDataSource remoteDataSource;

  SubscriptionRepositoryImpl({required this.remoteDataSource});

  @override
  Future<Subscription> getSubscriptionStatus() async {
    final model = await remoteDataSource.getSubscriptionStatus();
    return model.toEntity();
  }

  @override
  Future<void> syncQonversionSubscription(
      Map<String, dynamic> qonversionData,
      ) async {
    await remoteDataSource.syncQonversionSubscription(qonversionData);
  }
}