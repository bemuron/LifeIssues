// lib/presentation/blocs/subscription/subscription_state.dart

import 'package:equatable/equatable.dart';
import '../../../domain/entities/subscription.dart';

abstract class SubscriptionState extends Equatable {
  @override
  List<Object?> get props => [];
}

class SubscriptionInitial extends SubscriptionState {}

class SubscriptionLoading extends SubscriptionState {}

class SubscriptionLoaded extends SubscriptionState {
  final Subscription subscription;
  final bool canPost;

  SubscriptionLoaded({
    required this.subscription,
    required this.canPost,
  });

  @override
  List<Object?> get props => [subscription, canPost];

  SubscriptionLoaded copyWith({
    Subscription? subscription,
    bool? canPost,
  }) {
    return SubscriptionLoaded(
      subscription: subscription ?? this.subscription,
      canPost: canPost ?? this.canPost,
    );
  }
}

class SubscriptionFree extends SubscriptionState {}

class SubscriptionActive extends SubscriptionState {
  final Subscription subscription;

  SubscriptionActive(this.subscription);

  @override
  List<Object?> get props => [subscription];
}

class SubscriptionExpired extends SubscriptionState {
  final Subscription subscription;

  SubscriptionExpired(this.subscription);

  @override
  List<Object?> get props => [subscription];
}

class SubscriptionSyncing extends SubscriptionState {}

class SubscriptionSynced extends SubscriptionState {
  /// Human-readable result message. Empty string means show nothing.
  final String message;
  SubscriptionSynced({this.message = ''});

  @override
  List<Object?> get props => [message];
}

class SubscriptionError extends SubscriptionState {
  final String message;

  SubscriptionError(this.message);

  @override
  List<Object?> get props => [message];
}