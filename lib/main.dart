// lib/main.dart
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:life_issues_flutter/presentation/pages/main_navigation_page.dart';
import 'core/constants/app_colors.dart';
import 'core/constants/app_strings.dart';
import 'core/di/injection_container.dart' as di;
import 'core/services/notification_handler.dart';
import 'presentation/blocs/auth/auth_bloc.dart';
import 'presentation/blocs/auth/auth_event.dart';
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

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize Firebase FIRST (before NotificationHandler)
  try {
    await Firebase.initializeApp();
    debugPrint('✅ Firebase initialized');
  } catch (e) {
    debugPrint('⚠️ Firebase initialization error: $e');
  }

  // Initialize Mobile Ads SDK
  await MobileAds.instance.initialize();

  // Initialize Dependency Injection
  await di.init();

  // Initialize notification handler (unified service)
  // Note: This requires Firebase to be initialized first
  NotificationHandler.navigatorKey = navigatorKey;
  await NotificationHandler().initialize();

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
          create: (_) => di.sl<AuthBloc>()..add(CheckAuthStatusEvent()),
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
            theme: _buildLightTheme(),
            darkTheme: _buildDarkTheme(),
            themeMode: themeMode,
            home: const MainNavigationWrapper(),
            debugShowCheckedModeBanner: false,
          );
        },
      ),
    );
  }

  ThemeData _buildLightTheme() {
    final textTheme = GoogleFonts.loraTextTheme();

    return ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.primary,
        brightness: Brightness.light,
      ),
      textTheme: textTheme,
      scaffoldBackgroundColor: AppColors.background,
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.primary,
        foregroundColor: Colors.white,
        elevation: 0,
        titleTextStyle: GoogleFonts.lora(
          fontSize: 20,
          fontWeight: FontWeight.w600,
          color: Colors.white,
        ),
      ),
      cardTheme: CardThemeData(
        elevation: 2,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
      ),
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        selectedItemColor: AppColors.primary,
        unselectedItemColor: AppColors.textSecondary,
        showUnselectedLabels: true,
        type: BottomNavigationBarType.fixed,
        selectedLabelStyle: GoogleFonts.lora(fontSize: 12),
        unselectedLabelStyle: GoogleFonts.lora(fontSize: 12),
      ),
    );
  }

  ThemeData _buildDarkTheme() {
    final textTheme = GoogleFonts.loraTextTheme(ThemeData.dark().textTheme);

    return ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.primary,
        brightness: Brightness.dark,
      ),
      textTheme: textTheme,
      scaffoldBackgroundColor: AppColors.darkBackground,
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.darkSurface,
        foregroundColor: AppColors.darkTextPrimary,
        elevation: 0,
        titleTextStyle: GoogleFonts.lora(
          fontSize: 20,
          fontWeight: FontWeight.w600,
          color: AppColors.darkTextPrimary,
        ),
      ),
      cardTheme: CardThemeData(
        color: AppColors.darkSurface,
        elevation: 2,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(16),
        ),
      ),
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        backgroundColor: AppColors.darkSurface,
        selectedItemColor: AppColors.primary,
        unselectedItemColor: AppColors.darkTextSecondary,
        showUnselectedLabels: true,
        type: BottomNavigationBarType.fixed,
        selectedLabelStyle: GoogleFonts.lora(fontSize: 12),
        unselectedLabelStyle: GoogleFonts.lora(fontSize: 12),
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
  int _currentIndex = 0;

  void _onTabSelected(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      onWillPop: () async {
        // If not on home tab, go to home tab
        if (_currentIndex != 0) {
          setState(() {
            _currentIndex = 0;
          });
          return false; // Don't exit app
        }
        // Already on home tab, allow exit
        return true;
      },
      child: MainNavigationPageUpdated(
        initialIndex: _currentIndex,
        onTabSelected: _onTabSelected,
      ),
    );
  }
}