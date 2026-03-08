import '../models/verse_model.dart';
import 'database_helper.dart';

abstract class VerseLocalDataSource {
  Future<List<VerseModel>> getVersesForIssue(int issueId);
  Future<VerseModel> getVerseById(int verseId);
  Future<VerseModel> getRandomVerse();
}

class VerseLocalDataSourceImpl implements VerseLocalDataSource {
  @override
  Future<List<VerseModel>> getVersesForIssue(int issueId) async {
    try {
      final db = await DatabaseHelper.database;

      final List<Map<String, dynamic>> maps = await db.rawQuery('''
        SELECT 
          v.${DatabaseHelper.columnId},
          v.${DatabaseHelper.columnVerse},
          v.${DatabaseHelper.columnKjv},
          v.${DatabaseHelper.columnMsg},
          v.${DatabaseHelper.columnAmp},
          CASE WHEN iv.${DatabaseHelper.columnIsFavoriteVerse} = 1 THEN 1 ELSE 0 END as is_favorite
        FROM ${DatabaseHelper.tableBibleVerses} v
        INNER JOIN ${DatabaseHelper.tableIssuesVerses} iv 
          ON v.${DatabaseHelper.columnId} = iv.${DatabaseHelper.columnVerseId}
        WHERE iv.${DatabaseHelper.columnIssueIdFk} = ?
      ''', [issueId.toString()]);

      return maps.map((map) => VerseModel.fromMap(map)).toList();
    } catch (e) {
      throw Exception('Failed to get verses for issue: $e');
    }
  }

  @override
  Future<VerseModel> getVerseById(int verseId) async {
    try {
      final db = await DatabaseHelper.database;

      final List<Map<String, dynamic>> maps = await db.rawQuery('''
        SELECT 
          v.${DatabaseHelper.columnId},
          v.${DatabaseHelper.columnVerse},
          v.${DatabaseHelper.columnKjv},
          v.${DatabaseHelper.columnMsg},
          v.${DatabaseHelper.columnAmp},
          CASE WHEN EXISTS (
            SELECT 1 FROM ${DatabaseHelper.tableIssuesVerses} iv 
            WHERE iv.${DatabaseHelper.columnVerseId} = v.${DatabaseHelper.columnId} 
              AND iv.${DatabaseHelper.columnIsFavoriteVerse} = 1
          ) THEN 1 ELSE 0 END as is_favorite
        FROM ${DatabaseHelper.tableBibleVerses} v
        WHERE v.${DatabaseHelper.columnId} = ?
      ''', [verseId]);

      if (maps.isEmpty) {
        throw Exception('Verse not found');
      }

      return VerseModel.fromMap(maps.first);
    } catch (e) {
      throw Exception('Failed to get verse by ID: $e');
    }
  }

  @override
  Future<VerseModel> getRandomVerse() async {
    try {
      final db = await DatabaseHelper.database;

      final List<Map<String, dynamic>> maps = await db.rawQuery('''
        SELECT 
          v.${DatabaseHelper.columnId},
          v.${DatabaseHelper.columnVerse},
          v.${DatabaseHelper.columnKjv},
          v.${DatabaseHelper.columnMsg},
          v.${DatabaseHelper.columnAmp},
          0 as is_favorite
        FROM ${DatabaseHelper.tableBibleVerses} v
        ORDER BY RANDOM()
        LIMIT 1
      ''');

      if (maps.isEmpty) {
        throw Exception('No verses available');
      }

      return VerseModel.fromMap(maps.first);
    } catch (e) {
      throw Exception('Failed to get random verse: $e');
    }
  }
}