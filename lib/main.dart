// lib/main.dart
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'firebase_options.dart';
import 'package:life_issues_flutter/presentation/pages/main_navigation_page.dart';
import 'presentation/pages/auth/onboarding_page.dart';
import 'core/constants/app_strings.dart';
import 'core/theme/app_theme.dart';
import 'core/di/injection_container.dart' as di;
import 'core/services/database_sync_service.dart';
import 'core/services/notification_handler.dart';
import 'presentation/blocs/auth/auth_bloc.dart';
import 'presentation/blocs/auth/auth_event.dart';
import 'presentation/pages/auth/splash_page.dart';
import 'presentation/blocs/daily_verse/daily_verse_bloc.dart';
import 'presentation/blocs/favorites/favorites_bloc.dart';
import 'presentation/blocs/issues/issues_bloc.dart';
import 'presentation/blocs/prayer/prayer_bloc.dart';
import 'presentation/blocs/random_verse/random_verse_bloc.dart';
import 'presentation/blocs/settings/settings_bloc.dart';
import 'presentation/blocs/subscription/subscription_bloc.dart';
import 'presentation/blocs/testimony/testimony_bloc.dart';
import 'presentation/blocs/verses/verses_bloc.dart';

// Global navigator key for notifications
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

// Flag to track Firebase initialization
bool _firebaseInitialized = false;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize Firebase FIRST with error handling
  try {
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );
    _firebaseInitialized = true;
    debugPrint('✅ Firebase initialized successfully');
  } catch (e) {
    _firebaseInitialized = false;
    debugPrint('❌ Firebase initialization failed: $e');
    debugPrint('⚠️ App will continue but Firebase features will not work');
  }

  // Initialize Mobile Ads SDK
  try {
    await MobileAds.instance.initialize();
    debugPrint('✅ Mobile Ads initialized');
  } catch (e) {
    debugPrint('⚠️ Mobile Ads initialization failed: $e');
  }

  // Initialize Dependency Injection
  await di.init();

  // Initialize notification handler if Firebase is available
  if (_firebaseInitialized) {
    try {
      NotificationHandler.navigatorKey = navigatorKey;
      NotificationHandler.apiClient = di.sl();
      await NotificationHandler().initialize();
      debugPrint('✅ Notification handler initialized');
    } catch (e) {
      debugPrint('⚠️ Notification handler initialization failed: $e');
    }
  }

  // Sync local DB in background (does not block app startup)
  di.sl<DatabaseSyncService>().sync();

  runApp(const LifeIssuesApp());
}

class LifeIssuesApp extends StatelessWidget {
  const LifeIssuesApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        // Existing BLoCs
        BlocProvider<SettingsBloc>(
          create: (_) => di.sl<SettingsBloc>()..add(LoadSettingsEvent()),
        ),
        BlocProvider<DailyVerseBloc>(
          create: (_) => di.sl<DailyVerseBloc>()..add(LoadDailyVerseEvent()),
        ),
        BlocProvider<IssuesBloc>(
          create: (_) => di.sl<IssuesBloc>()..add(LoadIssuesEvent()),
        ),
        BlocProvider<FavoritesBloc>(
          create: (_) => di.sl<FavoritesBloc>()..add(LoadFavoritesEvent()),
        ),
        BlocProvider<RandomVerseBloc>(
          create: (_) => di.sl<RandomVerseBloc>(),
        ),
        BlocProvider<VersesBloc>(
          create: (_) => di.sl<VersesBloc>(),
        ),

        // Community BLoCs
        BlocProvider<AuthBloc>(
          create: (_) => di.sl<AuthBloc>(),
        ),
        BlocProvider<SubscriptionBloc>(
          create: (_) => di.sl<SubscriptionBloc>(),
        ),
        BlocProvider<PrayerBloc>(
          create: (_) => di.sl<PrayerBloc>(),
        ),
        BlocProvider<TestimonyBloc>(
          create: (_) => di.sl<TestimonyBloc>(),
        ),
      ],
      child: BlocBuilder<SettingsBloc, SettingsState>(
        builder: (context, state) {
          final themeMode = state is SettingsLoaded
              ? (state.isDarkMode ? ThemeMode.dark : ThemeMode.light)
              : ThemeMode.system;

          return MaterialApp(
            title: AppStrings.appName,
            navigatorKey: navigatorKey, // For notification navigation
            theme: AppTheme.lightTheme,
            darkTheme: AppTheme.darkTheme,
            themeMode: themeMode,
            home: const SplashPage(),
            debugShowCheckedModeBanner: false,
          );
        },
      ),
    );
  }

}

/// Wrapper to handle back navigation behavior
/// Always returns to home page (tab 0) before exiting app
class MainNavigationWrapper extends StatelessWidget {
  const MainNavigationWrapper({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const _MainNavigationWithBackHandler();
  }
}

class _MainNavigationWithBackHandler extends StatefulWidget {
  const _MainNavigationWithBackHandler({Key? key}) : super(key: key);

  @override
  State<_MainNavigationWithBackHandler> createState() =>
      _MainNavigationWithBackHandlerState();
}

class _MainNavigationWithBackHandlerState
    extends State<_MainNavigationWithBackHandler> {
  late Future<bool> _shouldShowOnboarding;

  @override
  void initState() {
    super.initState();
    _shouldShowOnboarding = _checkOnboardingStatus();
  }

  Future<bool> _checkOnboardingStatus() async {
    final prefs = await SharedPreferences.getInstance();
    final hasSeenOnboarding = prefs.getBool('has_seen_onboarding') ?? false;
    return !hasSeenOnboarding;
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<bool>(
      future: _shouldShowOnboarding,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Scaffold(
            body: Center(child: CircularProgressIndicator()),
          );
        }

        if (snapshot.hasData && snapshot.data == true) {
          return const OnboardingPage();
        }

        // PopScope and tab-index state live inside MainNavigationPageUpdated —
        // single source of truth, no external state sync needed.
        return const MainNavigationPageUpdated();
      },
    );
  }
}