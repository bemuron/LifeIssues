// lib/core/services/connectivity_service.dart
import 'dart:io';

class ConnectivityService {
  /// Returns true if the device can reach the internet.
  /// Uses a DNS lookup against a well-known host with a short timeout so it
  /// resolves near-instantly when offline (SocketException fires immediately)
  /// and within a couple of seconds on slow networks.
  static Future<bool> isConnected() async {
    try {
      final result = await InternetAddress.lookup('google.com')
          .timeout(const Duration(seconds: 5));
      return result.isNotEmpty && result.first.rawAddress.isNotEmpty;
    } catch (_) {
      return false;
    }
  }

  /// Returns true if the error message describes a connectivity problem
  /// (no internet, timeout, unknown network error, etc.).
  static bool isConnectivityError(String message) {
    final lower = message.toLowerCase();
    return lower.contains('internet') ||
        lower.contains('network') ||
        lower.contains('connection') ||
        lower.contains('timeout') ||
        lower.contains('unknown error') ||
        lower.contains('socket');
  }
}
