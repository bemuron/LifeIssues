import '../../domain/entities/verse.dart';
import '../../domain/repositories/daily_verse_repository.dart';
import '../datasources/daily_verse_local_datasource.dart';

class DailyVerseRepositoryImpl implements DailyVerseRepository {
  final DailyVerseLocalDataSource localDataSource;

  DailyVerseRepositoryImpl({required this.localDataSource});

  @override
  Future<Verse> getDailyVerse() async {
    final model = await localDataSource.getDailyVerse();
    return model.toEntity();
  }

  @override
  Future<Verse> getRandomVerse() async {
    final model = await localDataSource.getRandomVerse();
    return model.toEntity();
  }
}