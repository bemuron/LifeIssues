// lib/domain/usecases/prayers/get_prayer_by_id.dart

import '../../entities/prayer.dart';
import '../../repositories/prayer_repository.dart';

class GetPrayerById {
  final PrayerRepository repository;

  GetPrayerById(this.repository);

  Future<Prayer> call(int id) async {
    return await repository.getPrayerById(id);
  }
}