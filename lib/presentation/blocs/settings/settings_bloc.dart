import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:equatable/equatable.dart';
import '../../../domain/usecases/settings_usecases.dart';

// Events
abstract class SettingsEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadSettingsEvent extends SettingsEvent {}

class UpdateBibleVersionEvent extends SettingsEvent {
  final String version;

  UpdateBibleVersionEvent(this.version);

  @override
  List<Object?> get props => [version];
}

class ToggleNotificationsEvent extends SettingsEvent {}

class UpdateNotificationTimeEvent extends SettingsEvent {
  final TimeOfDay time;

  UpdateNotificationTimeEvent(this.time);

  @override
  List<Object?> get props => [time];
}

class ToggleThemeEvent extends SettingsEvent {}

// States
abstract class SettingsState extends Equatable {
  @override
  List<Object?> get props => [];
}

class SettingsInitial extends SettingsState {}

class SettingsLoading extends SettingsState {}

class SettingsLoaded extends SettingsState {
  final String bibleVersion;
  final bool notificationsEnabled;
  final TimeOfDay notificationTime;
  final bool isDarkMode;

  SettingsLoaded({
    required this.bibleVersion,
    required this.notificationsEnabled,
    required this.notificationTime,
    required this.isDarkMode,
  });

  @override
  List<Object?> get props => [
    bibleVersion,
    notificationsEnabled,
    notificationTime,
    isDarkMode,
  ];

  SettingsLoaded copyWith({
    String? bibleVersion,
    bool? notificationsEnabled,
    TimeOfDay? notificationTime,
    bool? isDarkMode,
  }) {
    return SettingsLoaded(
      bibleVersion: bibleVersion ?? this.bibleVersion,
      notificationsEnabled: notificationsEnabled ?? this.notificationsEnabled,
      notificationTime: notificationTime ?? this.notificationTime,
      isDarkMode: isDarkMode ?? this.isDarkMode,
    );
  }
}

class SettingsError extends SettingsState {
  final String message;

  SettingsError(this.message);

  @override
  List<Object?> get props => [message];
}

// BLoC
class SettingsBloc extends Bloc<SettingsEvent, SettingsState> {
  final GetSettings getSettings;
  final UpdateBibleVersion updateBibleVersion;
  final ToggleNotifications toggleNotifications;
  final UpdateNotificationTime updateNotificationTime;
  final ToggleTheme toggleTheme;

  SettingsBloc({
    required this.getSettings,
    required this.updateBibleVersion,
    required this.toggleNotifications,
    required this.updateNotificationTime,
    required this.toggleTheme,
  }) : super(SettingsInitial()) {
    on<LoadSettingsEvent>(_onLoadSettings);
    on<UpdateBibleVersionEvent>(_onUpdateBibleVersion);
    on<ToggleNotificationsEvent>(_onToggleNotifications);
    on<UpdateNotificationTimeEvent>(_onUpdateNotificationTime);
    on<ToggleThemeEvent>(_onToggleTheme);
  }

  Future<void> _onLoadSettings(
      LoadSettingsEvent event,
      Emitter<SettingsState> emit,
      ) async {
    emit(SettingsLoading());
    try {
      final settings = await getSettings();
      emit(SettingsLoaded(
        bibleVersion: settings['bibleVersion'] as String,
        notificationsEnabled: settings['notificationsEnabled'] as bool,
        notificationTime: settings['notificationTime'] as TimeOfDay,
        isDarkMode: settings['isDarkMode'] as bool,
      ));
    } catch (e) {
      emit(SettingsError('Failed to load settings: ${e.toString()}'));
    }
  }

  Future<void> _onUpdateBibleVersion(
      UpdateBibleVersionEvent event,
      Emitter<SettingsState> emit,
      ) async {
    if (state is SettingsLoaded) {
      try {
        await updateBibleVersion(event.version);
        emit((state as SettingsLoaded).copyWith(bibleVersion: event.version));
      } catch (e) {
        emit(SettingsError('Failed to update Bible version: ${e.toString()}'));
      }
    }
  }

  Future<void> _onToggleNotifications(
      ToggleNotificationsEvent event,
      Emitter<SettingsState> emit,
      ) async {
    if (state is SettingsLoaded) {
      try {
        final enabled = await toggleNotifications();
        emit((state as SettingsLoaded).copyWith(notificationsEnabled: enabled));
      } catch (e) {
        emit(SettingsError('Failed to toggle notifications: ${e.toString()}'));
      }
    }
  }

  Future<void> _onUpdateNotificationTime(
      UpdateNotificationTimeEvent event,
      Emitter<SettingsState> emit,
      ) async {
    if (state is SettingsLoaded) {
      try {
        await updateNotificationTime(event.time);
        emit((state as SettingsLoaded).copyWith(notificationTime: event.time));
      } catch (e) {
        emit(SettingsError('Failed to update notification time: ${e.toString()}'));
      }
    }
  }

  Future<void> _onToggleTheme(
      ToggleThemeEvent event,
      Emitter<SettingsState> emit,
      ) async {
    if (state is SettingsLoaded) {
      try {
        final isDark = await toggleTheme();
        emit((state as SettingsLoaded).copyWith(isDarkMode: isDark));
      } catch (e) {
        emit(SettingsError('Failed to toggle theme: ${e.toString()}'));
      }
    }
  }
}