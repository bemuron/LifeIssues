import '../models/verse_model.dart';
import 'database_helper.dart';

abstract class DailyVerseLocalDataSource {
  Future<VerseModel> getDailyVerse();
  Future<VerseModel> getRandomVerse();
  Future<void> ensureDailyVersesExist();
}

class DailyVerseLocalDataSourceImpl implements DailyVerseLocalDataSource {
  // ── Shared: group flat rows into VerseModels ────────────────────────────
  List<VerseModel> _groupRows(List<Map<String, dynamic>> maps) {
    final order = <int>[];
    final groups = <int, List<Map<String, dynamic>>>{};
    for (final row in maps) {
      final id = row['canonical_id'] as int;
      if (!groups.containsKey(id)) order.add(id);
      groups.putIfAbsent(id, () => []).add(row);
    }
    return order
        .map((id) => VerseModel.fromGroupedRows(
              canonicalId: id,
              rows: groups[id]!,
            ))
        .toList();
  }

  // ── Base SELECT columns ─────────────────────────────────────────────────
  static const String _verseColumns = '''
      bv2.${DatabaseHelper.columnId},
      bv2.${DatabaseHelper.columnBibleReference} AS reference,
      bv2.${DatabaseHelper.columnBibleBook}      AS book,
      bv2.${DatabaseHelper.columnBibleChapter}   AS chapter,
      bv2.${DatabaseHelper.columnBibleVerseNum}  AS verse_num,
      bv2.${DatabaseHelper.columnBibleText}      AS text,
      bv2.${DatabaseHelper.columnBibleVersion}   AS version
  ''';

  /// Ensures that daily verses exist for today and tomorrow.
  @override
  Future<void> ensureDailyVersesExist() async {
    try {
      final db = await DatabaseHelper.database;
      final today = _getDateString(DateTime.now());
      final tomorrow =
          _getDateString(DateTime.now().add(const Duration(days: 1)));

      if (!await _verseExistsForDate(today)) {
        await _createDailyVerseForDate(today);
      }
      if (!await _verseExistsForDate(tomorrow)) {
        await _createDailyVerseForDate(tomorrow);
      }

      // Clean up entries older than yesterday.
      final yesterday =
          _getDateString(DateTime.now().subtract(const Duration(days: 1)));
      await db.delete(
        DatabaseHelper.tableDailyVerses,
        where: '${DatabaseHelper.columnNotifyDate} < ?',
        whereArgs: [yesterday],
      );
    } catch (e) {
      print('Error ensuring daily verses exist: $e');
    }
  }

  Future<bool> _verseExistsForDate(String date) async {
    final db = await DatabaseHelper.database;
    final result = await db.query(
      DatabaseHelper.tableDailyVerses,
      where: '${DatabaseHelper.columnNotifyDate} = ?',
      whereArgs: [date],
      limit: 1,
    );
    return result.isNotEmpty;
  }

  /// Picks a random KJV verse (avoiding the last 30 days) and stores it.
  Future<void> _createDailyVerseForDate(String date) async {
    try {
      final db = await DatabaseHelper.database;
      final cutoffDate =
          _getDateString(DateTime.now().subtract(const Duration(days: 30)));

      // Restrict to KJV rows (canonical rows) only.
      var rows = await db.rawQuery('''
        SELECT ${DatabaseHelper.columnId} AS verse_id
        FROM ${DatabaseHelper.tableBibleVerses}
        WHERE ${DatabaseHelper.columnBibleVersion} = 'KJV'
          AND ${DatabaseHelper.columnId} NOT IN (
            SELECT ${DatabaseHelper.columnVerseId}
            FROM ${DatabaseHelper.tableDailyVerses}
            WHERE ${DatabaseHelper.columnNotifyDate} >= ?
          )
        ORDER BY RANDOM()
        LIMIT 1
      ''', [cutoffDate]);

      // Fallback: any KJV verse if all were used recently.
      if (rows.isEmpty) {
        rows = await db.rawQuery('''
          SELECT ${DatabaseHelper.columnId} AS verse_id
          FROM ${DatabaseHelper.tableBibleVerses}
          WHERE ${DatabaseHelper.columnBibleVersion} = 'KJV'
          ORDER BY RANDOM()
          LIMIT 1
        ''');
      }

      if (rows.isNotEmpty) {
        final verseId = rows.first['verse_id'] as int;
        await db.insert(
          DatabaseHelper.tableDailyVerses,
          {
            DatabaseHelper.columnVerseId: verseId,
            DatabaseHelper.columnNotifyDate: date,
          },
        );
      }
    } catch (e) {
      print('Error creating daily verse for $date: $e');
      rethrow;
    }
  }

  String _getDateString(DateTime date) =>
      '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';

  @override
  Future<VerseModel> getDailyVerse() async {
    try {
      await ensureDailyVersesExist();

      final db = await DatabaseHelper.database;
      final today = _getDateString(DateTime.now());

      // bv1 = canonical KJV row stored in daily_verses; bv2 = all translations.
      final maps = await db.rawQuery('''
        SELECT
          bv1.${DatabaseHelper.columnId}              AS canonical_id,
          $_verseColumns,
          0                                           AS is_favorite,
          i.${DatabaseHelper.columnName}              AS issue_name
        FROM  ${DatabaseHelper.tableDailyVerses} dv
        JOIN  ${DatabaseHelper.tableBibleVerses} bv1
          ON  bv1.${DatabaseHelper.columnId} = dv.${DatabaseHelper.columnVerseId}
        JOIN  ${DatabaseHelper.tableBibleVerses} bv2
          ON  bv2.${DatabaseHelper.columnBibleBook}     = bv1.${DatabaseHelper.columnBibleBook}
         AND  bv2.${DatabaseHelper.columnBibleChapter}  = bv1.${DatabaseHelper.columnBibleChapter}
         AND  bv2.${DatabaseHelper.columnBibleVerseNum} = bv1.${DatabaseHelper.columnBibleVerseNum}
        LEFT JOIN ${DatabaseHelper.tableIssuesVerses} iv
          ON  iv.${DatabaseHelper.columnVerseId} = bv1.${DatabaseHelper.columnId}
        LEFT JOIN ${DatabaseHelper.tableIssues} i
          ON  i.${DatabaseHelper.columnIssueId} = iv.${DatabaseHelper.columnIssueIdFk}
        WHERE dv.${DatabaseHelper.columnNotifyDate} = ?
        ORDER BY bv2.${DatabaseHelper.columnBibleVersion}
      ''', [today]);

      if (maps.isNotEmpty) {
        return _groupRows(maps).first;
      }

      return await getRandomVerse();
    } catch (e) {
      throw Exception('Failed to get daily verse: $e');
    }
  }

  @override
  Future<VerseModel> getRandomVerse() async {
    try {
      final db = await DatabaseHelper.database;

      // Step 1: pick a random KJV row.
      final pivot = await db.rawQuery('''
        SELECT ${DatabaseHelper.columnId} AS canonical_id
        FROM   ${DatabaseHelper.tableBibleVerses}
        WHERE  ${DatabaseHelper.columnBibleVersion} = 'KJV'
        ORDER  BY RANDOM()
        LIMIT  1
      ''');

      if (pivot.isEmpty) {
        throw Exception(
            'No verses in database. Ensure Life_Issues.db is in assets/databases/');
      }

      final canonicalId = pivot.first['canonical_id'] as int;

      // Step 2: load all translations of that verse.
      final maps = await db.rawQuery('''
        SELECT
          bv1.${DatabaseHelper.columnId}              AS canonical_id,
          $_verseColumns,
          0                                           AS is_favorite,
          i.${DatabaseHelper.columnName}              AS issue_name
        FROM  ${DatabaseHelper.tableBibleVerses} bv1
        JOIN  ${DatabaseHelper.tableBibleVerses} bv2
          ON  bv2.${DatabaseHelper.columnBibleBook}     = bv1.${DatabaseHelper.columnBibleBook}
         AND  bv2.${DatabaseHelper.columnBibleChapter}  = bv1.${DatabaseHelper.columnBibleChapter}
         AND  bv2.${DatabaseHelper.columnBibleVerseNum} = bv1.${DatabaseHelper.columnBibleVerseNum}
        LEFT JOIN ${DatabaseHelper.tableIssuesVerses} iv
          ON  iv.${DatabaseHelper.columnVerseId} = bv1.${DatabaseHelper.columnId}
        LEFT JOIN ${DatabaseHelper.tableIssues} i
          ON  i.${DatabaseHelper.columnIssueId} = iv.${DatabaseHelper.columnIssueIdFk}
        WHERE bv1.${DatabaseHelper.columnId} = ?
        ORDER BY bv2.${DatabaseHelper.columnBibleVersion}
      ''', [canonicalId]);

      if (maps.isEmpty) throw Exception('Query returned no results');
      return _groupRows(maps).first;
    } catch (e) {
      throw Exception('Failed to get random verse: $e');
    }
  }
}
