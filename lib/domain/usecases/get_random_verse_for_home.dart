import 'package:dartz/dartz.dart';
import '../../core/errors/failures.dart';
import '../entities/verse.dart';
import '../repositories/daily_verse_repository.dart';
import '../repositories/verse_repository.dart';

class GetRandomVerseForHome {
  final VerseRepository repository;

  GetRandomVerseForHome(this.repository);

  /*Future<Either<Failure, Verse>> call() async {
    try {
      final verse = await repository.getRandomVerse();
      return Right(verse);
    } catch (e) {
      // Map to your domain Failure type appropriately
      return Left(ServerFailure()); // or a more specific Failure
    }
  }*/

  Future<Verse> call() async {
    return await repository.getRandomVerseForHome();
  }
}