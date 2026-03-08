import '../entities/verse.dart';

abstract class DailyVerseRepository {
  Future<Verse> getDailyVerse();
  Future<Verse> getRandomVerse();
}