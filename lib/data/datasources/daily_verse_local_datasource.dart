import '../models/verse_model.dart';
import 'database_helper.dart';

abstract class DailyVerseLocalDataSource {
  Future<VerseModel> getDailyVerse();
  Future<VerseModel> getRandomVerse();
  Future<void> ensureDailyVersesExist();
}

class DailyVerseLocalDataSourceImpl implements DailyVerseLocalDataSource {

  /// Ensures that daily verses exist for today and tomorrow
  /// This should be called when app starts and when getting daily verse
  @override
  Future<void> ensureDailyVersesExist() async {
    try {
      final db = await DatabaseHelper.database;
      final today = _getDateString(DateTime.now());
      final tomorrow = _getDateString(DateTime.now().add(const Duration(days: 1)));

      // Check if verse exists for today
      final todayExists = await _verseExistsForDate(today);
      if (!todayExists) {
        await _createDailyVerseForDate(today);
      }

      // Check if verse exists for tomorrow
      final tomorrowExists = await _verseExistsForDate(tomorrow);
      if (!tomorrowExists) {
        await _createDailyVerseForDate(tomorrow);
      }

      // Clean up old verses (older than yesterday)
      final yesterday = _getDateString(DateTime.now().subtract(const Duration(days: 1)));
      await db.delete(
        DatabaseHelper.tableDailyVerses,
        where: '${DatabaseHelper.columnNotifyDate} < ?',
        whereArgs: [yesterday],
      );
    } catch (e) {
      print('Error ensuring daily verses exist: $e');
    }
  }

  /// Check if a verse exists for a specific date
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

  /// Create a daily verse entry for a specific date
  Future<void> _createDailyVerseForDate(String date) async {
    try {
      final db = await DatabaseHelper.database;

      // Get a random verse that hasn't been used recently (last 30 days)
      final cutoffDate = _getDateString(
          DateTime.now().subtract(const Duration(days: 30))
      );

      final List<Map<String, dynamic>> randomVerse = await db.rawQuery('''
        SELECT v._id as verse_id
        FROM ${DatabaseHelper.tableBibleVerses} v
        WHERE v._id NOT IN (
          SELECT dv.${DatabaseHelper.columnVerseId}
          FROM ${DatabaseHelper.tableDailyVerses} dv
          WHERE dv.${DatabaseHelper.columnNotifyDate} >= ?
        )
        AND v.${DatabaseHelper.columnKjv} IS NOT NULL
        ORDER BY RANDOM()
        LIMIT 1
      ''', [cutoffDate]);

      if (randomVerse.isNotEmpty) {
        final verseId = randomVerse.first['verse_id'] as int;

        // Insert into daily_verses table
        await db.insert(
          DatabaseHelper.tableDailyVerses,
          {
            DatabaseHelper.columnVerseId: verseId,
            DatabaseHelper.columnNotifyDate: date,
          },
        );

        print('Created daily verse for $date with verse ID: $verseId');
      } else {
        // If no verse found (all used recently), just pick any random verse
        final anyVerse = await db.rawQuery('''
          SELECT _id as verse_id
          FROM ${DatabaseHelper.tableBibleVerses}
          WHERE ${DatabaseHelper.columnKjv} IS NOT NULL
          ORDER BY RANDOM()
          LIMIT 1
        ''');

        if (anyVerse.isNotEmpty) {
          final verseId = anyVerse.first['verse_id'] as int;
          await db.insert(
            DatabaseHelper.tableDailyVerses,
            {
              DatabaseHelper.columnVerseId: verseId,
              DatabaseHelper.columnNotifyDate: date,
            },
          );
          print('Created daily verse for $date with verse ID: $verseId (from any)');
        }
      }
    } catch (e) {
      print('Error creating daily verse for $date: $e');
      rethrow;
    }
  }

  /// Get date string in YYYY-MM-DD format
  String _getDateString(DateTime date) {
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }

  @override
  Future<VerseModel> getDailyVerse() async {
    try {
      // First ensure verses exist for today and tomorrow
      await ensureDailyVersesExist();

      final db = await DatabaseHelper.database;
      final today = _getDateString(DateTime.now());

      // Get today's daily verse
      final List<Map<String, dynamic>> dailyMaps = await db.rawQuery('''
        SELECT 
          v.${DatabaseHelper.columnId},
          v.${DatabaseHelper.columnVerse},
          v.${DatabaseHelper.columnKjv},
          v.${DatabaseHelper.columnMsg},
          v.${DatabaseHelper.columnAmp},
          0 as is_favorite,
          i.${DatabaseHelper.columnName} as issue_name,
          i.${DatabaseHelper.columnIssueId}
        FROM ${DatabaseHelper.tableDailyVerses} dv
        INNER JOIN ${DatabaseHelper.tableBibleVerses} v 
          ON dv.${DatabaseHelper.columnVerseId} = v.${DatabaseHelper.columnId}
        LEFT JOIN ${DatabaseHelper.tableIssuesVerses} iv 
          ON v.${DatabaseHelper.columnId} = iv.${DatabaseHelper.columnVerseId}
        LEFT JOIN ${DatabaseHelper.tableIssues} i ON iv.${DatabaseHelper.columnIssueIdFk} = i.${DatabaseHelper.columnIssueId}
        WHERE dv.${DatabaseHelper.columnNotifyDate} = ?
        LIMIT 1
      ''', [today]);

      if (dailyMaps.isNotEmpty) {
        print('Found daily verse for $today');
        print('Text: $dailyMaps.first');
        return VerseModel.fromMap(dailyMaps.first);
      }

      // Fallback: This shouldn't happen, but if it does, return random verse
      print('No daily verse found for $today, using random verse');
      return await getRandomVerse();
    } catch (e) {
      throw Exception('Failed to get daily verse: $e');
    }
  }

  @override
  Future<VerseModel> getRandomVerse() async {
    try {
      final db = await DatabaseHelper.database;

      // First check if there are any verses
      final countResult = await db.rawQuery(
          'SELECT COUNT(*) as count FROM ${DatabaseHelper.tableBibleVerses}'
      );
      final count = countResult.first['count'] as int;

      if (count == 0) {
        throw Exception('No verses in database. Please ensure Life_Issues.db is copied to assets/databases/');
      }

      final List<Map<String, dynamic>> maps = await db.rawQuery('''
        SELECT 
          v.${DatabaseHelper.columnId},
          v.${DatabaseHelper.columnVerse},
          v.${DatabaseHelper.columnKjv},
          v.${DatabaseHelper.columnMsg},
          v.${DatabaseHelper.columnAmp},
          0 as is_favorite,
          i.${DatabaseHelper.columnName} as issue_name,
          i.${DatabaseHelper.columnIssueId}
        FROM ${DatabaseHelper.tableBibleVerses} v
        LEFT JOIN ${DatabaseHelper.tableIssuesVerses} iv 
          ON v.${DatabaseHelper.columnId} = iv.${DatabaseHelper.columnVerseId}
        LEFT JOIN ${DatabaseHelper.tableIssues} i ON iv.${DatabaseHelper.columnIssueIdFk} = i.${DatabaseHelper.columnIssueId}
        WHERE v.${DatabaseHelper.columnKjv} IS NOT NULL
        ORDER BY RANDOM()
        LIMIT 1
      ''');

      if (maps.isEmpty) {
        throw Exception('Query returned no results');
      }

      final firstMap = maps.first;

      // Log the data for debugging
      print('Random verse data: ${firstMap.toString()}');

      return VerseModel.fromMap(firstMap);
    } catch (e) {
      throw Exception('Failed to get random verse: $e');
    }
  }
}