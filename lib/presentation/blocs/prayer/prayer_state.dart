// lib/presentation/blocs/prayer/prayer_state.dart

import 'package:equatable/equatable.dart';
import '../../../../domain/entities/prayer.dart';

abstract class PrayerState extends Equatable {
  @override
  List<Object?> get props => [];
}

class PrayerInitial extends PrayerState {}

class PrayerLoading extends PrayerState {}

class PrayerLoaded extends PrayerState {
  final List<Prayer> prayers;
  final bool hasMore;
  final int currentPage;
  final String? currentCategory;
  final String? currentSortBy;
  final bool? currentHasPrayers;

  PrayerLoaded({
    required this.prayers,
    this.hasMore = true,
    this.currentPage = 1,
    this.currentCategory,
    this.currentSortBy,
    this.currentHasPrayers,
  });

  @override
  List<Object?> get props => [prayers, hasMore, currentPage, currentCategory, currentSortBy,
    currentHasPrayers,];

  PrayerLoaded copyWith({
    List<Prayer>? prayers,
    bool? hasMore,
    int? currentPage,
    String? currentCategory,
    String? currentSortBy,
    bool? currentHasPrayers,
  }) {
    return PrayerLoaded(
      prayers: prayers ?? this.prayers,
      hasMore: hasMore ?? this.hasMore,
      currentPage: currentPage ?? this.currentPage,
      currentCategory: currentCategory ?? this.currentCategory,
      currentSortBy: currentSortBy ?? this.currentSortBy,
      currentHasPrayers: currentHasPrayers ?? this.currentHasPrayers,
    );
  }
}

class PrayerLoadingMore extends PrayerState {
  final List<Prayer> currentPrayers;

  PrayerLoadingMore(this.currentPrayers);

  @override
  List<Object?> get props => [currentPrayers];
}

class PrayerDetailLoading extends PrayerState {}

class PrayerDetailLoaded extends PrayerState {
  final Prayer prayer;

  PrayerDetailLoaded(this.prayer);

  @override
  List<Object?> get props => [prayer];
}

class PrayerSubmitting extends PrayerState {}

class PrayerSubmitted extends PrayerState {
  final Prayer prayer;

  PrayerSubmitted(this.prayer);

  @override
  List<Object?> get props => [prayer];
}

class PrayerTogglingPraying extends PrayerState {
  final int prayerId;

  PrayerTogglingPraying(this.prayerId);

  @override
  List<Object?> get props => [prayerId];
}

class PrayerPrayingToggled extends PrayerState {
  final int prayerId;
  final bool hasPrayed;
  final int prayCount;

  PrayerPrayingToggled({
    required this.prayerId,
    required this.hasPrayed,
    required this.prayCount,
  });

  @override
  List<Object?> get props => [prayerId, hasPrayed, prayCount];
}

class PrayerDeleting extends PrayerState {
  final int prayerId;

  PrayerDeleting(this.prayerId);

  @override
  List<Object?> get props => [prayerId];
}

class PrayerDeleted extends PrayerState {
  final int prayerId;

  PrayerDeleted(this.prayerId);

  @override
  List<Object?> get props => [prayerId];
}

class MyPrayersLoading extends PrayerState {}

class MyPrayersLoaded extends PrayerState {
  final List<Prayer> myPrayers;
  final bool hasMore;
  final int currentPage;

  MyPrayersLoaded({
    required this.myPrayers,
    this.hasMore = true,
    this.currentPage = 1,
  });

  @override
  List<Object?> get props => [myPrayers, hasMore, currentPage];
}

class PrayerError extends PrayerState {
  final String message;

  PrayerError(this.message);

  @override
  List<Object?> get props => [message];
}