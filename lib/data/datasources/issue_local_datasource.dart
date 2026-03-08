import '../models/issue_model.dart';
import 'database_helper.dart';

abstract class IssueLocalDataSource {
  Future<List<IssueModel>> getAllIssues();
  Future<IssueModel> getIssueById(int issueId);
}

class IssueLocalDataSourceImpl implements IssueLocalDataSource {
  @override
  Future<List<IssueModel>> getAllIssues() async {
    try {
      final db = await DatabaseHelper.database;

      final List<Map<String, dynamic>> maps = await db.query(
        DatabaseHelper.tableIssues,
        orderBy: '${DatabaseHelper.columnName} ASC',
      );

      return maps.map((map) => IssueModel.fromMap(map)).toList();
    } catch (e) {
      throw Exception('Failed to get all issues: $e');
    }
  }

  @override
  Future<IssueModel> getIssueById(int issueId) async {
    try {
      final db = await DatabaseHelper.database;

      final List<Map<String, dynamic>> maps = await db.query(
        DatabaseHelper.tableIssues,
        where: '${DatabaseHelper.columnIssueId} = ?',
        whereArgs: [issueId],
      );

      if (maps.isEmpty) {
        throw Exception('Issue not found');
      }

      return IssueModel.fromMap(maps.first);
    } catch (e) {
      throw Exception('Failed to get issue by ID: $e');
    }
  }
}