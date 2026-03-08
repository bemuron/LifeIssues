import '../entities/verse.dart';
import '../repositories/favorites_repository.dart';

class GetFavoriteVerses {
  final FavoritesRepository repository;

  GetFavoriteVerses(this.repository);

  Future<List<Verse>> call() async {
    return await repository.getFavoriteVerses();
  }
}

class ToggleFavorite {
  final FavoritesRepository repository;

  ToggleFavorite(this.repository);

  Future<void> call(int verseId, bool isFavorite) async {
    return await repository.toggleFavorite(verseId, isFavorite);
  }
}