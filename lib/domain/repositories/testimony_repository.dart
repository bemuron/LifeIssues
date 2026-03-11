// lib/domain/repositories/testimony_repository.dart

import '../entities/testimony.dart';

abstract class TestimonyRepository {
  Future<List<Testimony>> getTestimonies({int page = 1, String? category});
  Future<Testimony> getTestimonyById(int id);
  Future<Testimony> submitTestimony({
    required String title,
    required String body,
    String? category,
    int? prayerId,
  });
  Future<Map<String, dynamic>> togglePraise(int testimonyId);
  Future<void> deleteTestimony(int testimonyId);
  Future<List<Testimony>> getMyTestimonies({int page = 1});
}