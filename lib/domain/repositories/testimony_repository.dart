// lib/domain/repositories/testimony_repository.dart

import '../entities/testimony.dart';

abstract class TestimonyRepository {
  Future<List<Testimony>> getTestimonies({
    int page = 1,
    String? category,
    String? sortBy,
    bool? linkedToPrayer,
    bool? hasPraise,
  });
  Future<Testimony> getTestimonyById(int id);
  Future<Testimony> submitTestimony({
    required String title,
    required String body,
    String? category,
    int? prayerId,
  });
  Future<Map<String, dynamic>> togglePraise(int testimonyId);
  Future<Testimony> editTestimony({
    required int testimonyId,
    required String title,
    required String body,
    String? category,
  });
  Future<void> deleteTestimony(int testimonyId);
  Future<List<Testimony>> getMyTestimonies({int page = 1});
}