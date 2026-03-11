import '../entities/verse.dart';

abstract class VerseRepository {
  Future<List<Verse>> getVersesForIssue(int issueId);
  Future<Verse> getVerseById(int verseId);
  //Future<Verse> getRandomVerse();

}