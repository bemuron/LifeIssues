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
    // Keep local cache in sync with the latest server data
    await authManager.saveUserInfo(
      userId: model.id,
      name: model.name,
      email: model.email,
      profileImageUrl: model.profileImageUrl,
    );
    return model.toEntity();
  }

  @override
  Future<User?> getLocalUser() async {
    final data = await authManager.getUserData();
    if (data == null) return null;
    return User(
      id: data['id'] as int,
      name: data['name'] as String,
      email: data['email'] as String,
      profileImageUrl: data['profileImageUrl'] as String?,
    );
  }

  @override
  Future<void> updateProfile({
    required String name,
    String? password,
    String? imagePath,
  }) async {
    final model = await remoteDataSource.updateProfile(
      name: name,
      password: password,
      imagePath: imagePath,
    );
    // Persist updated info locally
    await authManager.saveUserInfo(
      userId: model.id,
      name: model.name,
      email: model.email,
      profileImageUrl: model.profileImageUrl,
    );
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