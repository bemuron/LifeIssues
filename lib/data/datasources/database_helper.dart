import 'dart:io';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

class DatabaseHelper {
  static Database? _database;
  static const String _databaseName = 'Life_Issues.db';

  /// Increment this whenever the shipped asset DB changes.
  /// Devices with a stored version lower than this will have their DB replaced.
  static const int _assetDbVersion = 2;

  // Table names
  static const String tableBibleVerses = 'bible_verses';
  static const String tableIssues = 'issues';
  static const String tableIssuesVerses = 'issues_verses';
  static const String tableFavourites = 'favourites';
  static const String tableDailyVerses = 'daily_verses';

  // bible_verses columns (new narrow-table schema — one row per translation)
  static const String columnId = '_id';
  static const String columnBibleReference = 'reference'; // "John 3:16"
  static const String columnBibleBook = 'book';
  static const String columnBibleChapter = 'chapter';
  static const String columnBibleVerseNum = 'verse_num';
  static const String columnBibleText = 'text';
  static const String columnBibleVersion = 'version'; // 'KJV', 'MSG', 'AMP', …

  // issues columns
  static const String columnIssueId = 'issue_id';
  static const String columnName = 'name';
  static const String columnDescription = 'description';
  static const String columnIsFavorite = 'is_favorite';
  static const String columnImage = 'image';

  // issues_verses columns
  static const String columnIssuesVersesId = '_id';
  static const String columnVerseId = 'verse_id';
  static const String columnIssueIdFk = 'issue_id';
  static const String columnIsFavoriteVerse = 'is_favorite';
  static const String columnSortOrder = 'sort_order';

  // daily_verses columns
  static const String columnNotifyDate = 'notify_date';

  static Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  static Future<Database> _initDatabase() async {
    final databasesPath = await getDatabasesPath();
    final path = join(databasesPath, _databaseName);
    final prefs = await SharedPreferences.getInstance();
    final storedVersion = prefs.getInt('db_asset_version') ?? 0;

    if (await databaseExists(path)) {
      if (storedVersion < _assetDbVersion) {
        // Back up user favourites before wiping the old DB.
        final favoriteVerseIds = await _backupFavorites(path);

        await deleteDatabase(path);

        // Copy fresh asset DB.
        await _copyAssetDatabase(path);

        // Restore favourites into the new DB.
        if (favoriteVerseIds.isNotEmpty) {
          await _restoreFavorites(path, favoriteVerseIds);
        }

        await prefs.setInt('db_asset_version', _assetDbVersion);
      }
    } else {
      await _copyAssetDatabase(path);
      await prefs.setInt('db_asset_version', _assetDbVersion);
    }

    return await openDatabase(path, readOnly: false);
  }

  /// Returns the list of verse_ids that the user had marked as favourite.
  static Future<List<int>> _backupFavorites(String path) async {
    try {
      final db = await openDatabase(path, readOnly: true);
      final rows = await db.query(
        tableIssuesVerses,
        columns: [columnVerseId],
        where: '$columnIsFavoriteVerse = 1',
      );
      await db.close();
      return rows.map((r) => r[columnVerseId] as int).toList();
    } catch (_) {
      // If the old DB is unreadable just proceed without restoring.
      return [];
    }
  }

  /// Re-applies is_favorite = 1 for each verse_id in the freshly copied DB.
  static Future<void> _restoreFavorites(
      String path, List<int> verseIds) async {
    final db = await openDatabase(path, readOnly: false);
    final batch = db.batch();
    for (final id in verseIds) {
      batch.update(
        tableIssuesVerses,
        {columnIsFavoriteVerse: 1},
        where: '$columnVerseId = ?',
        whereArgs: [id],
      );
    }
    await batch.commit(noResult: true);
    await db.close();
  }

  static Future<void> _copyAssetDatabase(String path) async {
    try {
      await Directory(dirname(path)).create(recursive: true);
    } catch (_) {}
    final ByteData data =
        await rootBundle.load('assets/databases/$_databaseName');
    final List<int> bytes =
        data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes);
    await File(path).writeAsBytes(bytes, flush: true);
  }

  static Future<void> close() async {
    final db = _database;
    if (db != null) {
      await db.close();
      _database = null;
    }
  }
}
