// lib/core/services/notification_handler.dart

import 'dart:io' show Platform;
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:timezone/timezone.dart' as tz;
import 'package:timezone/data/latest.dart' as tz;
import '../../presentation/pages/prayers/prayer_detail_page.dart';
import '../../presentation/pages/testimonies/testimony_detail_page.dart';
import '../../data/datasources/daily_verse_local_datasource.dart';
import 'notification_storage.dart';

class NotificationHandler {
  static final NotificationHandler _instance = NotificationHandler._internal();
  factory NotificationHandler() => _instance;
  NotificationHandler._internal();

  FirebaseMessaging? _firebaseMessaging;
  final FlutterLocalNotificationsPlugin _localNotifications =
  FlutterLocalNotificationsPlugin();

  NotificationStorage? _storage;

  // Navigation key for handling notifications when app is in background
  static GlobalKey<NavigatorState>? navigatorKey;

  // Daily verse notification settings keys
  static const String _notificationEnabledKey = 'daily_verse_notification';
  static const String _notificationHourKey = 'notification_hour';
  static const String _notificationMinuteKey = 'notification_minute';
  static const int _dailyVerseNotificationId = 1;

  bool _firebaseAvailable = false;

  Future<void> initialize() async {
    // Initialize timezone data for daily verse scheduling
    tz.initializeTimeZones();
    final String timeZoneName = await _getLocalTimeZoneName();
    tz.setLocalLocation(tz.getLocation(timeZoneName));

    // Initialize storage
    final prefs = await SharedPreferences.getInstance();
    _storage = NotificationStorage(prefs);

    // Try to initialize Firebase Messaging (graceful failure if not configured)
    try {
      _firebaseMessaging = FirebaseMessaging.instance;
      _firebaseAvailable = true;
      debugPrint('✅ Firebase Messaging available');
    } catch (e) {
      debugPrint('⚠️ Firebase Messaging not available: $e');
      debugPrint('   Daily verse notifications will still work');
      _firebaseAvailable = false;
    }

    // Request permission (iOS & Android 13+)
    await _requestPermission();

    // Initialize local notifications
    await _initializeLocalNotifications();

    // Set up FCM handlers only if Firebase is available
    if (_firebaseAvailable && _firebaseMessaging != null) {
      // Handle foreground messages (FCM)
      FirebaseMessaging.onMessage.listen(_handleForegroundMessage);

      // Handle background messages (when app is in background but not terminated)
      FirebaseMessaging.onMessageOpenedApp.listen(_handleNotificationTap);

      // Handle notification tap when app was terminated
      final initialMessage = await _firebaseMessaging!.getInitialMessage();
      if (initialMessage != null) {
        _handleNotificationTap(initialMessage);
      }

      // Listen for token refresh
      _firebaseMessaging!.onTokenRefresh.listen(_handleTokenRefresh);
    }
  }

  /// Get local timezone name
  Future<String> _getLocalTimeZoneName() async {
    try {
      return 'Africa/Kampala'; // Default for Uganda
    } catch (e) {
      return 'UTC';
    }
  }

  Future<void> _requestPermission() async {
    // Request FCM permission (only if Firebase is available)
    if (_firebaseAvailable && _firebaseMessaging != null) {
      try {
        final fcmSettings = await _firebaseMessaging!.requestPermission(
          alert: true,
          badge: true,
          sound: true,
          provisional: false,
        );
        debugPrint('🔔 FCM Permission: ${fcmSettings.authorizationStatus}');
      } catch (e) {
        debugPrint('⚠️ FCM permission request failed: $e');
      }
    }

    // Request Android 13+ notification permission
    if (Platform.isAndroid) {
      final androidImplementation = _localNotifications
          .resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();

      await androidImplementation?.requestNotificationsPermission();
      await androidImplementation?.requestExactAlarmsPermission();
    }

    // Request iOS local notification permissions
    if (Platform.isIOS) {
      await _localNotifications
          .resolvePlatformSpecificImplementation<IOSFlutterLocalNotificationsPlugin>()
          ?.requestPermissions(
        alert: true,
        badge: true,
        sound: true,
      );
    }
  }

  Future<void> _initializeLocalNotifications() async {
    const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
    const iosSettings = DarwinInitializationSettings(
      requestAlertPermission: true,
      requestBadgePermission: true,
      requestSoundPermission: true,
    );

    const initSettings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    await _localNotifications.initialize(
      initSettings,
      onDidReceiveNotificationResponse: _onNotificationTapped,
    );
  }

  Future<String?> getToken() async {
    if (!_firebaseAvailable || _firebaseMessaging == null) {
      debugPrint('⚠️ Firebase not available, cannot get token');
      return null;
    }
    return await _firebaseMessaging!.getToken();
  }

  Future<void> deleteToken() async {
    if (!_firebaseAvailable || _firebaseMessaging == null) {
      debugPrint('⚠️ Firebase not available, cannot delete token');
      return;
    }
    await _firebaseMessaging!.deleteToken();
  }

  Future<void> _handleForegroundMessage(RemoteMessage message) async {
    debugPrint('🔔 Foreground message: ${message.notification?.title}');

    // Store notification
    await _storeNotification(message);

    // Show local notification
    _showLocalNotification(message);
  }

  Future<void> _storeNotification(RemoteMessage message) async {
    if (_storage == null) return;

    final type = message.data['type'] ?? '';
    final entityId = int.tryParse(message.data['entity_id'] ?? '0') ?? 0;

    final notification = StoredNotification(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      type: type,
      title: message.notification?.title ?? '',
      body: message.notification?.body ?? '',
      entityId: entityId,
      timestamp: DateTime.now(),
      isRead: false,
    );

    await _storage!.saveNotification(notification);
  }

  Future<void> _showLocalNotification(RemoteMessage message) async {
    const androidDetails = AndroidNotificationDetails(
      'life_issues_channel',
      'Life Issues Notifications',
      channelDescription: 'Notifications for prayers and testimonies',
      importance: Importance.high,
      priority: Priority.high,
      icon: '@mipmap/ic_launcher',
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    const details = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _localNotifications.show(
      message.hashCode,
      message.notification?.title,
      message.notification?.body,
      details,
      payload: _buildPayload(message),
    );
  }

  String _buildPayload(RemoteMessage message) {
    final type = message.data['type'] ?? '';
    final entityId = message.data['entity_id'] ?? '';
    return '$type:$entityId';
  }

  void _onNotificationTapped(NotificationResponse response) {
    if (response.payload != null) {
      _navigateFromPayload(response.payload!);
    }
  }

  void _handleNotificationTap(RemoteMessage message) {
    final payload = _buildPayload(message);
    _navigateFromPayload(payload);
  }

  void _navigateFromPayload(String payload) {
    // Handle daily verse notification
    if (payload == 'daily_verse') {
      final context = navigatorKey?.currentContext;
      if (context != null) {
        // Navigate to home page (where daily verse is)
        Navigator.of(context).pushNamedAndRemoveUntil('/', (route) => false);
      }
      return;
    }

    // Handle FCM notifications (prayers/testimonies)
    final parts = payload.split(':');
    if (parts.length != 2) return;

    final type = parts[0];
    final entityId = int.tryParse(parts[1]);
    if (entityId == null) return;

    final context = navigatorKey?.currentContext;
    if (context == null) return;

    switch (type) {
      case 'prayer_approved':
      case 'someone_prayed':
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => PrayerDetailPage(prayerId: entityId),
          ),
        );
        break;

      case 'testimony_approved':
      case 'someone_praised':
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => TestimonyDetailPage(testimonyId: entityId),
          ),
        );
        break;

      default:
        debugPrint('⚠️ Unknown notification type: $type');
    }
  }

  void _handleTokenRefresh(String token) {
    debugPrint('🔔 FCM Token refreshed: $token');
    // TODO: Send new token to backend
  }

  // ========================================
  // DAILY VERSE NOTIFICATION METHODS
  // ========================================

  /// Check if exact alarms are permitted (Android 12+)
  Future<bool> canScheduleExactAlarms() async {
    if (Platform.isAndroid) {
      final androidImplementation = _localNotifications
          .resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();

      final canSchedule = await androidImplementation?.canScheduleExactNotifications() ?? false;
      debugPrint('⏰ Can schedule exact alarms: $canSchedule');
      return canSchedule;
    }
    return true; // iOS doesn't have this restriction
  }

  /// Schedule daily verse notification
  Future<bool> scheduleDailyVerseNotification({
    required TimeOfDay time,
  }) async {
    try {
      // Cancel existing notification first
      await cancelDailyVerseNotification();

      // Check if we can schedule exact alarms
      final canScheduleExact = await canScheduleExactAlarms();

      // Create notification time
      final now = DateTime.now();
      var scheduledDate = DateTime(
        now.year,
        now.month,
        now.day,
        time.hour,
        time.minute,
      );

      // If the time has passed today, schedule for tomorrow
      if (scheduledDate.isBefore(now)) {
        scheduledDate = scheduledDate.add(const Duration(days: 1));
      }

      // Convert to TZDateTime
      final tzScheduledDate = tz.TZDateTime.from(
        scheduledDate,
        tz.local,
      );

      // Get a verse for the notification
      String title = '📖 Daily Verse';
      String body = 'Your daily Bible verse is ready. Tap to read!';

      try {
        final dataSource = DailyVerseLocalDataSourceImpl();
        final verse = await dataSource.getDailyVerse();
        title = '📖 ${verse.reference}';
        body = _truncateText(verse.kjv, 100);
      } catch (e) {
        debugPrint('⚠️ Could not load verse for notification: $e');
      }

      // Android notification details
      final androidDetails = AndroidNotificationDetails(
        'daily_verse_channel',
        'Daily Verse',
        channelDescription: 'Daily Bible verse notifications',
        importance: Importance.high,
        priority: Priority.high,
        icon: '@mipmap/ic_launcher',
        styleInformation: BigTextStyleInformation(
          body,
          contentTitle: title,
          summaryText: 'Life Issues',
        ),
      );

      // iOS notification details
      const iosDetails = DarwinNotificationDetails(
        presentAlert: true,
        presentBadge: true,
        presentSound: true,
      );

      final notificationDetails = NotificationDetails(
        android: androidDetails,
        iOS: iosDetails,
      );

      // Choose schedule mode based on permissions
      AndroidScheduleMode scheduleMode;
      if (canScheduleExact) {
        scheduleMode = AndroidScheduleMode.exactAllowWhileIdle;
        debugPrint('✅ Scheduling with EXACT timing');
      } else {
        scheduleMode = AndroidScheduleMode.inexactAllowWhileIdle;
        debugPrint('⚠️ Scheduling with INEXACT timing (exact alarms not permitted)');
      }

      // Schedule the notification
      await _localNotifications.zonedSchedule(
        _dailyVerseNotificationId,
        title,
        body,
        tzScheduledDate,
        notificationDetails,
        androidScheduleMode: scheduleMode,
        uiLocalNotificationDateInterpretation: UILocalNotificationDateInterpretation.absoluteTime,
        matchDateTimeComponents: DateTimeComponents.time, // Repeat daily
        payload: 'daily_verse',
      );

      debugPrint('✅ Daily verse notification scheduled for ${time.hour}:${time.minute.toString().padLeft(2, '0')}');
      return true;
    } catch (e) {
      debugPrint('❌ Error scheduling notification: $e');
      return false;
    }
  }

  /// Truncate text to specified length
  String _truncateText(String text, int maxLength) {
    if (text.length <= maxLength) return text;
    return '${text.substring(0, maxLength)}...';
  }

  /// Cancel daily verse notification
  Future<void> cancelDailyVerseNotification() async {
    try {
      await _localNotifications.cancel(_dailyVerseNotificationId);
      debugPrint('✅ Daily verse notification cancelled');
    } catch (e) {
      debugPrint('❌ Error cancelling notification: $e');
    }
  }

  /// Enable daily verse notifications
  Future<bool> enableDailyNotifications(TimeOfDay time) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_notificationEnabledKey, true);
    await prefs.setInt(_notificationHourKey, time.hour);
    await prefs.setInt(_notificationMinuteKey, time.minute);

    final success = await scheduleDailyVerseNotification(time: time);
    return success;
  }

  /// Disable daily verse notifications
  Future<void> disableDailyNotifications() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_notificationEnabledKey, false);

    await cancelDailyVerseNotification();
  }

  /// Check if daily verse notifications are enabled
  Future<bool> areNotificationsEnabled() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_notificationEnabledKey) ?? false;
  }

  /// Get notification time
  Future<TimeOfDay?> getNotificationTime() async {
    final prefs = await SharedPreferences.getInstance();
    final hour = prefs.getInt(_notificationHourKey);
    final minute = prefs.getInt(_notificationMinuteKey);

    if (hour == null || minute == null) {
      return null;
    }

    return TimeOfDay(hour: hour, minute: minute);
  }

  /// Update notification time
  Future<bool> updateNotificationTime(TimeOfDay time) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_notificationHourKey, time.hour);
    await prefs.setInt(_notificationMinuteKey, time.minute);

    // Check if notifications are enabled
    final isEnabled = prefs.getBool(_notificationEnabledKey) ?? false;

    if (isEnabled) {
      return await scheduleDailyVerseNotification(time: time);
    }
    return true;
  }

  /// Show immediate test notification
  Future<void> showTestNotification() async {
    const androidDetails = AndroidNotificationDetails(
      'test_channel',
      'Test Notifications',
      channelDescription: 'Test notifications',
      importance: Importance.high,
      priority: Priority.high,
      icon: '@mipmap/ic_launcher',
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    const notificationDetails = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _localNotifications.show(
      999,
      '🔔 Test Notification',
      'This is a test notification from Life Issues',
      notificationDetails,
      payload: 'test',
    );

    debugPrint('✅ Test notification shown');
  }
}

// Background message handler (must be top-level function)
@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  debugPrint('🔔 Background message: ${message.notification?.title}');
  // Handle background message if needed
}