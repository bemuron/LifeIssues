// lib/presentation/blocs/subscription/subscription_bloc.dart

import 'package:flutter_bloc/flutter_bloc.dart';
import '../../../domain/usecases/subscription/get_subscription_status.dart';
import '../../../domain/usecases/subscription/check_subscription.dart';
import '../../../domain/repositories/subscription_repository.dart';
import 'subscription_event.dart';
import 'subscription_state.dart';

class SubscriptionBloc extends Bloc<SubscriptionEvent, SubscriptionState> {
  final GetSubscriptionStatus getSubscriptionStatus;
  final CheckSubscription checkSubscription;
  final SubscriptionRepository subscriptionRepository;

  SubscriptionBloc({
    required this.getSubscriptionStatus,
    required this.checkSubscription,
    required this.subscriptionRepository,
  }) : super(SubscriptionInitial()) {
    on<LoadSubscriptionStatusEvent>(_onLoadSubscriptionStatus);
    on<CheckCanPostEvent>(_onCheckCanPost);
    on<RefreshSubscriptionEvent>(_onRefreshSubscription);
    on<SyncQonversionEvent>(_onSyncQonversion);
  }

  Future<void> _onLoadSubscriptionStatus(
      LoadSubscriptionStatusEvent event,
      Emitter<SubscriptionState> emit,
      ) async {
    try {
      emit(SubscriptionLoading());

      final subscription = await getSubscriptionStatus();
      final canPost = subscription.isValid;

      emit(SubscriptionLoaded(
        subscription: subscription,
        canPost: canPost,
      ));
    } catch (e) {
      emit(SubscriptionError(e.toString()));
    }
  }

  Future<void> _onCheckCanPost(
      CheckCanPostEvent event,
      Emitter<SubscriptionState> emit,
      ) async {
    try {
      final canPost = await checkSubscription();

      if (state is SubscriptionLoaded) {
        final currentState = state as SubscriptionLoaded;
        emit(currentState.copyWith(canPost: canPost));
      } else {
        // Load full subscription status
        add(LoadSubscriptionStatusEvent());
      }
    } catch (e) {
      emit(SubscriptionError(e.toString()));
    }
  }

  Future<void> _onRefreshSubscription(
      RefreshSubscriptionEvent event,
      Emitter<SubscriptionState> emit,
      ) async {
    // Reload subscription status
    add(LoadSubscriptionStatusEvent());
  }

  Future<void> _onSyncQonversion(
      SyncQonversionEvent event,
      Emitter<SubscriptionState> emit,
      ) async {
    try {
      emit(SubscriptionSyncing());

      await subscriptionRepository.syncQonversionSubscription(
        event.qonversionData,
      );

      final isActive = event.qonversionData['is_active'] == true;
      final status = event.qonversionData['status'] as String? ?? '';

      // Only show a message when something genuinely positive happened.
      // Expiry / cancellation syncs are silent — the UI updates via the
      // subsequent LoadSubscriptionStatusEvent instead.
      String message = '';
      if (isActive) {
        message = status == 'restored'
            ? 'Subscription restored successfully!'
            : 'Subscription activated successfully!';
      }

      emit(SubscriptionSynced(message: message));

      // Reload subscription status after sync
      add(LoadSubscriptionStatusEvent());
    } catch (e) {
      emit(SubscriptionError(e.toString()));
    }
  }
}