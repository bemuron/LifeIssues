// lib/data/models/verse_model.dart
import '../../domain/entities/verse.dart';

class VerseModel extends Verse {
  const VerseModel({
    required super.id,
    required super.reference,
    required super.kjv,
    super.msg,
    super.amp,
    super.imageUrl,
    super.createdAt,
    super.isFavorite,
  });

  factory VerseModel.fromMap(Map<String, dynamic> map) {
    // Handle potential null values from database
    final kjvText = map['kjv'] as String?;
    final msgText = map['msg'] as String?;
    final ampText = map['amp'] as String?;

    // Use KJV as fallback if others are null
    final fallbackText = kjvText ?? msgText ?? ampText ?? 'Text not available';

    return VerseModel(
      id: map['_id'] as int,
      reference: map['verse'] as String? ?? 'Unknown Reference',
      kjv: kjvText ?? fallbackText,
      msg: msgText ?? fallbackText,
      amp: ampText ?? fallbackText,
      isFavorite: (map['is_favorite'] as int?) == 1,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      '_id': id,
      'verse': reference,
      'kjv': kjv,
      'msg': msg,
      'amp': amp,
      'is_favorite': isFavorite ? 1 : 0,
    };
  }

  /*factory VerseModel.fromJson(Map<String, dynamic> json) {
    return VerseModel(
      id: json['_id'] as String,
      reference: json['reference'] as String,
      kjv: json['kjv'] as String,
      msg: json['msg'] as String?,
      amp: json['amp'] as String?,
      imageUrl: json['image_url'] as String?,
      createdAt: DateTime.parse(json['created_at'] as String),
      isFavorite: json['is_favorite'] == 1,
    );
  }

  factory VerseModel.fromMap(Map<String, dynamic> map) {
    return VerseModel(
      id: map['id'] as String,
      reference: map['verse'] as String,
      kjv: map['kjv'] as String,
      msg: map['msg'] as String,
      amp: map['amp'] as String,
      isFavorite: (map['is_favorite'] as int?) == 1,
      createdAt: DateTime.parse(map['created_at'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'reference': reference,
      'kjv': kjv,
      'msg': msg,
      'amp': amp,
      'image_url': imageUrl,
      'created_at': createdAt.toIso8601String(),
      'is_favorite': isFavorite ? 1 : 0,
    };
  }*/

  /*Verse toEntity() {
    return Verse(
      id: id,
      reference: reference,
      kjv: kjv,
      msg: msg,
      amp: amp,
      imageUrl: imageUrl,
      createdAt: createdAt,
      isFavorite: isFavorite,
    );
  }

  factory VerseModel.fromEntity(Verse verse) {
    return VerseModel(
      id: verse.id,
      reference: verse.reference,
      kjv: verse.kjv,
      msg: verse.msg,
      amp: verse.amp,
      imageUrl: verse.imageUrl,
      createdAt: verse.createdAt,
      isFavorite: verse.isFavorite,
    );
  }*/

  Verse toEntity() {
    return Verse(
      id: id,
      reference: reference,
      kjv: kjv,
      msg: msg,
      amp: amp,
      isFavorite: isFavorite,
    );
  }

  factory VerseModel.fromEntity(Verse verse) {
    return VerseModel(
      id: verse.id,
      reference: verse.reference,
      kjv: verse.kjv,
      msg: verse.msg,
      amp: verse.amp,
      isFavorite: verse.isFavorite,
    );
  }
}