// lib/domain/usecases/auth/check_auth_status.dart

import '../../repositories/auth_repository.dart';

class CheckAuthStatus {
  final AuthRepository repository;

  CheckAuthStatus(this.repository);

  Future<bool> call() async {
    return await repository.isAuthenticated();
  }
}