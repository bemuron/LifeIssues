// lib/core/services/database_sync_service.dart

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:sqflite/sqflite.dart';
import '../config/api_config.dart';
import '../network/api_client.dart';
import '../../data/datasources/database_helper.dart';

class DatabaseSyncService {
  final ApiClient apiClient;
  static const String _lastSyncKey = 'last_db_sync';

  /// The timestamp of the bundled asset database.
  /// Update this whenever you ship a new asset DB in the app.
  static const String bundleTimestamp = '2026-03-20T00:00:00.000000Z';

  DatabaseSyncService({required this.apiClient});

  /// Call on app startup.
  /// First run: uses [bundleTimestamp] as the baseline — only fetches records
  /// added/changed after the bundled DB was built.
  /// Subsequent runs: uses the stored last-sync timestamp.
  Future<void> sync() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final lastSync = prefs.getString(_lastSyncKey) ?? bundleTimestamp;
      await _incrementalSync(lastSync, prefs);
    } catch (e) {
      debugPrint('⚠️ DB sync failed: $e');
    }
  }

  Future<void> _incrementalSync(String since, SharedPreferences prefs) async {
    // First check metadata to see if anything has changed
    try {
      final metaResponse = await apiClient.get(ApiConfig.syncMetadata);
      final lastUpdates = metaResponse.data['last_updates'] as Map<String, dynamic>;

      final tables = <String>[];
      for (final table in ['issues', 'bible_verses', 'issues_verses']) {
        final serverUpdated = lastUpdates[table] as String?;
        if (serverUpdated != null && serverUpdated.compareTo(since) > 0) {
          tables.add(table);
        }
      }

      if (tables.isEmpty) {
        debugPrint('✅ DB is up to date, no sync needed');
        return;
      }

      debugPrint('📥 Syncing tables: $tables since $since');
      final response = await apiClient.get(
        ApiConfig.syncDatabase,
        queryParameters: {
          'since': since,
          'tables': tables,
        },
      );

      final data = response.data['data'] as Map<String, dynamic>;
      final timestamp = response.data['timestamp'] as String;

      final db = await DatabaseHelper.database;
      await db.transaction((txn) async {
        if (tables.contains('issues') && data['issues'] != null) {
          await _upsertIssues(txn, data['issues'] as List);
        }
        if (tables.contains('bible_verses') && data['bible_verses'] != null) {
          await _upsertBibleVerses(txn, data['bible_verses'] as List);
        }
        if (tables.contains('issues_verses') && data['issues_verses'] != null) {
          await _upsertIssuesVerses(txn, data['issues_verses'] as List);
        }
      });

      await prefs.setString(_lastSyncKey, timestamp);
      debugPrint('✅ Incremental DB sync complete');
    } catch (e) {
      debugPrint('⚠️ Incremental sync failed: $e');
    }
  }

  Future<void> _upsertIssues(DatabaseExecutor db, List rows) async {
    for (final row in rows) {
      final map = row as Map<String, dynamic>;
      await db.insert(
        DatabaseHelper.tableIssues,
        {
          DatabaseHelper.columnIssueId: map['issue_id'] ?? map['id'],
          DatabaseHelper.columnName: map['name'] ?? '',
          DatabaseHelper.columnDescription: map['description'] ?? '',
          DatabaseHelper.columnIsFavorite: map['is_favorite'] ?? 0,
          DatabaseHelper.columnImage: map['image'],
        },
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    }
    debugPrint('  ↳ Upserted ${rows.length} issues');
  }

  Future<void> _upsertBibleVerses(DatabaseExecutor db, List rows) async {
    for (final row in rows) {
      final map = row as Map<String, dynamic>;
      final book     = (map['book']    as String?) ?? '';
      final chapter  = (map['chapter'] as num?)?.toInt() ?? 0;
      final verseNum = ((map['verse_num'] ?? map['verse']) as num?)?.toInt() ?? 0;
      final version  = ((map['version'] as String?) ?? 'KJV').toUpperCase();
      final text     = (map['text']      as String?) ?? '';
      final ref      = (map['reference'] as String?) ?? '';

      // Update existing row by natural key — never touch _id (IDs differ between
      // backend and mobile after the narrow-table migration).
      final updated = await db.update(
        DatabaseHelper.tableBibleVerses,
        {
          DatabaseHelper.columnBibleReference: ref,
          DatabaseHelper.columnBibleText: text,
        },
        where: '${DatabaseHelper.columnBibleBook}=? '
            'AND ${DatabaseHelper.columnBibleChapter}=? '
            'AND ${DatabaseHelper.columnBibleVerseNum}=? '
            'AND ${DatabaseHelper.columnBibleVersion}=?',
        whereArgs: [book, chapter, verseNum, version],
      );

      // Row doesn't exist yet — insert without specifying _id (auto-increment).
      if (updated == 0) {
        await db.insert(
          DatabaseHelper.tableBibleVerses,
          {
            DatabaseHelper.columnBibleReference: ref,
            DatabaseHelper.columnBibleBook: book,
            DatabaseHelper.columnBibleChapter: chapter,
            DatabaseHelper.columnBibleVerseNum: verseNum,
            DatabaseHelper.columnBibleText: text,
            DatabaseHelper.columnBibleVersion: version,
          },
          conflictAlgorithm: ConflictAlgorithm.ignore,
        );
      }
    }
    debugPrint('  ↳ Upserted ${rows.length} bible verses');
  }

  Future<void> _upsertIssuesVerses(DatabaseExecutor db, List rows) async {
    for (final row in rows) {
      final map = row as Map<String, dynamic>;

      // Resolve the local KJV _id by natural key when the backend includes
      // book/chapter/verse_num in the response (recommended).
      // Falls back to the raw verse_id if those fields are absent.
      int? localVerseId;
      if (map['book'] != null) {
        final verseNum = ((map['verse_num'] ?? map['verse']) as num?)?.toInt();
        final result = await db.query(
          DatabaseHelper.tableBibleVerses,
          columns: [DatabaseHelper.columnId],
          where: '${DatabaseHelper.columnBibleBook}=? '
              'AND ${DatabaseHelper.columnBibleChapter}=? '
              'AND ${DatabaseHelper.columnBibleVerseNum}=? '
              'AND ${DatabaseHelper.columnBibleVersion}=?',
          whereArgs: [map['book'], map['chapter'], verseNum, 'KJV'],
          limit: 1,
        );
        localVerseId = result.isNotEmpty
            ? result.first[DatabaseHelper.columnId] as int?
            : null;
      }

      final verseId = localVerseId ?? map['verse_id'];
      if (verseId == null) continue;

      await db.insert(
        DatabaseHelper.tableIssuesVerses,
        {
          DatabaseHelper.columnIssuesVersesId: map['_id'] ?? map['id'],
          DatabaseHelper.columnVerseId: verseId,
          DatabaseHelper.columnIssueIdFk: map['issue_id'],
          DatabaseHelper.columnIsFavoriteVerse: map['is_favorite'] ?? 0,
          DatabaseHelper.columnSortOrder: map['sort_order'] ?? 0,
        },
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    }
    debugPrint('  ↳ Upserted ${rows.length} issue-verse mappings');
  }
}
