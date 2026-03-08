import '../entities/verse.dart';
import '../repositories/verse_repository.dart';

class GetVersesForIssue {
  final VerseRepository repository;

  GetVersesForIssue(this.repository);

  Future<List<Verse>> call(int issueId) async {
    return await repository.getVersesForIssue(issueId);
  }
}