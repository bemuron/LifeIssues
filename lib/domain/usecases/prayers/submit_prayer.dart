// lib/domain/usecases/prayers/submit_prayer.dart

import '../../entities/prayer.dart';
import '../../repositories/prayer_repository.dart';

class SubmitPrayer {
  final PrayerRepository repository;

  SubmitPrayer(this.repository);

  Future<Prayer> call({
    required String body,
    String? category,
    bool isAnonymous = false,
  }) async {
    // Validation
    if (body.trim().isEmpty) {
      throw ArgumentError('Prayer body cannot be empty');
    }

    if (body.trim().length < 10) {
      throw ArgumentError('Prayer must be at least 10 characters');
    }

    if (body.length > 500) {
      throw ArgumentError('Prayer must be less than 500 characters');
    }

    return await repository.submitPrayer(
      body: body.trim(),
      category: category,
      isAnonymous: isAnonymous,
    );
  }
}