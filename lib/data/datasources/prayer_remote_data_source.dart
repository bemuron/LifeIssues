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
  Future<PrayerModel> editPrayer({
    required int prayerId,
    required String body,
    String? category,
    bool? isAnonymous,
  });
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
    return PrayerModel.fromJson(response.data as Map<String, dynamic>);
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

    return PrayerModel.fromJson(response.data['prayer']);
  }

  @override
  Future<Map<String, dynamic>> togglePraying(int prayerId) async {
    final response = await apiClient.post(
      ApiConfig.prayerPray(prayerId),
    );

    final rawHasPrayed = response.data['has_prayed'];
    final rawPrayCount = response.data['pray_count'];
    return {
      'has_prayed': rawHasPrayed == true || rawHasPrayed == 1,
      'pray_count': rawPrayCount is int ? rawPrayCount : (rawPrayCount as num?)?.toInt() ?? 0,
      'already_prayed': response.data['already_prayed'] == true || response.data['already_prayed'] == 1,
    };
  }

  @override
  Future<PrayerModel> editPrayer({
    required int prayerId,
    required String body,
    String? category,
    bool? isAnonymous,
  }) async {
    final data = <String, dynamic>{'body': body};
    if (category != null) data['category'] = category;
    if (isAnonymous != null) data['is_anonymous'] = isAnonymous;

    final response = await apiClient.put(
      ApiConfig.prayerById(prayerId),
      data: data,
    );

    return PrayerModel.fromJson(response.data['prayer'] ?? response.data);
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