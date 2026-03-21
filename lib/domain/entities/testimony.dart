// lib/domain/entities/testimony.dart

import 'package:equatable/equatable.dart';

class Testimony extends Equatable {
  final int id;
  final int userId;
  final String posterName;
  final String title;
  final String body;
  final String? category;
  final String status; // 'pending', 'approved', 'rejected'
  final int praiseCount;
  final bool hasPraised;
  final int? prayerId;
  final LinkedPrayer? linkedPrayer;
  final DateTime createdAt;
  final String? profileImageUrl;

  const Testimony({
    required this.id,
    required this.userId,
    required this.posterName,
    required this.title,
    required this.body,
    this.category,
    required this.status,
    required this.praiseCount,
    required this.hasPraised,
    this.prayerId,
    this.linkedPrayer,
    required this.createdAt,
    this.profileImageUrl,
  });

  @override
  List<Object?> get props => [
    id,
    userId,
    posterName,
    title,
    body,
    category,
    status,
    praiseCount,
    hasPraised,
    prayerId,
    linkedPrayer,
    createdAt,
    profileImageUrl,
  ];

  Testimony copyWith({
    int? id,
    int? userId,
    String? posterName,
    String? title,
    String? body,
    String? category,
    String? status,
    int? praiseCount,
    bool? hasPraised,
    int? prayerId,
    LinkedPrayer? linkedPrayer,
    DateTime? createdAt,
    String? profileImageUrl,
  }) {
    return Testimony(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      posterName: posterName ?? this.posterName,
      title: title ?? this.title,
      body: body ?? this.body,
      category: category ?? this.category,
      status: status ?? this.status,
      praiseCount: praiseCount ?? this.praiseCount,
      hasPraised: hasPraised ?? this.hasPraised,
      prayerId: prayerId ?? this.prayerId,
      linkedPrayer: linkedPrayer ?? this.linkedPrayer,
      createdAt: createdAt ?? this.createdAt,
      profileImageUrl: profileImageUrl ?? this.profileImageUrl,
    );
  }
}

class LinkedPrayer extends Equatable {
  final int id;
  final String bodyExcerpt; // First 100 chars
  final int prayCount;
  final bool isAnonymous;

  const LinkedPrayer({
    required this.id,
    required this.bodyExcerpt,
    required this.prayCount,
    required this.isAnonymous,
  });

  @override
  List<Object?> get props => [id, bodyExcerpt, prayCount, isAnonymous];
}