// lib/domain/entities/verse.dart
import 'package:equatable/equatable.dart';

class Verse extends Equatable {
  final int id;
  final String reference;
  final String? issueName;
  final String kjv;
  final String? msg;
  final String? amp;
  final String? imageUrl;
  final DateTime? createdAt;
  final bool isFavorite;

  const Verse({
    required this.id,
    required this.reference,
    this.issueName,
    required this.kjv,
    this.msg,
    this.amp,
    this.imageUrl,
    this.createdAt,
    this.isFavorite = false,
  });

  // Convenience getter for default text
  String get text => kjv;

  Verse copyWith({
    int? id,
    String? reference,
    String? issueName,
    String? kjv,
    String? msg,
    String? amp,
    String? imageUrl,
    DateTime? createdAt,
    bool? isFavorite,
  }) {
    return Verse(
      id: id ?? this.id,
      reference: reference ?? this.reference,
      issueName: issueName ?? this.issueName,
      kjv: kjv ?? this.kjv,
      msg: msg ?? this.msg,
      amp: amp ?? this.amp,
      imageUrl: imageUrl ?? this.imageUrl,
      createdAt: createdAt ?? this.createdAt,
      isFavorite: isFavorite ?? this.isFavorite,
    );
  }

  @override
  List<Object?> get props => [
    id,
    reference,
    issueName,
    kjv,
    msg,
    amp,
    imageUrl,
    createdAt,
    isFavorite,
  ];
}