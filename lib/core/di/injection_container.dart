import 'package:get_it/get_it.dart';
import 'package:life_issues_flutter/domain/usecases/get_random_verse_for_home.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

// Core
import '../network/api_client.dart';
import '../utils/auth_manager.dart';
import '../services/social_auth_service.dart';
import '../services/database_sync_service.dart';

// Data Sources - Local (Existing)
import '../../data/datasources/verse_local_datasource.dart';
import '../../data/datasources/issue_local_datasource.dart';
import '../../data/datasources/daily_verse_local_datasource.dart';
import '../../data/datasources/favorites_local_datasource.dart';
import '../../data/datasources/settings_local_datasource.dart';

// Data Sources - Remote (New)
import '../../data/datasources/prayer_remote_data_source.dart';
import '../../data/datasources/testimony_remote_datasource.dart';
import '../../data/datasources/auth_remote_datasource.dart';
import '../../data/datasources/subscription_remote_datasource.dart';
import '../../data/datasources/home_remote_datasource.dart';

// Repository Implementations - Existing
import '../../data/repositories/verse_repository_impl.dart';
import '../../data/repositories/issue_repository_impl.dart';
import '../../data/repositories/daily_verse_repository_impl.dart';
import '../../data/repositories/favorites_repository_impl.dart';
import '../../data/repositories/settings_repository_impl.dart';

// Repository Implementations - New
import '../../data/repositories/prayer_repository_impl.dart';
import '../../data/repositories/testimony_repository_impl.dart';
import '../../data/repositories/auth_repository_impl.dart';
import '../../data/repositories/subscription_repository_impl.dart';

// Repository Interfaces - Existing
import '../../domain/repositories/verse_repository.dart';
import '../../domain/repositories/issue_repository.dart';
import '../../domain/repositories/daily_verse_repository.dart';
import '../../domain/repositories/favorites_repository.dart';
import '../../domain/repositories/settings_repository.dart';

// Repository Interfaces - New
import '../../domain/repositories/prayer_repository.dart';
import '../../domain/repositories/testimony_repository.dart';
import '../../domain/repositories/auth_repository.dart';
import '../../domain/repositories/subscription_repository.dart';

// Use Cases - Existing
import '../../domain/usecases/get_verses_for_issue.dart';
import '../../domain/usecases/get_issues.dart';
import '../../domain/usecases/get_daily_verse.dart';
import '../../domain/usecases/get_random_verse.dart';
import '../../domain/usecases/favorites_usecases.dart';
import '../../domain/usecases/settings_usecases.dart';

// Use Cases - Prayers
import '../../domain/usecases/prayers/get_prayers.dart';
import '../../domain/usecases/prayers/get_prayer_by_id.dart';
import '../../domain/usecases/prayers/submit_prayer.dart';
import '../../domain/usecases/prayers/toggle_praying.dart';
import '../../domain/usecases/prayers/delete_prayer.dart';
import '../../domain/usecases/prayers/get_my_prayers.dart';

// Use Cases - Testimonies
import '../../domain/usecases/testimonies/get_testimonies.dart';
import '../../domain/usecases/testimonies/get_testimony_by_id.dart';
import '../../domain/usecases/testimonies/submit_testimony.dart';
import '../../domain/usecases/testimonies/toggle_praise.dart';
import '../../domain/usecases/testimonies/delete_testimony.dart';
import '../../domain/usecases/testimonies/get_my_testimonies.dart';

// Use Cases - Auth
import '../../presentation/blocs/auth/auth_bloc.dart';
import '../../domain/usecases/auth/login.dart';
import '../../domain/usecases/auth/register.dart';
import '../../domain/usecases/auth/social_login.dart';
import '../../domain/usecases/auth/logout.dart';
import '../../domain/usecases/auth/check_auth_status.dart';

// Use Cases - Subscription
import '../../domain/usecases/subscription/get_subscription_status.dart';
import '../../domain/usecases/subscription/check_subscription.dart';

// BLoCs - Existing
import '../../presentation/blocs/daily_verse/daily_verse_bloc.dart';
import '../../presentation/blocs/issues/issues_bloc.dart';
import '../../presentation/blocs/verses/verses_bloc.dart';
import '../../presentation/blocs/favorites/favorites_bloc.dart';
import '../../presentation/blocs/settings/settings_bloc.dart';

// BLoCs - New
import '../../presentation/blocs/prayer/prayer_bloc.dart';
import '../../presentation/blocs/testimony/testimony_bloc.dart';
import '../../presentation/blocs/auth/auth_bloc.dart';
import '../../presentation/blocs/subscription/subscription_bloc.dart';

final sl = GetIt.instance;

Future<void> init() async {
  //! External Dependencies
  // SharedPreferences
  final sharedPreferences = await SharedPreferences.getInstance();
  sl.registerLazySingleton(() => sharedPreferences);

  // FlutterSecureStorage
  const secureStorage = FlutterSecureStorage();
  sl.registerLazySingleton(() => secureStorage);

  //! Core
  // Auth Manager
  sl.registerLazySingleton<AuthManager>(
        () => AuthManager(
      sl(),
      sl(),
    ),
  );

  // API Client
  sl.registerLazySingleton<ApiClient>(
        () => ApiClient(sl()),
  );

  // Social Auth Service
  sl.registerLazySingleton<SocialAuthService>(
        () => SocialAuthService(authRepository: sl()),
  );

  // Database Sync Service
  sl.registerLazySingleton<DatabaseSyncService>(
        () => DatabaseSyncService(apiClient: sl()),
  );

  //! BLoCs
  // Existing BLoCs
  sl.registerFactory(() => DailyVerseBloc(
    getDailyVerse: sl(),
    getRandomVerse: sl(),
  ));

  sl.registerFactory(() => IssuesBloc(getIssues: sl()));

  sl.registerFactory(() => VersesBloc(
    getVersesForIssue: sl(),
    getRandomVerseForHome: sl(),
  ));

  sl.registerFactory(() => FavoritesBloc(
    getFavoriteVerses: sl(),
    toggleFavorite: sl(),
  ));

  sl.registerFactory(() => SettingsBloc(
    getSettings: sl(),
    updateBibleVersion: sl(),
    toggleNotifications: sl(),
    updateNotificationTime: sl(),
    toggleTheme: sl(),
  ));

  // New BLoCs
  sl.registerFactory(() => PrayerBloc(
    getPrayers: sl(),
    getPrayerById: sl(),
    submitPrayer: sl(),
    togglePraying: sl(),
    deletePrayer: sl(),
    getMyPrayers: sl(),
  ));

  sl.registerFactory(() => TestimonyBloc(
    getTestimonies: sl(),
    getTestimonyById: sl(),
    submitTestimony: sl(),
    togglePraise: sl(),
    deleteTestimony: sl(),
    getMyTestimonies: sl(),
  ));

  sl.registerFactory(() => SubscriptionBloc(
    getSubscriptionStatus: sl(),
    checkSubscription: sl(),
    subscriptionRepository: sl(),
  ));

  // AuthBloc as singleton to ensure single instance throughout app
  sl.registerLazySingleton(() => AuthBloc(
    login: sl(),
    register: sl(),
    socialLogin: sl(),
    logout: sl(),
    checkAuthStatus: sl(),
    authRepository: sl(),
    socialAuthService: sl(),
  ));

  //! Use Cases
  // Existing Use Cases
  sl.registerLazySingleton(() => GetDailyVerse(sl()));
  sl.registerLazySingleton(() => GetRandomVerse(sl()));
  sl.registerLazySingleton(() => GetIssues(sl()));
  sl.registerLazySingleton(() => GetVersesForIssue(sl()));
  sl.registerLazySingleton(() => GetRandomVerseForHome(sl()));
  sl.registerLazySingleton(() => GetFavoriteVerses(sl()));
  sl.registerLazySingleton(() => ToggleFavorite(sl()));
  sl.registerLazySingleton(() => GetSettings(sl()));
  sl.registerLazySingleton(() => UpdateBibleVersion(sl()));
  sl.registerLazySingleton(() => ToggleNotifications(sl()));
  sl.registerLazySingleton(() => UpdateNotificationTime(sl()));
  sl.registerLazySingleton(() => ToggleTheme(sl()));

  // Prayer Use Cases
  sl.registerLazySingleton(() => GetPrayers(sl()));
  sl.registerLazySingleton(() => GetPrayerById(sl()));
  sl.registerLazySingleton(() => SubmitPrayer(sl()));
  sl.registerLazySingleton(() => TogglePraying(sl()));
  sl.registerLazySingleton(() => DeletePrayer(sl()));
  sl.registerLazySingleton(() => GetMyPrayers(sl()));

  // Testimony Use Cases
  sl.registerLazySingleton(() => GetTestimonies(sl()));
  sl.registerLazySingleton(() => GetTestimonyById(sl()));
  sl.registerLazySingleton(() => SubmitTestimony(sl()));
  sl.registerLazySingleton(() => TogglePraise(sl()));
  sl.registerLazySingleton(() => DeleteTestimony(sl()));
  sl.registerLazySingleton(() => GetMyTestimonies(sl()));

  // Auth Use Cases
  sl.registerLazySingleton(() => Login(sl()));
  sl.registerLazySingleton(() => Register(sl()));
  sl.registerLazySingleton(() => SocialLogin(sl()));
  sl.registerLazySingleton(() => Logout(sl()));
  sl.registerLazySingleton(() => CheckAuthStatus(sl()));

  // Subscription Use Cases
  sl.registerLazySingleton(() => GetSubscriptionStatus(sl()));
  sl.registerLazySingleton(() => CheckSubscription(sl()));

  //! Repositories
  // Existing Repositories
  sl.registerLazySingleton<DailyVerseRepository>(
        () => DailyVerseRepositoryImpl(localDataSource: sl()),
  );

  sl.registerLazySingleton<IssueRepository>(
        () => IssueRepositoryImpl(localDataSource: sl()),
  );

  sl.registerLazySingleton<VerseRepository>(
        () => VerseRepositoryImpl(localDataSource: sl()),
  );

  sl.registerLazySingleton<FavoritesRepository>(
        () => FavoritesRepositoryImpl(localDataSource: sl()),
  );

  sl.registerLazySingleton<SettingsRepository>(
        () => SettingsRepositoryImpl(localDataSource: sl()),
  );

  // New Repositories
  sl.registerLazySingleton<PrayerRepository>(
        () => PrayerRepositoryImpl(remoteDataSource: sl()),
  );

  sl.registerLazySingleton<TestimonyRepository>(
        () => TestimonyRepositoryImpl(remoteDataSource: sl()),
  );

  sl.registerLazySingleton<AuthRepository>(
        () => AuthRepositoryImpl(
      remoteDataSource: sl(),
      authManager: sl(),
    ),
  );

  sl.registerLazySingleton<SubscriptionRepository>(
        () => SubscriptionRepositoryImpl(remoteDataSource: sl()),
  );

  //! Data Sources
  // Existing Local Data Sources
  sl.registerLazySingleton<DailyVerseLocalDataSource>(
        () => DailyVerseLocalDataSourceImpl(),
  );

  sl.registerLazySingleton<IssueLocalDataSource>(
        () => IssueLocalDataSourceImpl(),
  );

  sl.registerLazySingleton<VerseLocalDataSource>(
        () => VerseLocalDataSourceImpl(),
  );

  sl.registerLazySingleton<FavoritesLocalDataSource>(
        () => FavoritesLocalDataSourceImpl(),
  );

  sl.registerLazySingleton<SettingsLocalDataSource>(
        () => SettingsLocalDataSourceImpl(),
  );

  // New Remote Data Sources
  sl.registerLazySingleton<PrayerRemoteDataSource>(
        () => PrayerRemoteDataSourceImpl(apiClient: sl()),
  );

  sl.registerLazySingleton<TestimonyRemoteDataSource>(
        () => TestimonyRemoteDataSourceImpl(apiClient: sl()),
  );

  sl.registerLazySingleton<AuthRemoteDataSource>(
        () => AuthRemoteDataSourceImpl(apiClient: sl()),
  );

  sl.registerLazySingleton<SubscriptionRemoteDataSource>(
        () => SubscriptionRemoteDataSourceImpl(apiClient: sl()),
  );

  sl.registerLazySingleton<HomeRemoteDataSource>(
        () => HomeRemoteDataSourceImpl(apiClient: sl()),
  );
}