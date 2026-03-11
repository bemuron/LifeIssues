// lib/domain/usecases/testimonies/delete_testimony.dart

import '../../repositories/testimony_repository.dart';

class DeleteTestimony {
  final TestimonyRepository repository;

  DeleteTestimony(this.repository);

  Future<void> call(int testimonyId) async {
    if (testimonyId <= 0) {
      throw ArgumentError('Invalid testimony ID');
    }

    await repository.deleteTestimony(testimonyId);
  }
}