import 'package:flutter/material.dart';
import '../repositories/settings_repository.dart';

class GetSettings {
  final SettingsRepository repository;

  GetSettings(this.repository);

  Future<Map<String, dynamic>> call() async {
    return await repository.getSettings();
  }
}

class UpdateBibleVersion {
  final SettingsRepository repository;

  UpdateBibleVersion(this.repository);

  Future<void> call(String version) async {
    return await repository.updateBibleVersion(version);
  }
}

class ToggleNotifications {
  final SettingsRepository repository;

  ToggleNotifications(this.repository);

  Future<bool> call() async {
    return await repository.toggleNotifications();
  }
}

class UpdateNotificationTime {
  final SettingsRepository repository;

  UpdateNotificationTime(this.repository);

  Future<void> call(TimeOfDay time) async {
    return await repository.updateNotificationTime(time);
  }
}

class ToggleTheme {
  final SettingsRepository repository;

  ToggleTheme(this.repository);

  Future<bool> call() async {
    return await repository.toggleTheme();
  }
}