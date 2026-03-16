// lib/presentation/blocs/auth/auth_bloc.dart
import 'dart:io';
import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../domain/usecases/auth/login.dart';
import '../../../domain/usecases/auth/register.dart';
import '../../../domain/usecases/auth/social_login.dart';
import '../../../domain/usecases/auth/logout.dart';
import '../../../domain/usecases/auth/check_auth_status.dart';
import '../../../domain/repositories/auth_repository.dart';
import 'auth_event.dart';
import 'auth_state.dart';

class AuthBloc extends Bloc<AuthEvent, AuthState> {
  final Login login;
  final Register register;
  final SocialLogin socialLogin;
  final Logout logout;
  final CheckAuthStatus checkAuthStatus;
  final AuthRepository authRepository;

  AuthBloc({
    required this.login,
    required this.register,
    required this.socialLogin,
    required this.logout,
    required this.checkAuthStatus,
    required this.authRepository,
  }) : super(AuthInitial()) {
    on<CheckAuthStatusEvent>(_onCheckAuthStatus);
    on<LoginEvent>(_onLogin);
    on<RegisterEvent>(_onRegister);
    on<SocialLoginEvent>(_onSocialLogin);
    on<LogoutEvent>(_onLogout);
    on<GetCurrentUserEvent>(_onGetCurrentUser);
  }

  Future<void> _onCheckAuthStatus(
      CheckAuthStatusEvent event,
      Emitter<AuthState> emit,
      ) async {
    try {
      emit(AuthCheckingStatus());
      final isAuthenticated = await checkAuthStatus();
      if (isAuthenticated) {
        final user = await authRepository.getCurrentUser();
        emit(Authenticated(user));
      } else {
        emit(Unauthenticated());
      }
    } catch (e) {
      // Auth check is silent — just fall through to unauthenticated
      emit(Unauthenticated());
    }
  }

  Future<void> _onLogin(
      LoginEvent event,
      Emitter<AuthState> emit,
      ) async {
    try {
      emit(AuthLoggingIn());
      final user = await login(
        email: event.email,
        password: event.password,
      );
      emit(Authenticated(user));
    } on ArgumentError catch (e) {
      // ArgumentError carries a structured validation message — use it directly
      emit(AuthError(e.message));
      emit(Unauthenticated());
    } catch (e) {
      emit(AuthError(_friendlyError(e)));
      emit(Unauthenticated());
    }
  }

  Future<void> _onRegister(
      RegisterEvent event,
      Emitter<AuthState> emit,
      ) async {
    try {
      emit(AuthRegistering());
      final user = await register(
        name: event.name,
        email: event.email,
        password: event.password,
      );
      emit(Authenticated(user));
    } on ArgumentError catch (e) {
      emit(AuthError(e.message));
      emit(Unauthenticated());
    } catch (e) {
      emit(AuthError(_friendlyError(e)));
      emit(Unauthenticated());
    }
  }

  Future<void> _onSocialLogin(
      SocialLoginEvent event,
      Emitter<AuthState> emit,
      ) async {
    try {
      emit(AuthSocialLoggingIn());
      final user = await socialLogin(
        provider: event.provider,
        idToken: event.idToken,
      );
      emit(Authenticated(user));
    } catch (e) {
      emit(AuthError(_friendlyError(e)));
      emit(Unauthenticated());
    }
  }

  Future<void> _onLogout(
      LogoutEvent event,
      Emitter<AuthState> emit,
      ) async {
    try {
      emit(AuthLoading());
      await logout();
      emit(AuthLoggedOut());
      emit(Unauthenticated());
    } catch (e) {
      emit(AuthError(_friendlyError(e)));
    }
  }

  Future<void> _onGetCurrentUser(
      GetCurrentUserEvent event,
      Emitter<AuthState> emit,
      ) async {
    try {
      emit(AuthLoading());
      final user = await authRepository.getCurrentUser();
      emit(Authenticated(user));
    } catch (e) {
      emit(AuthError(_friendlyError(e)));
      emit(Unauthenticated());
    }
  }

  // ---------------------------------------------------------------------------
  // Converts raw exceptions into user-friendly messages so the UI never
  // shows "Unknown error occurred" or raw Dart exception strings.
  // ---------------------------------------------------------------------------
  String _friendlyError(Object e) {
    // No internet / DNS failure
    if (e is SocketException) {
      return 'No internet connection. Please check your network and try again.';
    }
    // SSL/TLS handshake failure — often a captive portal intercepting HTTPS
    if (e is HandshakeException) {
      return 'Secure connection failed. Please check your network and try again.';
    }
    // Low-level HTTP error from dart:io
    if (e is HttpException) {
      return 'Could not reach the server. Please try again later.';
    }
    // Server returned non-JSON (e.g. HTML error page from captive portal or
    // proxy) — this is the most common cause of "Unknown error" when data is off
    if (e is FormatException) {
      return 'Received an unexpected response. Please check your connection.';
    }
    // Any other exception — use its own message, stripping the "Exception: " prefix
    final msg = e.toString();
    if (msg.isNotEmpty && msg != 'null') {
      return msg.replaceFirst(RegExp(r'^Exception:\s*'), '');
    }
    return 'Something went wrong. Please try again.';
  }
}