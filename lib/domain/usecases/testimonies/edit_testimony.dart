import '../../entities/testimony.dart';
import '../../repositories/testimony_repository.dart';

class EditTestimony {
  final TestimonyRepository repository;

  EditTestimony(this.repository);

  Future<Testimony> call({
    required int testimonyId,
    required String title,
    required String body,
    String? category,
  }) async {
    return await repository.editTestimony(
      testimonyId: testimonyId,
      title: title,
      body: body,
      category: category,
    );
  }
}
