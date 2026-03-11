// lib/domain/entities/prayer.dart

import 'package:equatable/equatable.dart';

class Prayer extends Equatable {
  final int id;
  final int? userId;
  final String? posterName;
  final String body;
  final String? category;
  final bool isAnonymous;
  final String status; // 'pending', 'approved', 'rejected'
  final int prayCount;
  final bool hasPrayed;
  final bool answered;
  final LinkedTestimony? linkedTestimony;
  final DateTime createdAt;

  const Prayer({
    required this.id,
    this.userId,
    this.posterName,
    required this.body,
    this.category,
    required this.isAnonymous,
    required this.status,
    required this.prayCount,
    required this.hasPrayed,
    this.answered = false,
    this.linkedTestimony,
    required this.createdAt,
  });

  @override
  List<Object?> get props => [
    id,
    userId,
    posterName,
    body,
    category,
    isAnonymous,
    status,
    prayCount,
    hasPrayed,
    answered,
    linkedTestimony,
    createdAt,
  ];

  Prayer copyWith({
    int? id,
    int? userId,
    String? posterName,
    String? body,
    String? category,
    bool? isAnonymous,
    String? status,
    int? prayCount,
    bool? hasPrayed,
    bool? answered,
    LinkedTestimony? linkedTestimony,
    DateTime? createdAt,
  }) {
    return Prayer(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      posterName: posterName ?? this.posterName,
      body: body ?? this.body,
      category: category ?? this.category,
      isAnonymous: isAnonymous ?? this.isAnonymous,
      status: status ?? this.status,
      prayCount: prayCount ?? this.prayCount,
      hasPrayed: hasPrayed ?? this.hasPrayed,
      answered: answered ?? this.answered,
      linkedTestimony: linkedTestimony ?? this.linkedTestimony,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}

class LinkedTestimony extends Equatable {
  final int id;
  final String title;
  final int praiseCount;

  const LinkedTestimony({
    required this.id,
    required this.title,
    required this.praiseCount,
  });

  @override
  List<Object?> get props => [id, title, praiseCount];
}