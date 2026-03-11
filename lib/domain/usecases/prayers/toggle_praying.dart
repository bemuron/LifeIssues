// lib/domain/usecases/prayers/toggle_praying.dart

import '../../repositories/prayer_repository.dart';

class TogglePraying {
  final PrayerRepository repository;

  TogglePraying(this.repository);

  Future<Map<String, dynamic>> call(int prayerId) async {
    if (prayerId <= 0) {
      throw ArgumentError('Invalid prayer ID');
    }

    return await repository.togglePraying(prayerId);
  }
}