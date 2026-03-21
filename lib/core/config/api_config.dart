// lib/core/config/api_config.dart

class ApiConfig {
  // Base URL - Update this with your actual Laravel API URL
  static const String baseUrl = 'https://yachalapp.emtechint.com/api';

  // Host root — used to resolve relative URLs returned by the API (e.g. /storage/…)
  static const String host = 'https://yachalapp.emtechint.com';

  // Endpoints

  // Authentication
  static const String login = '/auth/login';
  static const String register = '/auth/register';
  static const String logout = '/auth/logout';
  static const String user = '/auth/user';
  static const String updateProfile = '/auth/profile';

  // Prayers
  static const String prayers = '/prayers';
  static String prayerById(int id) => '/prayers/$id';
  static String prayerPray(int id) => '/prayers/$id/pray';
  static const String myPrayers = '/prayers/mine';

  // Testimonies
  static const String testimonies = '/testimonies';
  static String testimonyById(int id) => '/testimonies/$id';
  static String testimonyPraise(int id) => '/testimonies/$id/praise';
  static const String myTestimonies = '/testimonies/mine';

  // Subscription
  static const String subscriptionStatus = '/user/subscription-status';
  static const String revenueCatWebhook = '/webhooks/revenuecat';

  // FCM
  static const String fcmToken = '/fcm/token';

  // Database Sync
  static const String syncDatabase = '/sync/database';
  static const String syncDatabaseFull = '/sync/database/full';
  static const String syncMetadata = '/sync/metadata';

  // Home
  static const String homeSummary = '/home/summary';

  // Request timeout
  static const Duration timeout = Duration(seconds: 30);

  // Pagination
  static const int defaultPageSize = 15; // Prayers
  static const int testimonyPageSize = 10; // Testimonies

  /// Builds full URL from endpoint
  static String fullUrl(String endpoint) {
    return '$baseUrl$endpoint';
  }

  /// Adds query parameters to URL
  static String withParams(String endpoint, Map<String, dynamic> params) {
    final uri = Uri.parse(fullUrl(endpoint));
    final newUri = uri.replace(queryParameters: params.map(
          (key, value) => MapEntry(key, value.toString()),
    ));
    return newUri.toString();
  }
}