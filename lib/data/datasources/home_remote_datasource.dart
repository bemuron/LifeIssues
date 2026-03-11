// lib/data/datasources/home_remote_datasource.dart

import '../models/home_summary_model.dart';
import '../../core/network/api_client.dart';
import '../../core/config/api_config.dart';

abstract class HomeRemoteDataSource {
  Future<HomeSummaryModel> getHomeSummary();
}

class HomeRemoteDataSourceImpl implements HomeRemoteDataSource {
  final ApiClient apiClient;

  HomeRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<HomeSummaryModel> getHomeSummary() async {
    final response = await apiClient.get(ApiConfig.homeSummary);
    return HomeSummaryModel.fromJson(response.data);
  }
}