import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../domain/entities/verse.dart';
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

// States
abstract class VersesState extends Equatable {
  @override
  List<Object?> get props => [];
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

  VersesBloc({required this.getVersesForIssue}) : super(VersesInitial()) {
    on<LoadVersesForIssueEvent>(_onLoadVersesForIssue);
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
}