// lib/domain/usecases/testimonies/toggle_praise.dart

import '../../repositories/testimony_repository.dart';

class TogglePraise {
  final TestimonyRepository repository;

  TogglePraise(this.repository);

  Future<Map<String, dynamic>> call(int testimonyId) async {
    if (testimonyId <= 0) {
      throw ArgumentError('Invalid testimony ID');
    }

    return await repository.togglePraise(testimonyId);
  }
}