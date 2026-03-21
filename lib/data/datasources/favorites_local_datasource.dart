import '../models/verse_model.dart';
import 'database_helper.dart';

abstract class FavoritesLocalDataSource {
  Future<List<VerseModel>> getFavoriteVerses();
  Future<void> toggleFavorite(int verseId, bool isFavorite);
}

class FavoritesLocalDataSourceImpl implements FavoritesLocalDataSource {
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

  @override
  Future<List<VerseModel>> getFavoriteVerses() async {
    try {
      final db = await DatabaseHelper.database;

      // bv1 = canonical KJV row stored in issues_verses; bv2 = all translations.
      final maps = await db.rawQuery('''
        SELECT
          iv.${DatabaseHelper.columnVerseId}          AS canonical_id,
          bv2.${DatabaseHelper.columnId},
          bv2.${DatabaseHelper.columnBibleReference}  AS reference,
          bv2.${DatabaseHelper.columnBibleBook}       AS book,
          bv2.${DatabaseHelper.columnBibleChapter}    AS chapter,
          bv2.${DatabaseHelper.columnBibleVerseNum}   AS verse_num,
          bv2.${DatabaseHelper.columnBibleText}       AS text,
          bv2.${DatabaseHelper.columnBibleVersion}    AS version,
          1                                           AS is_favorite,
          i.${DatabaseHelper.columnName}              AS issue_name
        FROM  ${DatabaseHelper.tableIssuesVerses} iv
        JOIN  ${DatabaseHelper.tableBibleVerses}  bv1
          ON  bv1.${DatabaseHelper.columnId} = iv.${DatabaseHelper.columnVerseId}
        JOIN  ${DatabaseHelper.tableBibleVerses}  bv2
          ON  bv2.${DatabaseHelper.columnBibleBook}     = bv1.${DatabaseHelper.columnBibleBook}
         AND  bv2.${DatabaseHelper.columnBibleChapter}  = bv1.${DatabaseHelper.columnBibleChapter}
         AND  bv2.${DatabaseHelper.columnBibleVerseNum} = bv1.${DatabaseHelper.columnBibleVerseNum}
        LEFT JOIN ${DatabaseHelper.tableIssues} i
          ON  i.${DatabaseHelper.columnIssueId} = iv.${DatabaseHelper.columnIssueIdFk}
        WHERE iv.${DatabaseHelper.columnIsFavoriteVerse} = 1
        ORDER BY bv1.${DatabaseHelper.columnBibleReference} ASC,
                 bv2.${DatabaseHelper.columnBibleVersion}   ASC
      ''');

      return _groupRows(maps);
    } catch (e) {
      throw Exception('Failed to get favorite verses: $e');
    }
  }

  @override
  Future<void> toggleFavorite(int verseId, bool isFavorite) async {
    try {
      final db = await DatabaseHelper.database;
      await db.update(
        DatabaseHelper.tableIssuesVerses,
        {DatabaseHelper.columnIsFavoriteVerse: isFavorite ? 1 : 0},
        where: '${DatabaseHelper.columnVerseId} = ?',
        whereArgs: [verseId],
      );
    } catch (e) {
      throw Exception('Failed to toggle favorite: $e');
    }
  }
}
