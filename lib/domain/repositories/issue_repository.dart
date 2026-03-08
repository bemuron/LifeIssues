import '../entities/issue.dart';

abstract class IssueRepository {
  Future<List<Issue>> getAllIssues();
  Future<Issue> getIssueById(int issueId);
}