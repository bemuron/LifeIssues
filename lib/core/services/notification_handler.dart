// lib/core/services/notification_handler.dart

import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../presentation/pages/prayers/prayer_detail_page.dart';
import '../../presentation/pages/testimonies/testimony_detail_page.dart';
import 'notification_storage.dart';

class NotificationHandler {
  static final NotificationHandler _instance = NotificationHandler._internal();
  factory NotificationHandler() => _instance;
  NotificationHandler._internal();

  final FirebaseMessaging _firebaseMessaging = FirebaseMessaging.instance;
  final FlutterLocalNotificationsPlugin _localNotifications =
  FlutterLocalNotificationsPlugin();

  NotificationStorage? _storage;

  // Navigation key for handling notifications when app is in background
  static GlobalKey<NavigatorState>? navigatorKey;

  Future<void> initialize() async {
    // Initialize storage
    final prefs = await SharedPreferences.getInstance();
    _storage = NotificationStorage(prefs);

    // Request permission (iOS)
    await _requestPermission();

    // Initialize local notifications
    await _initializeLocalNotifications();

    // Handle foreground messages
    FirebaseMessaging.onMessage.listen(_handleForegroundMessage);

    // Handle background messages (when app is in background but not terminated)
    FirebaseMessaging.onMessageOpenedApp.listen(_handleNotificationTap);

    // Handle notification tap when app was terminated
    final initialMessage = await _firebaseMessaging.getInitialMessage();
    if (initialMessage != null) {
      _handleNotificationTap(initialMessage);
    }

    // Listen for token refresh
    _firebaseMessaging.onTokenRefresh.listen(_handleTokenRefresh);
  }

  Future<void> _requestPermission() async {
    final settings = await _firebaseMessaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
      provisional: false,
    );

    debugPrint('Notification permission: ${settings.authorizationStatus}');
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
    return await _firebaseMessaging.getToken();
  }

  Future<void> deleteToken() async {
    await _firebaseMessaging.deleteToken();
  }

  Future<void> _handleForegroundMessage(RemoteMessage message) async {
    debugPrint('Foreground message: ${message.notification?.title}');

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
        debugPrint('Unknown notification type: $type');
    }
  }

  void _handleTokenRefresh(String token) {
    debugPrint('FCM Token refreshed: $token');
    // TODO: Send new token to backend
    // This should be implemented in the app's initialization
    // to automatically update the token on the server
  }
}

// Background message handler (must be top-level function)
@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  debugPrint('Background message: ${message.notification?.title}');
  // Handle background message if needed
}