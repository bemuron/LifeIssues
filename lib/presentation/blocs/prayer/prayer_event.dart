// lib/presentation/blocs/prayer/prayer_event.dart

import 'package:equatable/equatable.dart';

abstract class PrayerEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadPrayersEvent extends PrayerEvent {
  final int page;
  final String? category;
  final String? sortBy; // 'newest', 'oldest', 'needs_prayer'
  final bool? hasPrayers; // null = all, true = has prayers, false = no prayers
  final bool isRefresh;

  LoadPrayersEvent({
    this.page = 1,
    this.category,
    this.sortBy,
    this.hasPrayers,
    this.isRefresh = false,
  });

  @override
  List<Object?> get props => [page, category, isRefresh];
}

class LoadMorePrayersEvent extends PrayerEvent {
  final int page;
  final String? category;
  final String? sortBy;
  final bool? hasPrayers;

  LoadMorePrayersEvent({
    required this.page,
    this.category,
    this.sortBy,
    this.hasPrayers,

  });

  @override
  List<Object?> get props => [page, category];
}

class ApplyPrayerFiltersEvent extends PrayerEvent {
  final String? sortBy;
  final bool? hasPrayers;
  final String? category;

  ApplyPrayerFiltersEvent({
    this.sortBy,
    this.hasPrayers,
    this.category,
  });
}

class ClearPrayerFiltersEvent extends PrayerEvent {}

class LoadPrayerByIdEvent extends PrayerEvent {
  final int prayerId;
  /// Pre-loaded prayer — when set, the bloc emits it directly without an API call.
  final dynamic preloaded;

  LoadPrayerByIdEvent(this.prayerId, {this.preloaded});

  @override
  List<Object?> get props => [prayerId];
}

class SubmitPrayerEvent extends PrayerEvent {
  final String body;
  final String? category;
  final bool isAnonymous;

  SubmitPrayerEvent({
    required this.body,
    this.category,
    this.isAnonymous = false,
  });

  @override
  List<Object?> get props => [body, category, isAnonymous];
}

class TogglePrayingEvent extends PrayerEvent {
  final int prayerId;

  TogglePrayingEvent(this.prayerId);

  @override
  List<Object?> get props => [prayerId];
}

class DeletePrayerEvent extends PrayerEvent {
  final int prayerId;

  DeletePrayerEvent(this.prayerId);

  @override
  List<Object?> get props => [prayerId];
}

class LoadMyPrayersEvent extends PrayerEvent {
  final int page;

  LoadMyPrayersEvent({this.page = 1});

  @override
  List<Object?> get props => [page];
}

class FilterPrayersByCategoryEvent extends PrayerEvent {
  final String? category;

  FilterPrayersByCategoryEvent(this.category);

  @override
  List<Object?> get props => [category];
}