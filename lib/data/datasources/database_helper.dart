import 'dart:io';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:flutter/services.dart';

class DatabaseHelper {
  static Database? _database;
  static const String _databaseName = 'Life_Issues.db';

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

    final exists = await databaseExists(path);

    if (exists) {
      // Detect old schema: old bible_verses had 'kjv' column, new has 'version'.
      // If version column is missing, delete and re-copy the updated asset DB.
      try {
        final db = await openDatabase(path, readOnly: true);
        await db.rawQuery(
            'SELECT $columnBibleVersion FROM $tableBibleVerses LIMIT 1');
        await db.close();
        // New schema detected — proceed normally.
      } catch (_) {
        // Old schema — remove and replace with new asset DB.
        await deleteDatabase(path);
      }
    }

    if (!await databaseExists(path)) {
      try {
        await Directory(dirname(path)).create(recursive: true);
      } catch (_) {}
      final ByteData data =
          await rootBundle.load('assets/databases/$_databaseName');
      final List<int> bytes =
          data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes);
      await File(path).writeAsBytes(bytes, flush: true);
    }

    return await openDatabase(path, readOnly: false);
  }

  static Future<void> close() async {
    final db = _database;
    if (db != null) {
      await db.close();
      _database = null;
    }
  }
}
