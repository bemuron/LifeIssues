// lib/presentation/blocs/auth/auth_event.dart

import 'package:equatable/equatable.dart';
import '../../../domain/usecases/auth/social_login.dart';
import '../../../domain/entities/user.dart';

abstract class AuthEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class CheckAuthStatusEvent extends AuthEvent {}

class LoginEvent extends AuthEvent {
  final String email;
  final String password;

  LoginEvent({
    required this.email,
    required this.password,
  });

  @override
  List<Object?> get props => [email, password];
}

class RegisterEvent extends AuthEvent {
  final String name;
  final String email;
  final String password;

  RegisterEvent({
    required this.name,
    required this.email,
    required this.password,
  });

  @override
  List<Object?> get props => [name, email, password];
}

class SocialLoginEvent extends AuthEvent {
  final SocialProvider provider;
  final String idToken;

  SocialLoginEvent({
    required this.provider,
    required this.idToken,
  });

  @override
  List<Object?> get props => [provider, idToken];
}

class LogoutEvent extends AuthEvent {}

class GetCurrentUserEvent extends AuthEvent {}

class UpdateProfileEvent extends AuthEvent {
  final String name;
  final String? password;
  final String? imagePath;

  UpdateProfileEvent({required this.name, this.password, this.imagePath});

  @override
  List<Object?> get props => [name, password, imagePath];
}

/// Direct authentication event - used for social auth after backend sync
class AuthenticateDirectEvent extends AuthEvent {
  final User user;

  AuthenticateDirectEvent({required this.user});

  @override
  List<Object?> get props => [user];
}