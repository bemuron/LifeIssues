import '../models/verse_model.dart';
import 'database_helper.dart';

abstract class FavoritesLocalDataSource {
  Future<List<VerseModel>> getFavoriteVerses();
  Future<void> toggleFavorite(int verseId, bool isFavorite);
}

class FavoritesLocalDataSourceImpl implements FavoritesLocalDataSource {
  @override
  Future<List<VerseModel>> getFavoriteVerses() async {
    try {
      final db = await DatabaseHelper.database;

      final List<Map<String, dynamic>> maps = await db.rawQuery('''
        SELECT 
          v.${DatabaseHelper.columnId},
          v.${DatabaseHelper.columnVerse},
          v.${DatabaseHelper.columnKjv},
          v.${DatabaseHelper.columnMsg},
          v.${DatabaseHelper.columnAmp},
          1 as is_favorite,
          i.${DatabaseHelper.columnName} as issue_name,
          i.${DatabaseHelper.columnIssueId}
        FROM ${DatabaseHelper.tableBibleVerses} v
        INNER JOIN ${DatabaseHelper.tableIssuesVerses} iv 
          ON v.${DatabaseHelper.columnId} = iv.${DatabaseHelper.columnVerseId}
        LEFT JOIN ${DatabaseHelper.tableIssues} i ON iv.${DatabaseHelper.columnIssueIdFk} = i.${DatabaseHelper.columnIssueId}
        WHERE iv.${DatabaseHelper.columnIsFavoriteVerse} = 1
        GROUP BY v.${DatabaseHelper.columnId}
        ORDER BY v.${DatabaseHelper.columnVerse} ASC
      ''');

      return maps.map((map) => VerseModel.fromMap(map)).toList();
    } catch (e) {
      throw Exception('Failed to get favorite verses: $e');
    }
  }

  @override
  Future<void> toggleFavorite(int verseId, bool isFavorite) async {
    try {
      final db = await DatabaseHelper.database;

      // Update all entries in issues_verses table for this verse
      await db.update(
        DatabaseHelper.tableIssuesVerses,
        {DatabaseHelper.columnIsFavoriteVerse: isFavorite ? 1 : 0},
        where: '${DatabaseHelper.columnVerseId} = ?',
        whereArgs: [verseId.toString()],
      );
    } catch (e) {
      throw Exception('Failed to toggle favorite: $e');
    }
  }
}