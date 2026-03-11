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
  Future<void> logout();
  Future<bool> isAuthenticated();
}