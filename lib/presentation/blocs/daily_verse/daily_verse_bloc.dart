import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../domain/entities/verse.dart';
import '../../../domain/usecases/get_daily_verse.dart';
import '../../../domain/usecases/get_random_verse.dart';
import '../../widgets/home_screen_widget_provider.dart';

// Events
abstract class DailyVerseEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

// bibleVersion is passed in by the caller — exactly like DailyVerseCard
// reads settingsState.bibleVersion from SettingsBloc and uses it.
// Defaults to 'kjv' so existing call sites need no changes.
class LoadDailyVerseEvent extends DailyVerseEvent {
  final String bibleVersion;

  LoadDailyVerseEvent({this.bibleVersion = 'kjv'});

  @override
  List<Object?> get props => [bibleVersion];
}

class LoadRandomVerseEvent extends DailyVerseEvent {}

// States
abstract class DailyVerseState extends Equatable {
  @override
  List<Object?> get props => [];
}

class DailyVerseInitial extends DailyVerseState {}

class DailyVerseLoading extends DailyVerseState {}

class DailyVerseLoaded extends DailyVerseState {
  final Verse verse;
  final bool isRandom;

  DailyVerseLoaded(this.verse, {this.isRandom = false});

  @override
  List<Object?> get props => [verse, isRandom];
}

class DailyVerseError extends DailyVerseState {
  final String message;

  DailyVerseError(this.message);

  @override
  List<Object?> get props => [message];
}

// BLoC
class DailyVerseBloc extends Bloc<DailyVerseEvent, DailyVerseState> {
  final GetDailyVerse getDailyVerse;
  final GetRandomVerse getRandomVerse;

  DailyVerseBloc({
    required this.getDailyVerse,
    required this.getRandomVerse,
  }) : super(DailyVerseInitial()) {
    on<LoadDailyVerseEvent>(_onLoadDailyVerse);
    on<LoadRandomVerseEvent>(_onLoadRandomVerse);
  }

  Future<void> _onLoadDailyVerse(
      LoadDailyVerseEvent event,
      Emitter<DailyVerseState> emit,
      ) async {
    emit(DailyVerseLoading());
    try {
      final verse = await getDailyVerse();
      emit(DailyVerseLoaded(verse));

      // Update the home screen widget using the same version the caller
      // read from SettingsBloc — mirrors how DailyVerseCard does it:
      //   final version = settingsState is SettingsLoaded
      //       ? settingsState.bibleVersion : 'kjv';
      // Non-critical: swallow any widget update failure silently.
      try {
        await HomeScreenWidgetProvider.updateWidget(verse, event.bibleVersion);
      } catch (_) {}
    } catch (e) {
      emit(DailyVerseError('Failed to load daily verse: ${e.toString()}'));
    }
  }

  Future<void> _onLoadRandomVerse(
      LoadRandomVerseEvent event,
      Emitter<DailyVerseState> emit,
      ) async {
    emit(DailyVerseLoading());
    try {
      final verse = await getRandomVerse();
      emit(DailyVerseLoaded(verse as Verse, isRandom: true));
      // No widget update — home screen widget always shows the daily verse.
    } catch (e) {
      emit(DailyVerseError('Failed to load random verse: ${e.toString()}'));
    }
  }
}