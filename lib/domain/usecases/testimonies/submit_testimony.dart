// lib/domain/usecases/testimonies/submit_testimony.dart

import '../../entities/testimony.dart';
import '../../repositories/testimony_repository.dart';

class SubmitTestimony {
  final TestimonyRepository repository;

  SubmitTestimony(this.repository);

  Future<Testimony> call({
    required String title,
    required String body,
    String? category,
    int? prayerId,
  }) async {
    // Validation
    if (title.trim().isEmpty) {
      throw ArgumentError('Testimony title cannot be empty');
    }

    if (title.trim().length < 5) {
      throw ArgumentError('Title must be at least 5 characters');
    }

    if (title.length > 120) {
      throw ArgumentError('Title must be less than 120 characters');
    }

    if (body.trim().isEmpty) {
      throw ArgumentError('Testimony body cannot be empty');
    }

    if (body.trim().length < 20) {
      throw ArgumentError('Testimony must be at least 20 characters');
    }

    if (body.length > 2000) {
      throw ArgumentError('Testimony must be less than 2000 characters');
    }

    return await repository.submitTestimony(
      title: title.trim(),
      body: body.trim(),
      category: category,
      prayerId: prayerId,
    );
  }
}