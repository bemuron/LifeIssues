// lib/core/services/notification_storage.dart

import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class NotificationStorage {
  static const String _notificationsKey = 'stored_notifications';
  static const String _unreadCountKey = 'unread_notifications_count';

  final SharedPreferences _prefs;

  NotificationStorage(this._prefs);

  /// Save a new notification
  Future<void> saveNotification(StoredNotification notification) async {
    final notifications = await getNotifications();
    notifications.insert(0, notification); // Add to beginning

    // Keep only last 100 notifications
    if (notifications.length > 100) {
      notifications.removeRange(100, notifications.length);
    }

    await _saveNotifications(notifications);

    // Increment unread count
    final currentUnread = await getUnreadCount();
    await _prefs.setInt(_unreadCountKey, currentUnread + 1);
  }

  /// Get all notifications
  Future<List<StoredNotification>> getNotifications() async {
    final jsonString = _prefs.getString(_notificationsKey);
    if (jsonString == null) return [];

    try {
      final List<dynamic> jsonList = json.decode(jsonString);
      return jsonList
          .map((json) => StoredNotification.fromJson(json))
          .toList();
    } catch (e) {
      return [];
    }
  }

  /// Save notifications list
  Future<void> _saveNotifications(List<StoredNotification> notifications) async {
    final jsonString = json.encode(
      notifications.map((n) => n.toJson()).toList(),
    );
    await _prefs.setString(_notificationsKey, jsonString);
  }

  /// Mark notification as read
  Future<void> markAsRead(String notificationId) async {
    final notifications = await getNotifications();
    final index = notifications.indexWhere((n) => n.id == notificationId);

    if (index != -1 && !notifications[index].isRead) {
      notifications[index].isRead = true;
      await _saveNotifications(notifications);

      // Decrement unread count
      final currentUnread = await getUnreadCount();
      if (currentUnread > 0) {
        await _prefs.setInt(_unreadCountKey, currentUnread - 1);
      }
    }
  }

  /// Mark all notifications as read
  Future<void> markAllAsRead() async {
    final notifications = await getNotifications();
    for (var notification in notifications) {
      notification.isRead = true;
    }
    await _saveNotifications(notifications);
    await _prefs.setInt(_unreadCountKey, 0);
  }

  /// Get unread count
  Future<int> getUnreadCount() async {
    return _prefs.getInt(_unreadCountKey) ?? 0;
  }

  /// Clear all notifications
  Future<void> clearAll() async {
    await _prefs.remove(_notificationsKey);
    await _prefs.setInt(_unreadCountKey, 0);
  }

  /// Delete specific notification
  Future<void> deleteNotification(String notificationId) async {
    final notifications = await getNotifications();
    final index = notifications.indexWhere((n) => n.id == notificationId);

    if (index != -1) {
      final wasUnread = !notifications[index].isRead;
      notifications.removeAt(index);
      await _saveNotifications(notifications);

      if (wasUnread) {
        final currentUnread = await getUnreadCount();
        if (currentUnread > 0) {
          await _prefs.setInt(_unreadCountKey, currentUnread - 1);
        }
      }
    }
  }
}

class StoredNotification {
  final String id;
  final String type; // prayer_approved, someone_prayed, testimony_approved, someone_praised
  final String title;
  final String body;
  final int entityId;
  final DateTime timestamp;
  bool isRead;

  StoredNotification({
    required this.id,
    required this.type,
    required this.title,
    required this.body,
    required this.entityId,
    required this.timestamp,
    this.isRead = false,
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'type': type,
      'title': title,
      'body': body,
      'entityId': entityId,
      'timestamp': timestamp.toIso8601String(),
      'isRead': isRead,
    };
  }

  factory StoredNotification.fromJson(Map<String, dynamic> json) {
    return StoredNotification(
      id: json['id'],
      type: json['type'],
      title: json['title'],
      body: json['body'],
      entityId: json['entityId'],
      timestamp: DateTime.parse(json['timestamp']),
      isRead: json['isRead'] ?? false,
    );
  }
}