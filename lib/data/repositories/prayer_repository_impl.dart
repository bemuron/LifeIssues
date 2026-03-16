// lib/data/repositories/prayer_repository_impl.dart

import '../../domain/entities/prayer.dart';
import '../../domain/repositories/prayer_repository.dart';
import '../datasources/prayer_remote_data_source.dart';

class PrayerRepositoryImpl implements PrayerRepository {
  final PrayerRemoteDataSource remoteDataSource;

  PrayerRepositoryImpl({required this.remoteDataSource});

  @override
  Future<List<Prayer>> getPrayers({int page = 1, String? category, String? sortBy,
    bool? hasPrayers,}) async {
    final models = await remoteDataSource.getPrayers(
      page: page,
      category: category,
      sortBy: sortBy,
      hasPrayers: hasPrayers,
    );
    return models.map((model) => model.toEntity()).toList();
  }

  @override
  Future<Prayer> getPrayerById(int id) async {
    final model = await remoteDataSource.getPrayerById(id);
    return model.toEntity();
  }

  @override
  Future<Prayer> submitPrayer({
    required String body,
    String? category,
    bool isAnonymous = false,
  }) async {
    final model = await remoteDataSource.submitPrayer(
      body: body,
      category: category,
      isAnonymous: isAnonymous,
    );
    return model.toEntity();
  }

  @override
  Future<Map<String, dynamic>> togglePraying(int prayerId) async {
    return await remoteDataSource.togglePraying(prayerId);
  }

  @override
  Future<void> deletePrayer(int prayerId) async {
    await remoteDataSource.deletePrayer(prayerId);
  }

  @override
  Future<List<Prayer>> getMyPrayers({int page = 1}) async {
    final models = await remoteDataSource.getMyPrayers(page: page);
    return models.map((model) => model.toEntity()).toList();
  }
}