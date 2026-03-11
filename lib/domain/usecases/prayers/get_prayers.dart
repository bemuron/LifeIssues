// lib/domain/usecases/prayers/get_prayers.dart

import '../../entities/prayer.dart';
import '../../repositories/prayer_repository.dart';

class GetPrayers {
  final PrayerRepository repository;

  GetPrayers(this.repository);

  Future<List<Prayer>> call({
    int page = 1,
    String? category,
  }) async {
    return await repository.getPrayers(
      page: page,
      category: category,
    );
  }
}