// lib/core/services/deep_link_handler.dart

import 'package:flutter/material.dart';
import '../../presentation/pages/prayers/prayer_detail_page.dart';
import '../../presentation/pages/testimonies/testimony_detail_page.dart';
import '../../presentation/pages/auth/login_page.dart';

class DeepLinkHandler {
  static final DeepLinkHandler _instance = DeepLinkHandler._internal();
  factory DeepLinkHandler() => _instance;
  DeepLinkHandler._internal();

  // Navigation key for handling deep links
  static GlobalKey<NavigatorState>? navigatorKey;

  /// Handle deep link from URL
  /// Expected formats:
  /// - lifeissues://prayer/123
  /// - lifeissues://testimony/456
  /// - lifeissues://login
  /// - https://lifeissues.com/prayer/123
  /// - https://lifeissues.com/testimony/456
  void handleDeepLink(Uri uri) {
    debugPrint('Handling deep link: $uri');

    final context = navigatorKey?.currentContext;
    if (context == null) {
      debugPrint('Navigator context not available');
      return;
    }

    // Parse the path
    final pathSegments = uri.pathSegments;
    if (pathSegments.isEmpty) {
      debugPrint('Empty path segments');
      return;
    }

    final type = pathSegments[0];

    switch (type) {
      case 'prayer':
        if (pathSegments.length > 1) {
          final prayerId = int.tryParse(pathSegments[1]);
          if (prayerId != null) {
            _navigateToPrayer(context, prayerId);
          }
        }
        break;

      case 'testimony':
        if (pathSegments.length > 1) {
          final testimonyId = int.tryParse(pathSegments[1]);
          if (testimonyId != null) {
            _navigateToTestimony(context, testimonyId);
          }
        }
        break;

      case 'login':
        _navigateToLogin(context);
        break;

      default:
        debugPrint('Unknown deep link type: $type');
    }
  }

  void _navigateToPrayer(BuildContext context, int prayerId) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => PrayerDetailPage(prayerId: prayerId),
      ),
    );
  }

  void _navigateToTestimony(BuildContext context, int testimonyId) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => TestimonyDetailPage(testimonyId: testimonyId),
      ),
    );
  }

  void _navigateToLogin(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => const LoginPage(),
      ),
    );
  }

  /// Parse query parameters from URL
  Map<String, String> parseQueryParameters(Uri uri) {
    return uri.queryParameters;
  }
}