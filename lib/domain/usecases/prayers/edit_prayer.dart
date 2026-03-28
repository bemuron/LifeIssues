import '../../entities/prayer.dart';
import '../../repositories/prayer_repository.dart';

class EditPrayer {
  final PrayerRepository repository;

  EditPrayer(this.repository);

  Future<Prayer> call({
    required int prayerId,
    required String body,
    String? category,
    bool? isAnonymous,
  }) async {
    return await repository.editPrayer(
      prayerId: prayerId,
      body: body,
      category: category,
      isAnonymous: isAnonymous,
    );
  }
}
