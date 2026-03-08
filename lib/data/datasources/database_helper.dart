import 'dart:io';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:flutter/services.dart';

class DatabaseHelper {
  static Database? _database;
  static const String _databaseName = 'Life_Issues.db';

  // Table names (matching actual database)
  static const String tableBibleVerses = 'bible_verses';
  static const String tableIssues = 'issues';
  static const String tableIssuesVerses = 'issues_verses';
  static const String tableFavourites = 'favourites';
  static const String tableDailyVerses = 'daily_verses';

  // bible_verses columns
  static const String columnId = '_id';
  static const String columnVerse = 'verse';
  static const String columnKjv = 'kjv';
  static const String columnMsg = 'msg';
  static const String columnAmp = 'amp';

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

    // Check if database exists
    final exists = await databaseExists(path);

    if (!exists) {
      // Copy from assets
      try {
        await Directory(dirname(path)).create(recursive: true);
      } catch (_) {}

      ByteData data = await rootBundle.load('assets/databases/$_databaseName');
      List<int> bytes =
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