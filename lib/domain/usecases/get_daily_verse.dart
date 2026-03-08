import '../entities/verse.dart';
import '../repositories/daily_verse_repository.dart';

class GetDailyVerse {
  final DailyVerseRepository repository;

  GetDailyVerse(this.repository);

  Future<Verse> call() async {
    return await repository.getDailyVerse();
  }
}