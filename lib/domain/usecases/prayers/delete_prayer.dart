// lib/domain/usecases/prayers/delete_prayer.dart

import '../../repositories/prayer_repository.dart';

class DeletePrayer {
  final PrayerRepository repository;

  DeletePrayer(this.repository);

  Future<void> call(int prayerId) async {
    if (prayerId <= 0) {
      throw ArgumentError('Invalid prayer ID');
    }

    await repository.deletePrayer(prayerId);
  }
}