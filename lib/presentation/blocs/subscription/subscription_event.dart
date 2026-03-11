// lib/presentation/blocs/subscription/subscription_event.dart

import 'package:equatable/equatable.dart';

abstract class SubscriptionEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoadSubscriptionStatusEvent extends SubscriptionEvent {}

class CheckCanPostEvent extends SubscriptionEvent {}

class RefreshSubscriptionEvent extends SubscriptionEvent {}

class SyncQonversionEvent extends SubscriptionEvent {
  final Map<String, dynamic> qonversionData;

  SyncQonversionEvent(this.qonversionData);

  @override
  List<Object?> get props => [qonversionData];
}