// lib/domain/usecases/auth/social_login.dart

import '../../entities/user.dart';
import '../../repositories/auth_repository.dart';

enum SocialProvider { google, apple }

class SocialLogin {
  final AuthRepository repository;

  SocialLogin(this.repository);

  Future<User> call({
    required SocialProvider provider,
    required String idToken,
  }) async {
    if (idToken.trim().isEmpty) {
      throw ArgumentError('ID token cannot be empty');
    }

    switch (provider) {
      case SocialProvider.google:
        return await repository.loginWithGoogle(idToken);
      case SocialProvider.apple:
        return await repository.loginWithApple(idToken);
    }
  }
}