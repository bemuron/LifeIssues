// lib/data/models/verse_model.dart
import '../../domain/entities/verse.dart';

class VerseModel extends Verse {
  const VerseModel({
    required super.id,
    required super.reference,
    super.issueName,
    required super.translations,
    super.imageUrl,
    super.createdAt,
    super.isFavorite,
  });

  /// Build from a list of raw DB rows that all represent the same verse
  /// in different translations (same book/chapter/verse_num, different version).
  /// [canonicalId] is the _id of the row used as the FK in issues_verses.
  static VerseModel fromGroupedRows({
    required int canonicalId,
    required List<Map<String, dynamic>> rows,
  }) {
    final first = rows.first;
    final translations = <String, String>{
      for (final r in rows)
        (r['version'] as String): (r['text'] as String),
    };
    return VerseModel(
      id: canonicalId,
      reference: first['reference'] as String? ?? '',
      translations: translations,
      isFavorite: (first['is_favorite'] as int?) == 1,
      issueName: first['issue_name'] as String?,
      imageUrl: first['image_url'] as String?,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      '_id': id,
      'reference': reference,
      'issue_name': issueName,
      'image_url': imageUrl,
      'is_favorite': isFavorite ? 1 : 0,
    };
  }

  Verse toEntity() {
    return Verse(
      id: id,
      reference: reference,
      issueName: issueName,
      translations: translations,
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
      translations: verse.translations,
      imageUrl: verse.imageUrl,
      createdAt: verse.createdAt,
      isFavorite: verse.isFavorite,
    );
  }
}
