// lib/data/models/subscription_model.dart

import '../../domain/entities/subscription.dart';

class SubscriptionModel {
  final bool isActive;
  final String? productId;
  final DateTime? expiresAt;

  SubscriptionModel({
    required this.isActive,
    this.productId,
    this.expiresAt,
  });

  factory SubscriptionModel.fromJson(Map<String, dynamic> json) {
    return SubscriptionModel(
      isActive: json['is_active'] as bool? ?? false,
      productId: json['product_id'] as String?,
      expiresAt: json['expires_at'] != null
          ? DateTime.parse(json['expires_at'] as String)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'is_active': isActive,
      'product_id': productId,
      'expires_at': expiresAt?.toIso8601String(),
    };
  }

  Subscription toEntity() {
    return Subscription(
      isActive: isActive,
      productId: productId,
      expiresAt: expiresAt,
    );
  }

  factory SubscriptionModel.fromEntity(Subscription subscription) {
    return SubscriptionModel(
      isActive: subscription.isActive,
      productId: subscription.productId,
      expiresAt: subscription.expiresAt,
    );
  }
}