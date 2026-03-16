// lib/domain/usecases/testimonies/get_testimonies.dart

import '../../entities/testimony.dart';
import '../../repositories/testimony_repository.dart';

class GetTestimonies {
  final TestimonyRepository repository;

  GetTestimonies(this.repository);

  Future<List<Testimony>> call({
    int page = 1,
    String? category,
    String? sortBy,
    bool? linkedToPrayer,
    bool? hasPraise,
  }) async {
    return await repository.getTestimonies(
      page: page,
      category: category,
      sortBy: sortBy,
      linkedToPrayer: linkedToPrayer,
      hasPraise: hasPraise,
    );
  }
}