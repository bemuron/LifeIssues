import '../entities/verse.dart';

abstract class FavoritesRepository {
  Future<List<Verse>> getFavoriteVerses();
  Future<void> toggleFavorite(int verseId, bool isFavorite);
}