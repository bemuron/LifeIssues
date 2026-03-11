// lib/data/repositories/auth_repository_impl.dart

import '../../domain/entities/user.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_remote_datasource.dart';
import '../../core/utils/auth_manager.dart';

class AuthRepositoryImpl implements AuthRepository {
  final AuthRemoteDataSource remoteDataSource;
  final AuthManager authManager;

  AuthRepositoryImpl({
    required this.remoteDataSource,
    required this.authManager,
  });

  @override
  Future<User> login({
    required String email,
    required String password,
  }) async {
    final model = await remoteDataSource.login(
      email: email,
      password: password,
    );

    // Save auth data
    if (model.token != null) {
      await authManager.saveToken(model.token!);
      await authManager.saveUserInfo(
        userId: model.id,
        name: model.name,
        email: model.email,
      );
    }

    return model.toEntity();
  }

  @override
  Future<User> register({
    required String name,
    required String email,
    required String password,
  }) async {
    final model = await remoteDataSource.register(
      name: name,
      email: email,
      password: password,
    );

    // Save auth data
    if (model.token != null) {
      await authManager.saveToken(model.token!);
      await authManager.saveUserInfo(
        userId: model.id,
        name: model.name,
        email: model.email,
      );
    }

    return model.toEntity();
  }

  @override
  Future<User> loginWithGoogle(String idToken) async {
    final model = await remoteDataSource.loginWithGoogle(idToken);

    // Save auth data
    if (model.token != null) {
      await authManager.saveToken(model.token!);
      await authManager.saveUserInfo(
        userId: model.id,
        name: model.name,
        email: model.email,
      );
    }

    return model.toEntity();
  }

  @override
  Future<User> loginWithApple(String idToken) async {
    final model = await remoteDataSource.loginWithApple(idToken);

    // Save auth data
    if (model.token != null) {
      await authManager.saveToken(model.token!);
      await authManager.saveUserInfo(
        userId: model.id,
        name: model.name,
        email: model.email,
      );
    }

    return model.toEntity();
  }

  @override
  Future<User> getCurrentUser() async {
    final model = await remoteDataSource.getCurrentUser();
    return model.toEntity();
  }

  @override
  Future<void> logout() async {
    await remoteDataSource.logout();
    await authManager.clearAuth();
  }

  @override
  Future<bool> isAuthenticated() async {
    return await authManager.isAuthenticated();
  }
}