// lib/domain/usecases/testimonies/get_testimony_by_id.dart

import '../../entities/testimony.dart';
import '../../repositories/testimony_repository.dart';

class GetTestimonyById {
  final TestimonyRepository repository;

  GetTestimonyById(this.repository);

  Future<Testimony> call(int id) async {
    return await repository.getTestimonyById(id);
  }
}