// lib/presentation/blocs/prayer/prayer_event.dart

import 'package:equatable/equatable.dart';

abstract class PrayerEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadPrayersEvent extends PrayerEvent {
  final int page;
  final String? category;
  final bool isRefresh;

  LoadPrayersEvent({
    this.page = 1,
    this.category,
    this.isRefresh = false,
  });

  @override
  List<Object?> get props => [page, category, isRefresh];
}

class LoadMorePrayersEvent extends PrayerEvent {
  final int page;
  final String? category;

  LoadMorePrayersEvent({
    required this.page,
    this.category,
  });

  @override
  List<Object?> get props => [page, category];
}

class LoadPrayerByIdEvent extends PrayerEvent {
  final int prayerId;

  LoadPrayerByIdEvent(this.prayerId);

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