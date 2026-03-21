// lib/presentation/blocs/testimony/testimony_bloc.dart

import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../domain/usecases/testimonies/get_testimonies.dart';
import '../../../domain/usecases/testimonies/get_testimony_by_id.dart';
import '../../../domain/usecases/testimonies/submit_testimony.dart';
import '../../../domain/usecases/testimonies/toggle_praise.dart';
import '../../../domain/usecases/testimonies/delete_testimony.dart';
import '../../../domain/usecases/testimonies/get_my_testimonies.dart';
import 'testimony_event.dart';
import 'testimony_state.dart';

class TestimonyBloc extends Bloc<TestimonyEvent, TestimonyState> {
  final GetTestimonies getTestimonies;
  final GetTestimonyById getTestimonyById;
  final SubmitTestimony submitTestimony;
  final TogglePraise togglePraise;
  final DeleteTestimony deleteTestimony;
  final GetMyTestimonies getMyTestimonies;

  TestimonyBloc({
    required this.getTestimonies,
    required this.getTestimonyById,
    required this.submitTestimony,
    required this.togglePraise,
    required this.deleteTestimony,
    required this.getMyTestimonies,
  }) : super(TestimonyInitial()) {
    on<LoadTestimoniesEvent>(_onLoadTestimonies);
    on<LoadMoreTestimoniesEvent>(_onLoadMoreTestimonies);
    on<ApplyTestimonyFiltersEvent>(_onApplyFilters);
    on<ClearTestimonyFiltersEvent>(_onClearFilters);
    on<LoadTestimonyByIdEvent>(_onLoadTestimonyById);
    on<SubmitTestimonyEvent>(_onSubmitTestimony);
    on<TogglePraiseEvent>(_onTogglePraise);
    on<DeleteTestimonyEvent>(_onDeleteTestimony);
    on<LoadMyTestimoniesEvent>(_onLoadMyTestimonies);
    on<FilterTestimoniesByCategoryEvent>(_onFilterByCategory);
  }

  Future<void> _onLoadTestimonies(
      LoadTestimoniesEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    try {
      if (!event.isRefresh) emit(TestimonyLoading());

      final testimonies = await getTestimonies(
        page: event.page,
        category: event.category,
        sortBy: event.sortBy,
        linkedToPrayer: event.linkedToPrayer,
        hasPraise: event.hasPraise,
      );

      emit(TestimonyLoaded(
        testimonies: testimonies,
        hasMore: testimonies.length >= 10, // Based on API page size
        currentPage: event.page,
        currentCategory: event.category,
        currentSortBy: event.sortBy,
        currentLinkedToPrayer: event.linkedToPrayer,
        currentHasPraise: event.hasPraise,
      ));
    } catch (e) {
      emit(TestimonyError(e.toString()));
    }
  }

  Future<void> _onLoadMoreTestimonies(
      LoadMoreTestimoniesEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    if (state is! TestimonyLoaded) return;

    final currentState = state as TestimonyLoaded;
    if (!currentState.hasMore) return;

    try {
      emit(TestimonyLoadingMore(currentState.testimonies));

      final newTestimonies = await getTestimonies(
        page: event.page,
        category: event.category,
        sortBy: event.sortBy,
        linkedToPrayer: event.linkedToPrayer,
        hasPraise: event.hasPraise,
      );

      final allTestimonies = [...currentState.testimonies, ...newTestimonies];

      emit(TestimonyLoaded(
        testimonies: allTestimonies,
        hasMore: newTestimonies.length >= 10,
        currentPage: event.page,
        currentCategory: event.category,
        currentSortBy: event.sortBy,
        currentLinkedToPrayer: event.linkedToPrayer,
        currentHasPraise: event.hasPraise,
      ));
    } catch (e) {
      emit(TestimonyLoaded(
        testimonies: currentState.testimonies,
        hasMore: currentState.hasMore,
        currentPage: currentState.currentPage,
        currentCategory: currentState.currentCategory,
      ));
      emit(TestimonyError(e.toString()));
    }
  }

  Future<void> _onApplyFilters(
      ApplyTestimonyFiltersEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    add(LoadTestimoniesEvent(
      page: 1,
      category: event.category,
      sortBy: event.sortBy,
      linkedToPrayer: event.linkedToPrayer,
      hasPraise: event.hasPraise,
    ));
  }

  Future<void> _onClearFilters(
      ClearTestimonyFiltersEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    add(LoadTestimoniesEvent(page: 1));
  }

  Future<void> _onLoadTestimonyById(
      LoadTestimonyByIdEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    try {
      emit(TestimonyDetailLoading());

      final testimony = await getTestimonyById(event.testimonyId);

      emit(TestimonyDetailLoaded(testimony));
    } catch (e) {
      emit(TestimonyError(e.toString()));
    }
  }

  Future<void> _onSubmitTestimony(
      SubmitTestimonyEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    try {
      emit(TestimonySubmitting());

      final testimony = await submitTestimony(
        title: event.title,
        body: event.body,
        category: event.category,
        prayerId: event.prayerId,
      );

      emit(TestimonySubmitted(testimony));
    } on ArgumentError catch (e) {
      emit(TestimonyError(e.message));
    } catch (e) {
      emit(TestimonyError(e.toString()));
    }
  }

  Future<void> _onTogglePraise(
      TogglePraiseEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    // Save current detail state so we can restore it after the toggle
    final prevDetailState = state is TestimonyDetailLoaded ? state as TestimonyDetailLoaded : null;
    final prevListState = state is TestimonyLoaded ? state as TestimonyLoaded : null;

    try {
      emit(TestimonyTogglingPraise(event.testimonyId));

      final result = await togglePraise(event.testimonyId);

      final hasPraised = result['has_praised'] as bool;
      final praiseCount = result['praise_count'] as int;
      final alreadyPraised = result['already_praised'] as bool? ?? false;

      emit(TestimonyPraiseToggled(
        testimonyId: event.testimonyId,
        hasPraised: hasPraised,
        praiseCount: praiseCount,
        alreadyPraised: alreadyPraised,
      ));

      // If the detail page was open, re-emit the updated detail state
      if (prevDetailState != null) {
        emit(TestimonyDetailLoaded(
          prevDetailState.testimony.copyWith(
            hasPraised: hasPraised,
            praiseCount: praiseCount,
          ),
        ));
      }

      // Update the testimony in the list if the list was the active state
      if (prevListState != null) {
        final updatedTestimonies = prevListState.testimonies.map((testimony) {
          if (testimony.id == event.testimonyId) {
            return testimony.copyWith(hasPraised: hasPraised, praiseCount: praiseCount);
          }
          return testimony;
        }).toList();

        emit(TestimonyLoaded(
          testimonies: updatedTestimonies,
          hasMore: prevListState.hasMore,
          currentPage: prevListState.currentPage,
          currentCategory: prevListState.currentCategory,
          currentSortBy: prevListState.currentSortBy,
          currentLinkedToPrayer: prevListState.currentLinkedToPrayer,
          currentHasPraise: prevListState.currentHasPraise,
        ));
      }
    } catch (e) {
      // Restore whichever state was active before the toggle attempt
      if (prevDetailState != null) {
        emit(prevDetailState);
      } else if (prevListState != null) {
        emit(prevListState);
      }
      emit(TestimonyError(e.toString()));
    }
  }

  Future<void> _onDeleteTestimony(
      DeleteTestimonyEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    try {
      emit(TestimonyDeleting(event.testimonyId));

      await deleteTestimony(event.testimonyId);

      emit(TestimonyDeleted(event.testimonyId));

      // Remove testimony from list if currently loaded
      if (state is TestimonyLoaded) {
        final currentState = state as TestimonyLoaded;
        final updatedTestimonies = currentState.testimonies
            .where((testimony) => testimony.id != event.testimonyId)
            .toList();

        emit(TestimonyLoaded(
          testimonies: updatedTestimonies,
          hasMore: currentState.hasMore,
          currentPage: currentState.currentPage,
          currentCategory: currentState.currentCategory,
        ));
      }
    } catch (e) {
      emit(TestimonyError(e.toString()));
    }
  }

  Future<void> _onLoadMyTestimonies(
      LoadMyTestimoniesEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    try {
      emit(MyTestimoniesLoading());

      final testimonies = await getMyTestimonies(page: event.page);

      emit(MyTestimoniesLoaded(
        myTestimonies: testimonies,
        hasMore: testimonies.length >= 10,
        currentPage: event.page,
      ));
    } catch (e) {
      emit(TestimonyError(e.toString()));
    }
  }

  Future<void> _onFilterByCategory(
      FilterTestimoniesByCategoryEvent event,
      Emitter<TestimonyState> emit,
      ) async {
    // Reload testimonies with new category filter
    add(LoadTestimoniesEvent(page: 1, category: event.category));
  }
}