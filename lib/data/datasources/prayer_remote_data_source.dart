// lib/data/datasources/prayer_remote_datasource.dart

import '../models/prayer_model.dart';
import '../../core/network/api_client.dart';
import '../../core/config/api_config.dart';

abstract class PrayerRemoteDataSource {
  Future<List<PrayerModel>> getPrayers({int page = 1, String? category, String? sortBy,
    bool? hasPrayers});
  Future<PrayerModel> getPrayerById(int id);
  Future<PrayerModel> submitPrayer({
    required String body,
    String? category,
    bool isAnonymous = false,
  });
  Future<Map<String, dynamic>> togglePraying(int prayerId);
  Future<void> deletePrayer(int prayerId);
  Future<List<PrayerModel>> getMyPrayers({int page = 1});
}

class PrayerRemoteDataSourceImpl implements PrayerRemoteDataSource {
  final ApiClient apiClient;

  PrayerRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<List<PrayerModel>> getPrayers({int page = 1, String? category, String? sortBy,
    bool? hasPrayers,}) async {
    final queryParams = <String, dynamic>{
      'page': page,
      'per_page': ApiConfig.defaultPageSize,
    };

    if (category != null && category.isNotEmpty) {
      queryParams['category'] = category;
    }

    if (sortBy != null) {
      queryParams['sort'] = sortBy; // 'newest', 'oldest', 'needs_prayer'
    }

    if (hasPrayers != null) {
      queryParams['has_prayers'] = hasPrayers ? '1' : '0';
    }

    final response = await apiClient.get(
      ApiConfig.prayers,
      queryParameters: queryParams,
    );

    final data = response.data['data'] as List;
    return data.map((json) => PrayerModel.fromJson(json)).toList();
  }

  @override
  Future<PrayerModel> getPrayerById(int id) async {
    final response = await apiClient.get(ApiConfig.prayerById(id));
    return PrayerModel.fromJson(response.data['data']);
  }

  @override
  Future<PrayerModel> submitPrayer({
    required String body,
    String? category,
    bool isAnonymous = false,
  }) async {
    final response = await apiClient.post(
      ApiConfig.prayers,
      data: {
        'body': body,
        'category': category,
        'is_anonymous': isAnonymous,
      },
    );

    return PrayerModel.fromJson(response.data['data']);
  }

  @override
  Future<Map<String, dynamic>> togglePraying(int prayerId) async {
    final response = await apiClient.post(
      ApiConfig.prayerPray(prayerId),
    );

    return {
      'has_prayed': response.data['has_prayed'] as bool,
      'pray_count': response.data['pray_count'] as int,
      'already_prayed': response.data['already_prayed'] as bool? ?? false,
    };
  }

  @override
  Future<void> deletePrayer(int prayerId) async {
    await apiClient.delete(ApiConfig.prayerById(prayerId));
  }

  @override
  Future<List<PrayerModel>> getMyPrayers({int page = 1}) async {
    final response = await apiClient.get(
      ApiConfig.myPrayers,
      queryParameters: {
        'page': page,
        'per_page': ApiConfig.defaultPageSize,
      },
    );

    final data = response.data['data'] as List;
    return data.map((json) => PrayerModel.fromJson(json)).toList();
  }
}