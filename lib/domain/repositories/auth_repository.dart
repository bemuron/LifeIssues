// lib/domain/repositories/auth_repository.dart

import '../entities/user.dart';

abstract class AuthRepository {
  Future<User> login({
    required String email,
    required String password,
  });

  Future<User> register({
    required String name,
    required String email,
    required String password,
  });

  Future<User> loginWithGoogle(String idToken);
  Future<User> loginWithApple(String idToken);

  Future<User> getCurrentUser();

  /// Returns a User built from locally cached data (no network call).
  /// Used as a fallback when [getCurrentUser] fails due to no connectivity.
  Future<User?> getLocalUser();

  Future<void> updateProfile({
    required String name,
    String? password,
    String? imagePath,
  });

  Future<void> logout();
  Future<bool> isAuthenticated();
}