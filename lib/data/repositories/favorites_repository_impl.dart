import '../../domain/entities/verse.dart';
import '../../domain/repositories/favorites_repository.dart';
import '../datasources/favorites_local_datasource.dart';
import '../models/verse_model.dart';

class FavoritesRepositoryImpl implements FavoritesRepository {
  final FavoritesLocalDataSource localDataSource;

  FavoritesRepositoryImpl({required this.localDataSource});

  @override
  Future<List<Verse>> getFavoriteVerses() async {
    final models = await localDataSource.getFavoriteVerses();
    return models.map((model) => model.toEntity()).toList();
  }

  @override
  Future<void> toggleFavorite(int verseId, bool isFavorite) async {
    return await localDataSource.toggleFavorite(verseId, isFavorite);
  }
}