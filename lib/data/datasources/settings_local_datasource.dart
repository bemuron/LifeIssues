import 'package:flutter/material.dart';
import 'package:life_issues_flutter/core/services/notification_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

abstract class SettingsLocalDataSource {
  Future<Map<String, dynamic>> getSettings();
  Future<void> saveBibleVersion(String version);
  Future<bool> toggleNotifications();
  Future<void> saveNotificationTime(TimeOfDay time);
  Future<bool> toggleTheme();
}

class SettingsLocalDataSourceImpl implements SettingsLocalDataSource {
  static const String _keyBibleVersion = 'bible_version';
  static const String _keyNotificationsEnabled = 'notifications_enabled';
  static const String _keyNotificationHour = 'notification_hour';
  static const String _keyNotificationMinute = 'notification_minute';
  static const String _keyIsDarkMode = 'is_dark_mode';

  @override
  Future<Map<String, dynamic>> getSettings() async {
    try {
      final prefs = await SharedPreferences.getInstance();

      final bibleVersion = prefs.getString(_keyBibleVersion) ?? 'kjv';
      final notificationsEnabled = prefs.getBool(_keyNotificationsEnabled) ?? false;
      final notificationHour = prefs.getInt(_keyNotificationHour) ?? 9;
      final notificationMinute = prefs.getInt(_keyNotificationMinute) ?? 0;
      final isDarkMode = prefs.getBool(_keyIsDarkMode) ?? false;

      return {
        'bibleVersion': bibleVersion,
        'notificationsEnabled': notificationsEnabled,
        'notificationTime': TimeOfDay(hour: notificationHour, minute: notificationMinute),
        'isDarkMode': isDarkMode,
      };
    } catch (e) {
      throw Exception('Failed to get settings: $e');
    }
  }

  @override
  Future<void> saveBibleVersion(String version) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString(_keyBibleVersion, version);
    } catch (e) {
      throw Exception('Failed to save Bible version: $e');
    }
  }

  @override
  Future<bool> toggleNotifications() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final current = prefs.getBool(_keyNotificationsEnabled) ?? false;
      final newValue = !current;

      // Get notification time
      final hour = prefs.getInt(_keyNotificationHour) ?? 9;
      final minute = prefs.getInt(_keyNotificationMinute) ?? 0;
      final time = TimeOfDay(hour: hour, minute: minute);

      // Enable or disable notifications with NotificationService
      if (newValue) {
        final success = await NotificationHandler().enableDailyNotifications(time);
        if (!success) {
          // Permission denied or scheduling failed
          return false;
        }
      } else {
        await NotificationHandler().disableDailyNotifications();
      }

      await prefs.setBool(_keyNotificationsEnabled, newValue);
      return newValue;
    } catch (e) {
      throw Exception('Failed to toggle notifications: $e');
    }
  }

  @override
  Future<void> saveNotificationTime(TimeOfDay time) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setInt(_keyNotificationHour, time.hour);
      await prefs.setInt(_keyNotificationMinute, time.minute);

      // Update notification schedule if notifications are enabled
      final isEnabled = prefs.getBool(_keyNotificationsEnabled) ?? false;
      if (isEnabled) {
        await NotificationHandler().updateNotificationTime(time);
      }
    } catch (e) {
      throw Exception('Failed to save notification time: $e');
    }
  }

  @override
  Future<bool> toggleTheme() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final current = prefs.getBool(_keyIsDarkMode) ?? false;
      final newValue = !current;
      await prefs.setBool(_keyIsDarkMode, newValue);
      return newValue;
    } catch (e) {
      throw Exception('Failed to toggle theme: $e');
    }
  }
}