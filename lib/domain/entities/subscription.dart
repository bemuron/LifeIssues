// lib/domain/entities/subscription.dart

import 'package:equatable/equatable.dart';

class Subscription extends Equatable {
  final bool isActive;
  final String? productId;
  final DateTime? expiresAt;

  const Subscription({
    required this.isActive,
    this.productId,
    this.expiresAt,
  });

  @override
  List<Object?> get props => [isActive, productId, expiresAt];

  Subscription copyWith({
    bool? isActive,
    String? productId,
    DateTime? expiresAt,
  }) {
    return Subscription(
      isActive: isActive ?? this.isActive,
      productId: productId ?? this.productId,
      expiresAt: expiresAt ?? this.expiresAt,
    );
  }

  /// Check if subscription is valid (active and not expired)
  bool get isValid {
    if (!isActive) return false;
    if (expiresAt == null) return true; // Lifetime subscription
    return expiresAt!.isAfter(DateTime.now());
  }
}