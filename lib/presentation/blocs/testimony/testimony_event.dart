// lib/presentation/blocs/testimony/testimony_event.dart

import 'package:equatable/equatable.dart';

abstract class TestimonyEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadTestimoniesEvent extends TestimonyEvent {
  final int page;
  final String? category;
  final String? sortBy; // 'newest', 'oldest', 'most_praised'
  final bool? linkedToPrayer; // null = all, true = linked, false = standalone
  final bool? hasPraise; // null = all, true = has praise, false = no praise yet
  final bool isRefresh;

  LoadTestimoniesEvent({
    this.page = 1,
    this.category,
    this.sortBy,
    this.linkedToPrayer,
    this.hasPraise,
    this.isRefresh = false,
  });

  @override
  List<Object?> get props => [page, category, sortBy, linkedToPrayer, hasPraise, isRefresh];
}

class LoadMoreTestimoniesEvent extends TestimonyEvent {
  final int page;
  final String? category;
  final String? sortBy;
  final bool? linkedToPrayer;
  final bool? hasPraise;

  LoadMoreTestimoniesEvent({
    required this.page,
    this.category,
    this.sortBy,
    this.linkedToPrayer,
    this.hasPraise,
  });

  @override
  List<Object?> get props => [page, category, sortBy, linkedToPrayer, hasPraise];
}

class ApplyTestimonyFiltersEvent extends TestimonyEvent {
  final String? sortBy;
  final bool? linkedToPrayer;
  final bool? hasPraise;
  final String? category;

  ApplyTestimonyFiltersEvent({
    this.sortBy,
    this.linkedToPrayer,
    this.hasPraise,
    this.category,
  });
}

class ClearTestimonyFiltersEvent extends TestimonyEvent {}

class LoadTestimonyByIdEvent extends TestimonyEvent {
  final int testimonyId;

  LoadTestimonyByIdEvent(this.testimonyId);

  @override
  List<Object?> get props => [testimonyId];
}

class SubmitTestimonyEvent extends TestimonyEvent {
  final String title;
  final String body;
  final String? category;
  final int? prayerId;

  SubmitTestimonyEvent({
    required this.title,
    required this.body,
    this.category,
    this.prayerId,
  });

  @override
  List<Object?> get props => [title, body, category, prayerId];
}

class TogglePraiseEvent extends TestimonyEvent {
  final int testimonyId;

  TogglePraiseEvent(this.testimonyId);

  @override
  List<Object?> get props => [testimonyId];
}

class DeleteTestimonyEvent extends TestimonyEvent {
  final int testimonyId;

  DeleteTestimonyEvent(this.testimonyId);

  @override
  List<Object?> get props => [testimonyId];
}

class LoadMyTestimoniesEvent extends TestimonyEvent {
  final int page;

  LoadMyTestimoniesEvent({this.page = 1});

  @override
  List<Object?> get props => [page];
}

class FilterTestimoniesByCategoryEvent extends TestimonyEvent {
  final String? category;

  FilterTestimoniesByCategoryEvent(this.category);

  @override
  List<Object?> get props => [category];
}