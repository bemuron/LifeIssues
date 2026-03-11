// lib/data/repositories/testimony_repository_impl.dart

import '../../domain/entities/testimony.dart';
import '../../domain/repositories/testimony_repository.dart';
import '../datasources/testimony_remote_datasource.dart';

class TestimonyRepositoryImpl implements TestimonyRepository {
  final TestimonyRemoteDataSource remoteDataSource;

  TestimonyRepositoryImpl({required this.remoteDataSource});

  @override
  Future<List<Testimony>> getTestimonies({
    int page = 1,
    String? category,
  }) async {
    final models = await remoteDataSource.getTestimonies(
      page: page,
      category: category,
    );
    return models.map((model) => model.toEntity()).toList();
  }

  @override
  Future<Testimony> getTestimonyById(int id) async {
    final model = await remoteDataSource.getTestimonyById(id);
    return model.toEntity();
  }

  @override
  Future<Testimony> submitTestimony({
    required String title,
    required String body,
    String? category,
    int? prayerId,
  }) async {
    final model = await remoteDataSource.submitTestimony(
      title: title,
      body: body,
      category: category,
      prayerId: prayerId,
    );
    return model.toEntity();
  }

  @override
  Future<Map<String, dynamic>> togglePraise(int testimonyId) async {
    return await remoteDataSource.togglePraise(testimonyId);
  }

  @override
  Future<void> deleteTestimony(int testimonyId) async {
    await remoteDataSource.deleteTestimony(testimonyId);
  }

  @override
  Future<List<Testimony>> getMyTestimonies({int page = 1}) async {
    final models = await remoteDataSource.getMyTestimonies(page: page);
    return models.map((model) => model.toEntity()).toList();
  }
}