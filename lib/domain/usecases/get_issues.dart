import '../entities/issue.dart';
import '../repositories/issue_repository.dart';

class GetIssues {
  final IssueRepository repository;

  GetIssues(this.repository);

  Future<List<Issue>> call() async {
    return await repository.getAllIssues();
  }
}