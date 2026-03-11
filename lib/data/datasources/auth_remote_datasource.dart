// lib/data/datasources/auth_remote_datasource.dart

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
  Future<void> logout();
}

class AuthRemoteDataSourceImpl implements AuthRemoteDataSource {
  final ApiClient apiClient;

  AuthRemoteDataSourceImpl({required this.apiClient});

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

    return UserModel.fromJson(response.data['user']);
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
      },
    );

    return UserModel.fromJson(response.data['user']);
  }

  @override
  Future<UserModel> loginWithGoogle(String idToken) async {
    final response = await apiClient.post(
      '${ApiConfig.login}/google',
      data: {
        'id_token': idToken,
      },
    );

    return UserModel.fromJson(response.data['user']);
  }

  @override
  Future<UserModel> loginWithApple(String idToken) async {
    final response = await apiClient.post(
      '${ApiConfig.login}/apple',
      data: {
        'id_token': idToken,
      },
    );

    return UserModel.fromJson(response.data['user']);
  }

  @override
  Future<UserModel> getCurrentUser() async {
    final response = await apiClient.get(ApiConfig.user);
    return UserModel.fromJson(response.data['user']);
  }

  @override
  Future<void> logout() async {
    await apiClient.post(ApiConfig.logout);
  }
}