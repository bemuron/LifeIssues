import 'package:flutter/material.dart';
import '../../domain/repositories/settings_repository.dart';
import '../datasources/settings_local_datasource.dart';

class SettingsRepositoryImpl implements SettingsRepository {
  final SettingsLocalDataSource localDataSource;

  SettingsRepositoryImpl({required this.localDataSource});

  @override
  Future<Map<String, dynamic>> getSettings() async {
    return await localDataSource.getSettings();
  }

  @override
  Future<void> updateBibleVersion(String version) async {
    return await localDataSource.saveBibleVersion(version);
  }

  @override
  Future<bool> toggleNotifications() async {
    return await localDataSource.toggleNotifications();
  }

  @override
  Future<void> updateNotificationTime(TimeOfDay time) async {
    return await localDataSource.saveNotificationTime(time);
  }

  @override
  Future<bool> toggleTheme() async {
    return await localDataSource.toggleTheme();
  }
}