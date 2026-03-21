import '../models/verse_model.dart';
import 'database_helper.dart';

abstract class VerseLocalDataSource {
  Future<List<VerseModel>> getVersesForIssue(int issueId);
  Future<VerseModel> getVerseById(int verseId);
  Future<VerseModel> getRandomVerseForHome();
}

class VerseLocalDataSourceImpl implements VerseLocalDataSource {
  // ── Shared: group flat rows into VerseModels ────────────────────────────
  List<VerseModel> _groupRows(List<Map<String, dynamic>> maps) {
    // Preserve the original ordering (first occurrence of each canonical_id).
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

  // ── Base SELECT columns (aliased consistently) ──────────────────────────
  static const String _verseColumns = '''
      bv2.${DatabaseHelper.columnId},
      bv2.${DatabaseHelper.columnBibleReference} AS reference,
      bv2.${DatabaseHelper.columnBibleBook}      AS book,
      bv2.${DatabaseHelper.columnBibleChapter}   AS chapter,
      bv2.${DatabaseHelper.columnBibleVerseNum}  AS verse_num,
      bv2.${DatabaseHelper.columnBibleText}      AS text,
      bv2.${DatabaseHelper.columnBibleVersion}   AS version
  ''';

  @override
  Future<List<VerseModel>> getVersesForIssue(int issueId) async {
    try {
      final db = await DatabaseHelper.database;

      final maps = await db.rawQuery('''
        SELECT
          iv.${DatabaseHelper.columnVerseId}          AS canonical_id,
          $_verseColumns,
          COALESCE(iv.${DatabaseHelper.columnIsFavoriteVerse}, 0) AS is_favorite,
          i.${DatabaseHelper.columnName}              AS issue_name
        FROM  ${DatabaseHelper.tableIssuesVerses} iv
        JOIN  ${DatabaseHelper.tableBibleVerses}  bv1
          ON  bv1.${DatabaseHelper.columnId} = iv.${DatabaseHelper.columnVerseId}
        JOIN  ${DatabaseHelper.tableBibleVerses}  bv2
          ON  bv2.${DatabaseHelper.columnBibleBook}      = bv1.${DatabaseHelper.columnBibleBook}
         AND  bv2.${DatabaseHelper.columnBibleChapter}   = bv1.${DatabaseHelper.columnBibleChapter}
         AND  bv2.${DatabaseHelper.columnBibleVerseNum}  = bv1.${DatabaseHelper.columnBibleVerseNum}
        JOIN  ${DatabaseHelper.tableIssues} i
          ON  i.${DatabaseHelper.columnIssueId} = iv.${DatabaseHelper.columnIssueIdFk}
        WHERE iv.${DatabaseHelper.columnIssueIdFk} = ?
        ORDER BY iv.${DatabaseHelper.columnIssuesVersesId},
                 bv2.${DatabaseHelper.columnBibleVersion}
      ''', [issueId]);

      return _groupRows(maps);
    } catch (e) {
      throw Exception('Failed to get verses for issue: $e');
    }
  }

  @override
  Future<VerseModel> getVerseById(int verseId) async {
    try {
      final db = await DatabaseHelper.database;

      // Load all translations of the verse identified by verseId.
      final maps = await db.rawQuery('''
        SELECT
          bv1.${DatabaseHelper.columnId}               AS canonical_id,
          $_verseColumns,
          CASE WHEN EXISTS (
            SELECT 1 FROM ${DatabaseHelper.tableIssuesVerses} iv
            WHERE iv.${DatabaseHelper.columnVerseId} = bv1.${DatabaseHelper.columnId}
              AND iv.${DatabaseHelper.columnIsFavoriteVerse} = 1
          ) THEN 1 ELSE 0 END                          AS is_favorite
        FROM ${DatabaseHelper.tableBibleVerses} bv1
        JOIN ${DatabaseHelper.tableBibleVerses} bv2
          ON  bv2.${DatabaseHelper.columnBibleBook}     = bv1.${DatabaseHelper.columnBibleBook}
         AND  bv2.${DatabaseHelper.columnBibleChapter}  = bv1.${DatabaseHelper.columnBibleChapter}
         AND  bv2.${DatabaseHelper.columnBibleVerseNum} = bv1.${DatabaseHelper.columnBibleVerseNum}
        WHERE bv1.${DatabaseHelper.columnId} = ?
        ORDER BY bv2.${DatabaseHelper.columnBibleVersion}
      ''', [verseId]);

      if (maps.isEmpty) throw Exception('Verse not found');
      return _groupRows(maps).first;
    } catch (e) {
      throw Exception('Failed to get verse by ID: $e');
    }
  }

  @override
  Future<VerseModel> getRandomVerseForHome() async {
    try {
      final db = await DatabaseHelper.database;

      // Step 1: pick a random canonical KJV row.
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
