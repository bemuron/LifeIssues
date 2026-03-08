import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../domain/entities/issue.dart';
import '../../../domain/usecases/get_issues.dart';

// Events
abstract class IssuesEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadIssuesEvent extends IssuesEvent {}

// States
abstract class IssuesState extends Equatable {
  @override
  List<Object?> get props => [];
}

class IssuesInitial extends IssuesState {}

class IssuesLoading extends IssuesState {}

class IssuesLoaded extends IssuesState {
  final List<Issue> issues;

  IssuesLoaded(this.issues);

  @override
  List<Object?> get props => [issues];
}

class IssuesError extends IssuesState {
  final String message;

  IssuesError(this.message);

  @override
  List<Object?> get props => [message];
}

// BLoC
class IssuesBloc extends Bloc<IssuesEvent, IssuesState> {
  final GetIssues getIssues;

  IssuesBloc({required this.getIssues}) : super(IssuesInitial()) {
    on<LoadIssuesEvent>(_onLoadIssues);
  }

  Future<void> _onLoadIssues(
      LoadIssuesEvent event,
      Emitter<IssuesState> emit,
      ) async {
    emit(IssuesLoading());
    try {
      final issues = await getIssues();
      emit(IssuesLoaded(issues));
    } catch (e) {
      emit(IssuesError('Failed to load issues: ${e.toString()}'));
    }
  }
}