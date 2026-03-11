// lib/presentation/blocs/prayer/prayer_bloc.dart

import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:life_issues_flutter/presentation/blocs/prayer/prayer_event.dart';
import 'package:life_issues_flutter/presentation/blocs/prayer/prayer_state.dart';
import '../../../domain/usecases/prayers/get_prayers.dart';
import '../../../domain/usecases/prayers/get_prayer_by_id.dart';
import '../../../domain/usecases/prayers/submit_prayer.dart';
import '../../../domain/usecases/prayers/toggle_praying.dart';
import '../../../domain/usecases/prayers/delete_prayer.dart';
import '../../../domain/usecases/prayers/get_my_prayers.dart';

class PrayerBloc extends Bloc<PrayerEvent, PrayerState> {
  final GetPrayers getPrayers;
  final GetPrayerById getPrayerById;
  final SubmitPrayer submitPrayer;
  final TogglePraying togglePraying;
  final DeletePrayer deletePrayer;
  final GetMyPrayers getMyPrayers;

  PrayerBloc({
    required this.getPrayers,
    required this.getPrayerById,
    required this.submitPrayer,
    required this.togglePraying,
    required this.deletePrayer,
    required this.getMyPrayers,
  }) : super(PrayerInitial()) {
    on<LoadPrayersEvent>(_onLoadPrayers);
    on<LoadMorePrayersEvent>(_onLoadMorePrayers);
    on<LoadPrayerByIdEvent>(_onLoadPrayerById);
    on<SubmitPrayerEvent>(_onSubmitPrayer);
    on<TogglePrayingEvent>(_onTogglePraying);
    on<DeletePrayerEvent>(_onDeletePrayer);
    on<LoadMyPrayersEvent>(_onLoadMyPrayers);
    on<FilterPrayersByCategoryEvent>(_onFilterByCategory);
  }

  Future<void> _onLoadPrayers(
      LoadPrayersEvent event,
      Emitter<PrayerState> emit,
      ) async {
    try {
      emit(PrayerLoading());

      final prayers = await getPrayers(
        page: event.page,
        category: event.category,
      );

      emit(PrayerLoaded(
        prayers: prayers,
        hasMore: prayers.length >= 15, // Based on API page size
        currentPage: event.page,
        currentCategory: event.category,
      ));
    } catch (e) {
      emit(PrayerError(e.toString()));
    }
  }

  Future<void> _onLoadMorePrayers(
      LoadMorePrayersEvent event,
      Emitter<PrayerState> emit,
      ) async {
    if (state is! PrayerLoaded) return;

    final currentState = state as PrayerLoaded;
    if (!currentState.hasMore) return;

    try {
      emit(PrayerLoadingMore(currentState.prayers));

      final newPrayers = await getPrayers(
        page: event.page,
        category: event.category,
      );

      final allPrayers = [...currentState.prayers, ...newPrayers];

      emit(PrayerLoaded(
        prayers: allPrayers,
        hasMore: newPrayers.length >= 15,
        currentPage: event.page,
        currentCategory: event.category,
      ));
    } catch (e) {
      emit(PrayerLoaded(
        prayers: currentState.prayers,
        hasMore: currentState.hasMore,
        currentPage: currentState.currentPage,
        currentCategory: currentState.currentCategory,
      ));
      emit(PrayerError(e.toString()));
    }
  }

  Future<void> _onLoadPrayerById(
      LoadPrayerByIdEvent event,
      Emitter<PrayerState> emit,
      ) async {
    try {
      emit(PrayerDetailLoading());

      final prayer = await getPrayerById(event.prayerId);

      emit(PrayerDetailLoaded(prayer));
    } catch (e) {
      emit(PrayerError(e.toString()));
    }
  }

  Future<void> _onSubmitPrayer(
      SubmitPrayerEvent event,
      Emitter<PrayerState> emit,
      ) async {
    try {
      emit(PrayerSubmitting());

      final prayer = await submitPrayer(
        body: event.body,
        category: event.category,
        isAnonymous: event.isAnonymous,
      );

      emit(PrayerSubmitted(prayer));
    } on ArgumentError catch (e) {
      emit(PrayerError(e.message));
    } catch (e) {
      emit(PrayerError(e.toString()));
    }
  }

  Future<void> _onTogglePraying(
      TogglePrayingEvent event,
      Emitter<PrayerState> emit,
      ) async {
    try {
      emit(PrayerTogglingPraying(event.prayerId));

      final result = await togglePraying(event.prayerId);

      emit(PrayerPrayingToggled(
        prayerId: event.prayerId,
        hasPrayed: result['has_prayed'] as bool,
        prayCount: result['pray_count'] as int,
      ));

      // Update the prayer in the list if currently loaded
      if (state is PrayerLoaded) {
        final currentState = state as PrayerLoaded;
        final updatedPrayers = currentState.prayers.map((prayer) {
          if (prayer.id == event.prayerId) {
            return prayer.copyWith(
              hasPrayed: result['has_prayed'] as bool,
              prayCount: result['pray_count'] as int,
            );
          }
          return prayer;
        }).toList();

        emit(PrayerLoaded(
          prayers: updatedPrayers,
          hasMore: currentState.hasMore,
          currentPage: currentState.currentPage,
          currentCategory: currentState.currentCategory,
        ));
      }
    } catch (e) {
      emit(PrayerError(e.toString()));
    }
  }

  Future<void> _onDeletePrayer(
      DeletePrayerEvent event,
      Emitter<PrayerState> emit,
      ) async {
    try {
      emit(PrayerDeleting(event.prayerId));

      await deletePrayer(event.prayerId);

      emit(PrayerDeleted(event.prayerId));

      // Remove prayer from list if currently loaded
      if (state is PrayerLoaded) {
        final currentState = state as PrayerLoaded;
        final updatedPrayers = currentState.prayers
            .where((prayer) => prayer.id != event.prayerId)
            .toList();

        emit(PrayerLoaded(
          prayers: updatedPrayers,
          hasMore: currentState.hasMore,
          currentPage: currentState.currentPage,
          currentCategory: currentState.currentCategory,
        ));
      }
    } catch (e) {
      emit(PrayerError(e.toString()));
    }
  }

  Future<void> _onLoadMyPrayers(
      LoadMyPrayersEvent event,
      Emitter<PrayerState> emit,
      ) async {
    try {
      emit(MyPrayersLoading());

      final prayers = await getMyPrayers(page: event.page);

      emit(MyPrayersLoaded(
        myPrayers: prayers,
        hasMore: prayers.length >= 15,
        currentPage: event.page,
      ));
    } catch (e) {
      emit(PrayerError(e.toString()));
    }
  }

  Future<void> _onFilterByCategory(
      FilterPrayersByCategoryEvent event,
      Emitter<PrayerState> emit,
      ) async {
    // Reload prayers with new category filter
    add(LoadPrayersEvent(page: 1, category: event.category));
  }
}