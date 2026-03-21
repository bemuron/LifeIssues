// lib/core/utils/auth_manager.dart
//
// All sensitive user data (token, profile info) is stored in FlutterSecureStorage,
// which is backed by Android Keystore / iOS Keychain and is encrypted at rest.
// SharedPreferences is used only for non-sensitive app preferences.

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthManager {
  final FlutterSecureStorage _secureStorage;
  final SharedPreferences _prefs;

  // Keys stored in encrypted FlutterSecureStorage
  static const String _tokenKey = 'auth_token';
  static const String _userIdKey = 'auth_user_id';
  static const String _userNameKey = 'auth_user_name';
  static const String _userEmailKey = 'auth_user_email';
  static const String _profileImageUrlKey = 'auth_profile_image_url';

  // Keys stored in plain SharedPreferences (non-sensitive)
  static const String _isAuthenticatedKey = 'is_authenticated';

  AuthManager(this._secureStorage, this._prefs);

  // ── Token ──────────────────────────────────────────────────────────────────

  Future<void> saveToken(String token) async {
    await _secureStorage.write(key: _tokenKey, value: token);
    await _prefs.setBool(_isAuthenticatedKey, true);
  }

  Future<String?> getToken() async {
    return await _secureStorage.read(key: _tokenKey);
  }

  Future<void> deleteToken() async {
    await _secureStorage.delete(key: _tokenKey);
    await _prefs.setBool(_isAuthenticatedKey, false);
  }

  // ── User info (stored in secure storage) ───────────────────────────────────

  Future<void> saveUserInfo({
    required int userId,
    required String name,
    required String email,
    String? profileImageUrl,
  }) async {
    await _secureStorage.write(key: _userIdKey, value: userId.toString());
    await _secureStorage.write(key: _userNameKey, value: name);
    await _secureStorage.write(key: _userEmailKey, value: email);
    if (profileImageUrl != null) {
      await _secureStorage.write(
          key: _profileImageUrlKey, value: profileImageUrl);
    }
  }

  Future<int?> getUserId() async {
    final value = await _secureStorage.read(key: _userIdKey);
    return value != null ? int.tryParse(value) : null;
  }

  Future<String?> getUserName() async {
    return await _secureStorage.read(key: _userNameKey);
  }

  Future<String?> getUserEmail() async {
    return await _secureStorage.read(key: _userEmailKey);
  }

  Future<String?> getProfileImageUrl() async {
    return await _secureStorage.read(key: _profileImageUrlKey);
  }

  // ── Auth status ────────────────────────────────────────────────────────────

  /// Checks whether a valid token exists in secure storage.
  Future<bool> isAuthenticated() async {
    final token = await getToken();
    return token != null && token.isNotEmpty;
  }

  // ── Clear ──────────────────────────────────────────────────────────────────

  Future<void> clearAuth() async {
    await _secureStorage.delete(key: _tokenKey);
    await _secureStorage.delete(key: _userIdKey);
    await _secureStorage.delete(key: _userNameKey);
    await _secureStorage.delete(key: _userEmailKey);
    await _secureStorage.delete(key: _profileImageUrlKey);
    await _prefs.setBool(_isAuthenticatedKey, false);
  }

  // ── Read all cached user data (no network) ─────────────────────────────────

  Future<Map<String, dynamic>?> getUserData() async {
    final userId = await getUserId();
    final name = await getUserName();
    final email = await getUserEmail();
    final profileImageUrl = await getProfileImageUrl();

    if (userId == null || name == null || email == null) return null;

    return {
      'id': userId,
      'name': name,
      'email': email,
      'profileImageUrl': profileImageUrl,
    };
  }
}
