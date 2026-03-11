// lib/domain/usecases/prayers/get_my_prayers.dart

import '../../entities/prayer.dart';
import '../../repositories/prayer_repository.dart';

class GetMyPrayers {
  final PrayerRepository repository;

  GetMyPrayers(this.repository);

  Future<List<Prayer>> call({int page = 1}) async {
    return await repository.getMyPrayers(page: page);
  }
}