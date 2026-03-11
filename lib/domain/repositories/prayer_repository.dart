// lib/domain/repositories/prayer_repository.dart

import '../entities/prayer.dart';

abstract class PrayerRepository {
  Future<List<Prayer>> getPrayers({int page = 1, String? category});
  Future<Prayer> getPrayerById(int id);
  Future<Prayer> submitPrayer({
    required String body,
    String? category,
    bool isAnonymous = false,
  });
  Future<Map<String, dynamic>> togglePraying(int prayerId);
  Future<void> deletePrayer(int prayerId);
  Future<List<Prayer>> getMyPrayers({int page = 1});
}