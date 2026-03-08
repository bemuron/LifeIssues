import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../domain/entities/verse.dart';
import '../../../domain/usecases/favorites_usecases.dart';

// Events
abstract class FavoritesEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadFavoritesEvent extends FavoritesEvent {}

class ToggleFavoriteEvent extends FavoritesEvent {
  final int verseId;
  final bool isFavorite;

  ToggleFavoriteEvent({
    required this.verseId,
    required this.isFavorite,
  });

  @override
  List<Object?> get props => [verseId, isFavorite];
}

// States
abstract class FavoritesState extends Equatable {
  @override
  List<Object?> get props => [];
}

class FavoritesInitial extends FavoritesState {}

class FavoritesLoading extends FavoritesState {}

class FavoritesLoaded extends FavoritesState {
  final List<Verse> favorites;

  FavoritesLoaded(this.favorites);

  @override
  List<Object?> get props => [favorites];
}

class FavoritesError extends FavoritesState {
  final String message;

  FavoritesError(this.message);

  @override
  List<Object?> get props => [message];
}

// BLoC
class FavoritesBloc extends Bloc<FavoritesEvent, FavoritesState> {
  final GetFavoriteVerses getFavoriteVerses;
  final ToggleFavorite toggleFavorite;

  FavoritesBloc({
    required this.getFavoriteVerses,
    required this.toggleFavorite,
  }) : super(FavoritesInitial()) {
    on<LoadFavoritesEvent>(_onLoadFavorites);
    on<ToggleFavoriteEvent>(_onToggleFavorite);
  }

  Future<void> _onLoadFavorites(
      LoadFavoritesEvent event,
      Emitter<FavoritesState> emit,
      ) async {
    emit(FavoritesLoading());
    try {
      final favorites = await getFavoriteVerses();
      emit(FavoritesLoaded(favorites));
    } catch (e) {
      emit(FavoritesError('Failed to load favorites: ${e.toString()}'));
    }
  }

  Future<void> _onToggleFavorite(
      ToggleFavoriteEvent event,
      Emitter<FavoritesState> emit,
      ) async {
    try {
      await toggleFavorite(event.verseId, event.isFavorite);
      // Reload favorites after toggling
      final favorites = await getFavoriteVerses();
      emit(FavoritesLoaded(favorites));
    } catch (e) {
      emit(FavoritesError('Failed to toggle favorite: ${e.toString()}'));
    }
  }
}