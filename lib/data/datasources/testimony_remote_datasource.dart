// lib/data/datasources/testimony_remote_datasource.dart

import '../models/testimony_model.dart';
import '../../core/network/api_client.dart';
import '../../core/config/api_config.dart';

abstract class TestimonyRemoteDataSource {
  Future<List<TestimonyModel>> getTestimonies({
    int page = 1,
    String? category,
    String? sortBy,
    bool? linkedToPrayer,
    bool? hasPraise,
  });
  Future<TestimonyModel> getTestimonyById(int id);
  Future<TestimonyModel> submitTestimony({
    required String title,
    required String body,
    String? category,
    int? prayerId,
  });
  Future<Map<String, dynamic>> togglePraise(int testimonyId);
  Future<void> deleteTestimony(int testimonyId);
  Future<List<TestimonyModel>> getMyTestimonies({int page = 1});
}

class TestimonyRemoteDataSourceImpl implements TestimonyRemoteDataSource {
  final ApiClient apiClient;

  TestimonyRemoteDataSourceImpl({required this.apiClient});

  @override
  Future<List<TestimonyModel>> getTestimonies({
    int page = 1,
    String? category,
    String? sortBy,
    bool? linkedToPrayer,
    bool? hasPraise,
  }) async {
    final queryParams = <String, dynamic>{
      'page': page,
      'per_page': ApiConfig.testimonyPageSize,
    };

    if (category != null && category.isNotEmpty) {
      queryParams['category'] = category;
    }

    if (sortBy != null) {
      queryParams['sort'] = sortBy; // 'newest', 'oldest', 'most_praised'
    }

    if (linkedToPrayer != null) {
      queryParams['linked_to_prayer'] = linkedToPrayer ? '1' : '0';
    }

    if (hasPraise != null) {
      queryParams['has_praise'] = hasPraise ? '1' : '0';
    }

    final response = await apiClient.get(
      ApiConfig.testimonies,
      queryParameters: queryParams,
    );

    final data = response.data['data'] as List;
    return data.map((json) => TestimonyModel.fromJson(json)).toList();
  }

  @override
  Future<TestimonyModel> getTestimonyById(int id) async {
    final response = await apiClient.get(ApiConfig.testimonyById(id));
    return TestimonyModel.fromJson(response.data['data']);
  }

  @override
  Future<TestimonyModel> submitTestimony({
    required String title,
    required String body,
    String? category,
    int? prayerId,
  }) async {
    final response = await apiClient.post(
      ApiConfig.testimonies,
      data: {
        'title': title,
        'body': body,
        'category': category,
        'prayer_id': prayerId,
      },
    );

    return TestimonyModel.fromJson(response.data['data']);
  }

  @override
  Future<Map<String, dynamic>> togglePraise(int testimonyId) async {
    final response = await apiClient.post(
      ApiConfig.testimonyPraise(testimonyId),
    );

    return {
      'has_praised': response.data['has_praised'] as bool,
      'praise_count': response.data['praise_count'] as int,
      'already_praised': response.data['already_praised'] as bool? ?? false,
    };
  }

  @override
  Future<void> deleteTestimony(int testimonyId) async {
    await apiClient.delete(ApiConfig.testimonyById(testimonyId));
  }

  @override
  Future<List<TestimonyModel>> getMyTestimonies({int page = 1}) async {
    final response = await apiClient.get(
      ApiConfig.myTestimonies,
      queryParameters: {
        'page': page,
        'per_page': ApiConfig.testimonyPageSize,
      },
    );

    final data = response.data['data'] as List;
    return data.map((json) => TestimonyModel.fromJson(json)).toList();
  }
}