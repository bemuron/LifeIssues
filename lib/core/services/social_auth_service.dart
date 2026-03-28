// lib/core/services/social_auth_service.dart

import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:firebase_auth/firebase_auth.dart' as firebase_auth;
import 'package:google_sign_in/google_sign_in.dart';
import '../../../domain/entities/user.dart';
import '../../../domain/repositories/auth_repository.dart';

/// Result container for auth operations
class SocialAuthResult {
  final bool success;
  final User? user;
  final String message;
  final String? idToken;

  SocialAuthResult({
    required this.success,
    this.user,
    required this.message,
    this.idToken,
  });
}

/// Handles Firebase, Google, and Apple authentication
class SocialAuthService {
  final AuthRepository authRepository;
  final firebase_auth.FirebaseAuth _firebaseAuth;
  final GoogleSignIn _googleSignIn;

  SocialAuthService({
    required this.authRepository,
  })  : _firebaseAuth = firebase_auth.FirebaseAuth.instance,
        // serverClientId is the Web OAuth client ID from google-services.json
        // (client_type: 3). Without it, googleAuth.idToken is null on Android,
        // making the Firebase GoogleAuthProvider credential incomplete.
        _googleSignIn = GoogleSignIn(
          serverClientId:
              '229607121728-ovbisd9vfsf6g2l6kmahumch4toh540i.apps.googleusercontent.com',
        );

  // ========== GOOGLE SIGN IN ==========

  /// Sign in with Google and sync with backend
  Future<SocialAuthResult> signInWithGoogle() async {
    try {
      // Check if Firebase is initialized
      if (!_isFirebaseInitialized()) {
        return SocialAuthResult(
          success: false,
          message: 'Authentication service is not available. Please try again later.',
        );
      }

      debugPrint('🔵 Starting Google Sign-In...');

      // Trigger Google Sign In
      final GoogleSignInAccount? googleUser = await _googleSignIn.signIn();

      if (googleUser == null) {
        debugPrint('⚠️ Google sign in cancelled by user');
        return SocialAuthResult(
          success: false,
          message: 'Google sign in was cancelled',
        );
      }

      debugPrint('✅ Google user selected: ${googleUser.email}');

      // Obtain auth details
      final GoogleSignInAuthentication googleAuth = await googleUser.authentication;

      debugPrint('🔑 Got Google auth tokens');

      // Create Firebase credential
      final credential = firebase_auth.GoogleAuthProvider.credential(
        accessToken: googleAuth.accessToken,
        idToken: googleAuth.idToken,
      );

      debugPrint('🔥 Signing in to Firebase...');

      // Sign in to Firebase
      final userCredential = await _firebaseAuth.signInWithCredential(credential);
      final firebaseUser = userCredential.user;

      if (firebaseUser == null) {
        debugPrint('❌ Firebase authentication failed');
        return SocialAuthResult(
          success: false,
          message: 'Firebase authentication failed',
        );
      }

      debugPrint('✅ Firebase sign-in successful: ${firebaseUser.email}');

      // Sync with backend
      return await _syncFirebaseUserWithBackend(
        firebaseUser: firebaseUser,
        authMethod: 'google',
      );
    } catch (e) {
      debugPrint('❌ Google sign in error: $e');
      return SocialAuthResult(
        success: false,
        message: 'Google sign-in failed: ${_sanitizeError(e)}',
      );
    }
  }

  // ========== APPLE SIGN IN ==========

  /// Sign in with Apple and sync with backend
  Future<SocialAuthResult> signInWithApple() async {
    try {
      // Check if Firebase is initialized
      if (!_isFirebaseInitialized()) {
        return SocialAuthResult(
          success: false,
          message: 'Authentication service is not available. Please try again later.',
        );
      }

      debugPrint('🍎 Starting Apple Sign-In...');

      final appleProvider = firebase_auth.AppleAuthProvider();
      appleProvider.addScope('email');
      appleProvider.addScope('name');

      final userCredential = await _firebaseAuth.signInWithProvider(appleProvider);
      final firebaseUser = userCredential.user;

      if (firebaseUser == null) {
        debugPrint('❌ Apple sign in failed');
        return SocialAuthResult(
          success: false,
          message: 'Apple sign in failed',
        );
      }

      debugPrint('✅ Apple sign-in successful: ${firebaseUser.email}');

      // Sync with backend
      return await _syncFirebaseUserWithBackend(
        firebaseUser: firebaseUser,
        authMethod: 'apple',
      );
    } catch (e) {
      debugPrint('❌ Apple sign in error: $e');
      return SocialAuthResult(
        success: false,
        message: 'Apple sign-in failed: ${_sanitizeError(e)}',
      );
    }
  }

  // ========== BACKEND SYNC ==========

  /// Sync Firebase user with backend
  /// First tries to login (existing user), then registers if not found
  Future<SocialAuthResult> _syncFirebaseUserWithBackend({
    required firebase_auth.User firebaseUser,
    required String authMethod,
  }) async {
    try {
      final email = firebaseUser.email ?? '';
      final name = firebaseUser.displayName ?? 'User';
      final firebaseUid = firebaseUser.uid;

      if (email.isEmpty) {
        return SocialAuthResult(
          success: false,
          message: 'Could not retrieve email from $authMethod. Please try again.',
        );
      }

      debugPrint('🔄 Syncing with backend: $email via $authMethod');

      // Use deterministic password based on Firebase UID
      // This allows the user to login again if they reinstall the app
      final password = 'firebase_${firebaseUid}_$authMethod';

      // First, try to login (user might already exist)
      debugPrint('🔐 Attempting login with existing account...');

      try {
        final user = await authRepository.login(
          email: email,
          password: password,
        );
        debugPrint('✅ Login successful - user exists in backend');
        return SocialAuthResult(
          success: true,
          user: user,
          message: 'Signed in successfully',
        );
      } catch (loginError) {
        debugPrint('⚠️ Login failed: $loginError, attempting registration...');

        // If login fails, register the user
        debugPrint('📝 Registering new user...');

        try {
          final user = await authRepository.register(
            name: name,
            email: email,
            password: password,
          );
          debugPrint('✅ Registration and login successful');
          return SocialAuthResult(
            success: true,
            user: user,
            message: 'Account created and signed in successfully',
          );
        } catch (registerError) {
          debugPrint('❌ Registration failed: $registerError');
          return SocialAuthResult(
            success: false,
            message: 'Could not create account. Please try again or contact support.',
          );
        }
      }
    } catch (e) {
      debugPrint('💥 Exception in _syncFirebaseUserWithBackend: $e');
      return SocialAuthResult(
        success: false,
        message: 'An unexpected error occurred. Please try again.',
      );
    }
  }

  // ========== LOGOUT ==========

  /// Sign out from all sources: Firebase, Google, and local storage
  Future<void> logout() async {
    try {
      debugPrint('🚪 Logging out...');
      
      // Sign out from Firebase only if initialized
      if (_isFirebaseInitialized()) {
        try {
          await _firebaseAuth.signOut();
          debugPrint('✅ Firebase signed out');
        } catch (e) {
          debugPrint('⚠️ Firebase signOut failed: $e');
        }
      }
      
      // Try to sign out from Google
      try {
        await _googleSignIn.signOut();
        debugPrint('✅ Google signed out');
      } catch (e) {
        debugPrint('⚠️ Google signOut failed: $e');
      }
      
      // Repository handles clearing local storage
      await authRepository.logout();
      debugPrint('✅ Local auth cleared');
    } catch (e) {
      debugPrint('⚠️ Error during logout: $e');
      // Still clear local auth even if Firebase/Google fail
      try {
        await authRepository.logout();
      } catch (_) {
        // Ignore
      }
    }
  }

  // ========== HELPERS ==========

  /// Check if Firebase is initialized
  bool _isFirebaseInitialized() {
    try {
      Firebase.app();
      return true;
    } catch (e) {
      debugPrint('⚠️ Firebase not initialized: $e');
      return false;
    }
  }

  /// Convert exceptions to user-friendly messages
  String _sanitizeError(Object error) {
    if (error is firebase_auth.FirebaseAuthException) {
      switch (error.code) {
        case 'account-exists-with-different-credential':
          return 'This email is already registered with a different method.';
        case 'invalid-credential':
          return 'Invalid credentials. Please try again.';
        case 'operation-not-allowed':
          return 'This sign-in method is not available.';
        case 'user-disabled':
          return 'This account has been disabled.';
        case 'user-not-found':
          return 'User not found.';
        case 'wrong-password':
          return 'Wrong password.';
        case 'invalid-email':
          return 'Invalid email address.';
        case 'network-request-failed':
          return 'Network error. Please check your connection.';
        default:
          return error.message ?? 'Authentication failed.';
      }
    }

    // PlatformException from google_sign_in — ApiException:10 is DEVELOPER_ERROR,
    // meaning the SHA-1 fingerprint of this APK is not registered in Firebase.
    if (error is PlatformException) {
      final msg = error.message ?? '';
      if (msg.contains('ApiException: 10') || msg.contains('DEVELOPER_ERROR')) {
        return 'Google Sign-In is not configured for this build. '
            'The app\'s SHA-1 fingerprint must be registered in Firebase Console.';
      }
      if (error.code == 'sign_in_canceled' || error.code == 'canceled') {
        return 'Sign-in was cancelled.';
      }
      if (msg.contains('network') || msg.contains('NETWORK_ERROR')) {
        return 'Network error. Please check your connection.';
      }
      return msg.isNotEmpty ? msg : 'Sign-in failed. Please try again.';
    }

    return error.toString().split(':').last.trim();
  }

  /// Check if user is already authenticated
  Future<bool> isAuthenticated() async {
    try {
      return await authRepository.isAuthenticated();
    } catch (e) {
      debugPrint('⚠️ Error checking authentication: $e');
      return false;
    }
  }

  /// Get current Firebase user (safe if Firebase not initialized)
  firebase_auth.User? getCurrentFirebaseUser() {
    try {
      if (_isFirebaseInitialized()) {
        return _firebaseAuth.currentUser;
      }
    } catch (e) {
      debugPrint('⚠️ Error getting Firebase user: $e');
    }
    return null;
  }
}
