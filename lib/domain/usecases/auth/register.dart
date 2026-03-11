// lib/domain/usecases/auth/register.dart

import '../../entities/user.dart';
import '../../repositories/auth_repository.dart';

class Register {
  final AuthRepository repository;

  Register(this.repository);

  Future<User> call({
    required String name,
    required String email,
    required String password,
  }) async {
    // Validation
    if (name.trim().isEmpty) {
      throw ArgumentError('Name cannot be empty');
    }

    if (name.trim().length < 2) {
      throw ArgumentError('Name must be at least 2 characters');
    }

    if (email.trim().isEmpty) {
      throw ArgumentError('Email cannot be empty');
    }

    if (!_isValidEmail(email)) {
      throw ArgumentError('Invalid email format');
    }

    if (password.isEmpty) {
      throw ArgumentError('Password cannot be empty');
    }

    if (password.length < 8) {
      throw ArgumentError('Password must be at least 8 characters');
    }

    return await repository.register(
      name: name.trim(),
      email: email.trim().toLowerCase(),
      password: password,
    );
  }

  bool _isValidEmail(String email) {
    final emailRegex = RegExp(
      r'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$',
    );
    return emailRegex.hasMatch(email);
  }
}