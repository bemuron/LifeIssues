import '../../domain/entities/verse.dart';
import '../../domain/repositories/verse_repository.dart';
import '../datasources/verse_local_datasource.dart';

class VerseRepositoryImpl implements VerseRepository {
  final VerseLocalDataSource localDataSource;

  VerseRepositoryImpl({required this.localDataSource});

  @override
  Future<List<Verse>> getVersesForIssue(int issueId) async {
    final models = await localDataSource.getVersesForIssue(issueId);
    return models.map((model) => model.toEntity()).toList();
  }

  @override
  Future<Verse> getVerseById(int verseId) async {
    final model = await localDataSource.getVerseById(verseId);
    return model.toEntity();
  }

  /*@override
  Future<Verse> getRandomVerse() async {
    final model = await localDataSource.getRandomVerse();
    return model.toEntity();
  }*/
}