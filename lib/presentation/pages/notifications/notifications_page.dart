// lib/presentation/pages/notifications/notifications_page.dart

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/services/notification_storage.dart';
import '../prayers/prayer_detail_page.dart';
import '../testimonies/testimony_detail_page.dart';

class NotificationsPage extends StatefulWidget {
  const NotificationsPage({Key? key}) : super(key: key);

  @override
  State<NotificationsPage> createState() => _NotificationsPageState();
}

class _NotificationsPageState extends State<NotificationsPage> {
  NotificationStorage? _storage;
  List<StoredNotification> _notifications = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _initializeStorage();
  }

  Future<void> _initializeStorage() async {
    final prefs = await SharedPreferences.getInstance();
    _storage = NotificationStorage(prefs);
    await _loadNotifications();
  }

  Future<void> _loadNotifications() async {
    if (_storage == null) return;

    setState(() {
      _isLoading = true;
    });

    final notifications = await _storage!.getNotifications();

    setState(() {
      _notifications = notifications;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final unreadCount = _notifications.where((n) => !n.isRead).length;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notifications'),
        actions: [
          if (unreadCount > 0)
            TextButton(
              onPressed: _markAllAsRead,
              child: const Text('Mark all read'),
            ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _notifications.isEmpty
          ? _buildEmptyState(context)
          : ListView.separated(
        itemCount: _notifications.length,
        separatorBuilder: (_, __) => const Divider(height: 1),
        itemBuilder: (context, index) {
          return _buildNotificationTile(_notifications[index]);
        },
      ),
    );
  }

  Widget _buildEmptyState(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.notifications_none,
            size: 64,
            color: Theme.of(context).colorScheme.primary.withOpacity(0.5),
          ),
          const SizedBox(height: 16),
          Text(
            'No notifications yet',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const SizedBox(height: 8),
          Text(
            'You\'ll see updates about your prayers and testimonies here',
            style: Theme.of(context).textTheme.bodyMedium,
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildNotificationTile(StoredNotification notification) {
    return Dismissible(
      key: Key(notification.id),
      background: Container(
        color: Colors.red,
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 16),
        child: const Icon(Icons.delete, color: Colors.white),
      ),
      direction: DismissDirection.endToStart,
      onDismissed: (_) => _deleteNotification(notification.id),
      child: ListTile(
        leading: Container(
          width: 48,
          height: 48,
          decoration: BoxDecoration(
            color: notification.isRead
                ? Theme.of(context).colorScheme.surfaceVariant
                : Theme.of(context).colorScheme.primaryContainer,
            shape: BoxShape.circle,
          ),
          child: Icon(
            _getIcon(notification.type),
            color: notification.isRead
                ? Theme.of(context).colorScheme.onSurfaceVariant
                : Theme.of(context).colorScheme.onPrimaryContainer,
          ),
        ),
        title: Text(
          notification.title,
          style: TextStyle(
            fontWeight: notification.isRead ? FontWeight.normal : FontWeight.bold,
          ),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 4),
            Text(
              notification.body,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 4),
            Text(
              _formatTimestamp(notification.timestamp),
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
        trailing: !notification.isRead
            ? Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.primary,
            shape: BoxShape.circle,
          ),
        )
            : null,
        onTap: () => _handleNotificationTap(notification),
      ),
    );
  }

  IconData _getIcon(String type) {
    switch (type) {
      case 'prayer_approved':
        return Icons.check_circle;
      case 'someone_prayed':
        return Icons.favorite;
      case 'testimony_approved':
        return Icons.verified;
      case 'someone_praised':
        return Icons.auto_awesome;
      default:
        return Icons.notifications;
    }
  }

  String _formatTimestamp(DateTime timestamp) {
    final now = DateTime.now();
    final difference = now.difference(timestamp);

    if (difference.inMinutes < 1) {
      return 'Just now';
    } else if (difference.inHours < 1) {
      return '${difference.inMinutes}m ago';
    } else if (difference.inDays < 1) {
      return '${difference.inHours}h ago';
    } else if (difference.inDays < 7) {
      return '${difference.inDays}d ago';
    } else {
      return DateFormat('MMM d').format(timestamp);
    }
  }

  Future<void> _handleNotificationTap(StoredNotification notification) async {
    // Mark as read
    if (_storage != null && !notification.isRead) {
      await _storage!.markAsRead(notification.id);
      await _loadNotifications(); // Refresh list
    }

    // Navigate to appropriate page
    if (!mounted) return;

    switch (notification.type) {
      case 'prayer_approved':
      case 'someone_prayed':
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => PrayerDetailPage(prayerId: notification.entityId),
          ),
        );
        break;

      case 'testimony_approved':
      case 'someone_praised':
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) => TestimonyDetailPage(testimonyId: notification.entityId),
          ),
        );
        break;
    }
  }

  Future<void> _markAllAsRead() async {
    if (_storage != null) {
      await _storage!.markAllAsRead();
      await _loadNotifications();
    }
  }

  Future<void> _deleteNotification(String notificationId) async {
    if (_storage != null) {
      await _storage!.deleteNotification(notificationId);
    }
  }
}