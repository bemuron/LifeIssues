// lib/domain/usecases/auth/logout.dart

import '../../repositories/auth_repository.dart';

class Logout {
  final AuthRepository repository;

  Logout(this.repository);

  Future<void> call() async {
    await repository.logout();
  }
}