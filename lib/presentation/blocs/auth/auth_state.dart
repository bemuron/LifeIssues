// lib/presentation/blocs/auth/auth_state.dart

import 'package:equatable/equatable.dart';
import '../../../domain/entities/user.dart';

abstract class AuthState extends Equatable {
  @override
  List<Object?> get props => [];
}

class AuthInitial extends AuthState {}

class AuthLoading extends AuthState {}

class AuthCheckingStatus extends AuthState {}

class Authenticated extends AuthState {
  final User user;

  Authenticated(this.user);

  @override
  List<Object?> get props => [user];
}

class Unauthenticated extends AuthState {}

class AuthLoggingIn extends AuthState {}

class AuthRegistering extends AuthState {}

class AuthSocialLoggingIn extends AuthState {}

class AuthLoggedOut extends AuthState {}

class ProfileUpdating extends AuthState {}

class ProfileUpdated extends AuthState {}

class AuthError extends AuthState {
  final String message;

  AuthError(this.message);

  @override
  List<Object?> get props => [message];
}