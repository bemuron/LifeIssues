import 'package:flutter/material.dart';

abstract class SettingsRepository {
  Future<Map<String, dynamic>> getSettings();
  Future<void> updateBibleVersion(String version);
  Future<bool> toggleNotifications();
  Future<void> updateNotificationTime(TimeOfDay time);
  Future<bool> toggleTheme();
}