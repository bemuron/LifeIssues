// lib/core/utils/auth_manager.dart

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthManager {
  final FlutterSecureStorage _secureStorage;
  final SharedPreferences _prefs;

  static const String _tokenKey = 'auth_token';
  static const String _userIdKey = 'user_id';
  static const String _userNameKey = 'user_name';
  static const String _userEmailKey = 'user_email';
  static const String _isAuthenticatedKey = 'is_authenticated';

  AuthManager(this._secureStorage, this._prefs);

  // Token management
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

  // User info management
  Future<void> saveUserInfo({
    required int userId,
    required String name,
    required String email,
  }) async {
    await _prefs.setInt(_userIdKey, userId);
    await _prefs.setString(_userNameKey, name);
    await _prefs.setString(_userEmailKey, email);
  }

  Future<int?> getUserId() async {
    return _prefs.getInt(_userIdKey);
  }

  Future<String?> getUserName() async {
    return _prefs.getString(_userNameKey);
  }

  Future<String?> getUserEmail() async {
    return _prefs.getString(_userEmailKey);
  }

  // Auth status
  Future<bool> isAuthenticated() async {
    final hasToken = await getToken();
    return hasToken != null && hasToken.isNotEmpty;
  }

  // Clear all auth data
  Future<void> clearAuth() async {
    await deleteToken();
    await _prefs.remove(_userIdKey);
    await _prefs.remove(_userNameKey);
    await _prefs.remove(_userEmailKey);
    await _prefs.setBool(_isAuthenticatedKey, false);
  }

  // Get complete user data as map
  Future<Map<String, dynamic>?> getUserData() async {
    final userId = await getUserId();
    final name = await getUserName();
    final email = await getUserEmail();

    if (userId == null || name == null || email == null) {
      return null;
    }

    return {
      'id': userId,
      'name': name,
      'email': email,
    };
  }
}