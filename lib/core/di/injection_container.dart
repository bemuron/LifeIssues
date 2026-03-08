import 'package:get_it/get_it.dart';

// Data Sources
import '../../data/datasources/verse_local_datasource.dart';
import '../../data/datasources/issue_local_datasource.dart';
import '../../data/datasources/daily_verse_local_datasource.dart';
import '../../data/datasources/favorites_local_datasource.dart';
import '../../data/datasources/settings_local_datasource.dart';

// Repositories
import '../../data/repositories/verse_repository_impl.dart';
import '../../data/repositories/issue_repository_impl.dart';
import '../../data/repositories/daily_verse_repository_impl.dart';
import '../../data/repositories/favorites_repository_impl.dart';
import '../../data/repositories/settings_repository_impl.dart';

// Use Cases
import '../../domain/repositories/daily_verse_repository.dart';
import '../../domain/repositories/favorites_repository.dart';
import '../../domain/repositories/issue_repository.dart';
import '../../domain/repositories/settings_repository.dart';
import '../../domain/repositories/verse_repository.dart';
import '../../domain/usecases/get_verses_for_issue.dart';
import '../../domain/usecases/get_issues.dart';
import '../../domain/usecases/get_daily_verse.dart';
import '../../domain/usecases/get_random_verse.dart';
import '../../domain/usecases/favorites_usecases.dart';
import '../../domain/usecases/settings_usecases.dart';

// BLoCs
import '../../presentation/blocs/daily_verse/daily_verse_bloc.dart';
import '../../presentation/blocs/issues/issues_bloc.dart';
import '../../presentation/blocs/verses/verses_bloc.dart';
import '../../presentation/blocs/favorites/favorites_bloc.dart';
import '../../presentation/blocs/settings/settings_bloc.dart';
import '../../presentation/blocs/random_verse/random_verse_bloc.dart';

final sl = GetIt.instance;

Future<void> init() async {
  // BLoCs
  sl.registerFactory(() => DailyVerseBloc(
    getDailyVerse: sl(),
    getRandomVerse: sl(),
  ));

  sl.registerFactory(() => IssuesBloc(getIssues: sl()));

  sl.registerFactory(() => VersesBloc(getVersesForIssue: sl()));

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

  sl.registerFactory(() => RandomVerseBloc(
    getRandomVerse: sl(),
  ));

  // Use Cases
  sl.registerLazySingleton(() => GetDailyVerse(sl()));
  sl.registerLazySingleton(() => GetRandomVerse(sl()));
  sl.registerLazySingleton(() => GetIssues(sl()));
  sl.registerLazySingleton(() => GetVersesForIssue(sl()));
  sl.registerLazySingleton(() => GetFavoriteVerses(sl()));
  sl.registerLazySingleton(() => ToggleFavorite(sl()));
  sl.registerLazySingleton(() => GetSettings(sl()));
  sl.registerLazySingleton(() => UpdateBibleVersion(sl()));
  sl.registerLazySingleton(() => ToggleNotifications(sl()));
  sl.registerLazySingleton(() => UpdateNotificationTime(sl()));
  sl.registerLazySingleton(() => ToggleTheme(sl()));

  // Repositories
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

  // Data Sources
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
}