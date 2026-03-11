// lib/presentation/blocs/testimony/testimony_state.dart

import 'package:equatable/equatable.dart';
import '../../../domain/entities/testimony.dart';

abstract class TestimonyState extends Equatable {
  @override
  List<Object?> get props => [];
}

class TestimonyInitial extends TestimonyState {}

class TestimonyLoading extends TestimonyState {}

class TestimonyLoaded extends TestimonyState {
  final List<Testimony> testimonies;
  final bool hasMore;
  final int currentPage;
  final String? currentCategory;

  TestimonyLoaded({
    required this.testimonies,
    this.hasMore = true,
    this.currentPage = 1,
    this.currentCategory,
  });

  @override
  List<Object?> get props => [testimonies, hasMore, currentPage, currentCategory];

  TestimonyLoaded copyWith({
    List<Testimony>? testimonies,
    bool? hasMore,
    int? currentPage,
    String? currentCategory,
  }) {
    return TestimonyLoaded(
      testimonies: testimonies ?? this.testimonies,
      hasMore: hasMore ?? this.hasMore,
      currentPage: currentPage ?? this.currentPage,
      currentCategory: currentCategory ?? this.currentCategory,
    );
  }
}

class TestimonyLoadingMore extends TestimonyState {
  final List<Testimony> currentTestimonies;

  TestimonyLoadingMore(this.currentTestimonies);

  @override
  List<Object?> get props => [currentTestimonies];
}

class TestimonyDetailLoading extends TestimonyState {}

class TestimonyDetailLoaded extends TestimonyState {
  final Testimony testimony;

  TestimonyDetailLoaded(this.testimony);

  @override
  List<Object?> get props => [testimony];
}

class TestimonySubmitting extends TestimonyState {}

class TestimonySubmitted extends TestimonyState {
  final Testimony testimony;

  TestimonySubmitted(this.testimony);

  @override
  List<Object?> get props => [testimony];
}

class TestimonyTogglingPraise extends TestimonyState {
  final int testimonyId;

  TestimonyTogglingPraise(this.testimonyId);

  @override
  List<Object?> get props => [testimonyId];
}

class TestimonyPraiseToggled extends TestimonyState {
  final int testimonyId;
  final bool hasPraised;
  final int praiseCount;

  TestimonyPraiseToggled({
    required this.testimonyId,
    required this.hasPraised,
    required this.praiseCount,
  });

  @override
  List<Object?> get props => [testimonyId, hasPraised, praiseCount];
}

class TestimonyDeleting extends TestimonyState {
  final int testimonyId;

  TestimonyDeleting(this.testimonyId);

  @override
  List<Object?> get props => [testimonyId];
}

class TestimonyDeleted extends TestimonyState {
  final int testimonyId;

  TestimonyDeleted(this.testimonyId);

  @override
  List<Object?> get props => [testimonyId];
}

class MyTestimoniesLoading extends TestimonyState {}

class MyTestimoniesLoaded extends TestimonyState {
  final List<Testimony> myTestimonies;
  final bool hasMore;
  final int currentPage;

  MyTestimoniesLoaded({
    required this.myTestimonies,
    this.hasMore = true,
    this.currentPage = 1,
  });

  @override
  List<Object?> get props => [myTestimonies, hasMore, currentPage];
}

class TestimonyError extends TestimonyState {
  final String message;

  TestimonyError(this.message);

  @override
  List<Object?> get props => [message];
}