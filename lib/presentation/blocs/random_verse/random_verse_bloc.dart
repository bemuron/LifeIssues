// lib/presentation/blocs/random_verse/random_verse_bloc.dart
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../domain/entities/verse.dart';
import '../../../domain/usecases/get_random_verse.dart';

// Events
abstract class RandomVerseEvent extends Equatable {
  const RandomVerseEvent();

  @override
  List<Object?> get props => [];
}

class LoadRandomVerse extends RandomVerseEvent {}

// States
abstract class RandomVerseState extends Equatable {
  const RandomVerseState();

  @override
  List<Object?> get props => [];
}

class RandomVerseInitial extends RandomVerseState {}

class RandomVerseLoading extends RandomVerseState {}

class RandomVerseLoaded extends RandomVerseState {
  final Verse verse;

  const RandomVerseLoaded({required this.verse});

  @override
  List<Object?> get props => [verse];
}

class RandomVerseError extends RandomVerseState {
  final String message;

  const RandomVerseError({required this.message});

  @override
  List<Object?> get props => [message];
}

// BLoC
class RandomVerseBloc extends Bloc<RandomVerseEvent, RandomVerseState> {
  final GetRandomVerse getRandomVerse;

  RandomVerseBloc({required this.getRandomVerse}) : super(RandomVerseInitial()) {
    on<LoadRandomVerse>(_onLoadRandomVerse);
  }

  /*Future<void> _onLoadRandomVerse(
      LoadRandomVerse event,
      Emitter<RandomVerseState> emit,
      ) async {
    emit(RandomVerseLoading());

    final result = await getRandomVerse();

    result.fold(
          (failure) => emit(RandomVerseError(message: 'Failed to load random verse')),
          (verse) => emit(RandomVerseLoaded(verse: verse)),
    );
  }*/

  Future<void> _onLoadRandomVerse(
      LoadRandomVerse event,
      Emitter<RandomVerseState> emit,
      ) async {
    emit(RandomVerseLoading());
    try {
      final verse = await getRandomVerse();
      emit(RandomVerseLoaded(verse: verse));
    } catch (e) {
      emit(RandomVerseError(message: 'Failed to load random verse: ${e.toString()}'));
    }
  }

}