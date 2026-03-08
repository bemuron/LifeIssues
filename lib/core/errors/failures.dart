import 'package:equatable/equatable.dart';

abstract class Failure extends Equatable {
  final String message;

  const Failure(this.message);

  @override
  List<Object> get props => [message];
}

// General failures
class ServerFailure extends Failure {
  const ServerFailure([String message = 'Server error occurred'])
      : super(message);
}

class CacheFailure extends Failure {
  const CacheFailure([String message = 'Cache error occurred'])
      : super(message);
}

class NetworkFailure extends Failure {
  const NetworkFailure([String message = 'Network error occurred'])
      : super(message);
}

// Database failures
class DatabaseFailure extends Failure {
  const DatabaseFailure([String message = 'Database error occurred'])
      : super(message);
}

class DataNotFoundFailure extends Failure {
  const DataNotFoundFailure([String message = 'Data not found'])
      : super(message);
}

class InvalidDataFailure extends Failure {
  const InvalidDataFailure([String message = 'Invalid data format'])
      : super(message);
}

// Settings failures
class SettingsFailure extends Failure {
  const SettingsFailure([String message = 'Settings error occurred'])
      : super(message);
}

class PreferenceFailure extends Failure {
  const PreferenceFailure([String message = 'Failed to save preference'])
      : super(message);
}

// Verse failures
class VerseNotFoundFailure extends Failure {
  const VerseNotFoundFailure([String message = 'Verse not found'])
      : super(message);
}

class NoVersesAvailableFailure extends Failure {
  const NoVersesAvailableFailure([String message = 'No verses available'])
      : super(message);
}

// Issue failures
class IssueNotFoundFailure extends Failure {
  const IssueNotFoundFailure([String message = 'Issue not found'])
      : super(message);
}

class NoIssuesAvailableFailure extends Failure {
  const NoIssuesAvailableFailure([String message = 'No issues available'])
      : super(message);
}

// Favorite failures
class FavoriteOperationFailure extends Failure {
  const FavoriteOperationFailure(
      [String message = 'Failed to update favorite'])
      : super(message);
}

// Generic failures
class UnknownFailure extends Failure {
  const UnknownFailure([String message = 'An unknown error occurred'])
      : super(message);
}

class ValidationFailure extends Failure {
  const ValidationFailure([String message = 'Validation failed'])
      : super(message);
}

// Exception to Failure converter
Failure exceptionToFailure(Exception exception) {
  if (exception.toString().contains('database')) {
    return DatabaseFailure(exception.toString());
  } else if (exception.toString().contains('not found')) {
    return DataNotFoundFailure(exception.toString());
  } else if (exception.toString().contains('network')) {
    return NetworkFailure(exception.toString());
  } else if (exception.toString().contains('cache')) {
    return CacheFailure(exception.toString());
  } else {
    return UnknownFailure(exception.toString());
  }
}

// Extension for Failure
extension FailureExtension on Failure {
  String get displayMessage {
    if (message.length > 100) {
      return '${message.substring(0, 100)}...';
    }
    return message;
  }

  bool get isRecoverable {
    return this is! ServerFailure && this is! UnknownFailure;
  }
}