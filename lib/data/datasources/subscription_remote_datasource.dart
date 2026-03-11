// lib/data/datasources/subscription_remote_datasource.dart

import '../models/subscription_model.dart';
import '../../core/network/api_client.dart';
import '../../core/config/api_config.dart';

abstract class SubscriptionRemoteDataSource {
  Future<SubscriptionModel> getSubscriptionStatus();
  Future<void> syncQonversionSubscription(Map<String, dynamic> qonversionData);
}

class SubscriptionRemoteDataSourceImpl implements SubscriptionRemoteDataSource {
  final ApiClient apiClient;

  SubscriptionRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<SubscriptionModel> getSubscriptionStatus() async {
    final response = await apiClient.get(ApiConfig.subscriptionStatus);
    return SubscriptionModel.fromJson(response.data['subscription']);
  }

  @override
  Future<void> syncQonversionSubscription(
      Map<String, dynamic> qonversionData,
      ) async {
    await apiClient.post(
      ApiConfig.revenueCatWebhook, // Note: Update this to qonversion webhook
      data: qonversionData,
    );
  }
}