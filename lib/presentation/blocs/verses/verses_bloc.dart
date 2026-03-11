import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../domain/entities/verse.dart';
import '../../../domain/usecases/get_random_verse_for_home.dart';
import '../../../domain/usecases/get_verses_for_issue.dart';

// Events
abstract class VersesEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadVersesForIssueEvent extends VersesEvent {
  final int issueId;

  LoadVersesForIssueEvent(this.issueId);

  @override
  List<Object?> get props => [issueId];
}

class LoadRandomVerseForHomeEvent extends VersesEvent {}

// States
abstract class VersesState extends Equatable {
  @override
  List<Object?> get props => [];
}

class RandomVerseForHomeLoaded extends VersesState {
  final Verse verse;
  final bool isRandom;

  RandomVerseForHomeLoaded(this.verse, {this.isRandom = false});

  @override
  List<Object?> get props => [verse, isRandom];
}

class VersesInitial extends VersesState {}

class VersesLoading extends VersesState {}

class VersesLoaded extends VersesState {
  final List<Verse> verses;

  VersesLoaded(this.verses);

  @override
  List<Object?> get props => [verses];
}

class VersesError extends VersesState {
  final String message;

  VersesError(this.message);

  @override
  List<Object?> get props => [message];
}

// BLoC
class VersesBloc extends Bloc<VersesEvent, VersesState> {
  final GetVersesForIssue getVersesForIssue;
  final GetRandomVerseForHome getRandomVerseForHome;

  VersesBloc({
    required this.getVersesForIssue,
    required this.getRandomVerseForHome,
  }) : super(VersesInitial()) {
    on<LoadVersesForIssueEvent>(_onLoadVersesForIssue);
    on<LoadRandomVerseForHomeEvent>(_onLoadRandomVerse);
  }

  Future<void> _onLoadVersesForIssue(
      LoadVersesForIssueEvent event,
      Emitter<VersesState> emit,
      ) async {
    emit(VersesLoading());
    try {
      final verses = await getVersesForIssue(event.issueId);
      emit(VersesLoaded(verses));
    } catch (e) {
      emit(VersesError('Failed to load verses: ${e.toString()}'));
    }
  }

  Future<void> _onLoadRandomVerse(
      LoadRandomVerseForHomeEvent event,
      Emitter<VersesState> emit,
      ) async {
    emit(VersesLoading());
    try {
      final verse = await getRandomVerseForHome();
      emit(RandomVerseForHomeLoaded(verse as Verse, isRandom: true) as VersesState);
    } catch (e) {
      emit(VersesError('Failed to load random verse: ${e.toString()}'));
    }
  }
}