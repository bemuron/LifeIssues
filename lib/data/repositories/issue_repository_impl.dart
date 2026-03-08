import '../../domain/entities/issue.dart';
import '../../domain/repositories/issue_repository.dart';
import '../datasources/issue_local_datasource.dart';

class IssueRepositoryImpl implements IssueRepository {
  final IssueLocalDataSource localDataSource;

  IssueRepositoryImpl({required this.localDataSource});

  @override
  Future<List<Issue>> getAllIssues() async {
    final models = await localDataSource.getAllIssues();
    return models.map((model) => model.toEntity()).toList();
  }

  @override
  Future<Issue> getIssueById(int issueId) async {
    final model = await localDataSource.getIssueById(issueId);
    return model.toEntity();
  }
}