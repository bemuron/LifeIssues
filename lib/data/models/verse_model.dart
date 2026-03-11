// lib/data/models/verse_model.dart
import '../../domain/entities/verse.dart';

class VerseModel extends Verse {
  const VerseModel({
    required super.id,
    required super.reference,
    super.issueName,
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
      issueName: map['issue_name'] as String? ?? fallbackText,
      imageUrl: map['image_url'] as String?,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      '_id': id,
      'verse': reference,
      'issue_name': issueName,
      'kjv': kjv,
      'msg': msg,
      'amp': amp,
      'image_url': imageUrl,
      'created_at': createdAt?.toIso8601String(),
      'is_favorite': isFavorite ? 1 : 0,
    };
  }

  Verse toEntity() {
    return Verse(
      id: id,
      reference: reference,
      issueName: issueName,
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
      issueName: verse.issueName,
      kjv: verse.kjv,
      msg: verse.msg,
      amp: verse.amp,
      imageUrl: verse.imageUrl,
      createdAt: verse.createdAt,
      isFavorite: verse.isFavorite,
    );
  }
}