// lib/data/datasources/auth_remote_datasource.dart

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../models/user_model.dart';
import '../../core/network/api_client.dart';
import '../../core/config/api_config.dart';

abstract class AuthRemoteDataSource {
  Future<UserModel> login({
    required String email,
    required String password,
  });

  Future<UserModel> register({
    required String name,
    required String email,
    required String password,
  });

  Future<UserModel> loginWithGoogle(String idToken);
  Future<UserModel> loginWithApple(String idToken);

  Future<UserModel> getCurrentUser();
  Future<UserModel> updateProfile({
    required String name,
    String? password,
    String? imagePath,
  });
  Future<void> logout();
}

class AuthRemoteDataSourceImpl implements AuthRemoteDataSource {
  final ApiClient apiClient;

  AuthRemoteDataSourceImpl({required this.apiClient});

  /// The API returns: { "user": {...}, "token": "...", "token_type": "Bearer" }
  /// The token is at the ROOT level, not inside the user object.
  /// This helper merges both so UserModel gets the token.
  UserModel _parseAuthResponse(dynamic responseData) {
    final data = responseData as Map<String, dynamic>;
    debugPrint('🔍 Auth response keys: ${data.keys.toList()}');
    debugPrint('🔍 Token from response: ${data['token']}');

    final userMap = Map<String, dynamic>.from(data['user'] as Map);
    // Inject root-level token into the user map
    userMap['token'] = data['token']?.toString();

    debugPrint('🔍 UserMap token: ${userMap['token']}');
    return UserModel.fromJson(userMap);
  }

  @override
  Future<UserModel> login({
    required String email,
    required String password,
  }) async {
    final response = await apiClient.post(
      ApiConfig.login,
      data: {
        'email': email,
        'password': password,
      },
    );

    return _parseAuthResponse(response.data);
  }

  @override
  Future<UserModel> register({
    required String name,
    required String email,
    required String password,
  }) async {
    final response = await apiClient.post(
      ApiConfig.register,
      data: {
        'name': name,
        'email': email,
        'password': password,
        'password_confirmation': password,
      },
    );

    return _parseAuthResponse(response.data);
  }

  @override
  Future<UserModel> loginWithGoogle(String idToken) async {
    final response = await apiClient.post(
      '${ApiConfig.login}/google',
      data: {
        'id_token': idToken,
      },
    );

    return _parseAuthResponse(response.data);
  }

  @override
  Future<UserModel> loginWithApple(String idToken) async {
    final response = await apiClient.post(
      '${ApiConfig.login}/apple',
      data: {
        'id_token': idToken,
      },
    );

    return _parseAuthResponse(response.data);
  }

  @override
  Future<UserModel> getCurrentUser() async {
    final response = await apiClient.get(ApiConfig.user);
    return UserModel.fromJson(response.data['user']);
  }

  @override
  Future<UserModel> updateProfile({
    required String name,
    String? password,
    String? imagePath,
  }) async {
    Response response;

    if (imagePath != null) {
      // Multipart form upload when image is provided
      final formData = FormData.fromMap({
        'name': name,
        if (password != null && password.isNotEmpty) 'password': password,
        'profile_image': await MultipartFile.fromFile(
          imagePath,
          filename: imagePath.split('/').last,
        ),
      });
      response = await apiClient.post(
        ApiConfig.updateProfile,
        data: formData,
      );
    } else {
      // Plain JSON when no image
      final data = <String, dynamic>{'name': name};
      if (password != null && password.isNotEmpty) {
        data['password'] = password;
      }
      response = await apiClient.put(ApiConfig.updateProfile, data: data);
    }

    return UserModel.fromJson(response.data['user']);
  }

  @override
  Future<void> logout() async {
    await apiClient.post(ApiConfig.logout);
  }
}
