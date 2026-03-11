// lib/domain/usecases/testimonies/get_my_testimonies.dart

import '../../entities/testimony.dart';
import '../../repositories/testimony_repository.dart';

class GetMyTestimonies {
  final TestimonyRepository repository;

  GetMyTestimonies(this.repository);

  Future<List<Testimony>> call({int page = 1}) async {
    return await repository.getMyTestimonies(page: page);
  }
}