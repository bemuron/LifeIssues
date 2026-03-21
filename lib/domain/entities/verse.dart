// lib/domain/entities/verse.dart
import 'package:equatable/equatable.dart';

class Verse extends Equatable {
  final int id;
  final String reference;
  final String? issueName;

  /// All available translations keyed by uppercase version code.
  /// e.g. {'KJV': '...', 'MSG': '...', 'AMP': '...', 'NIV': '...'}
  /// Adding a new Bible version only requires data — no code change.
  final Map<String, String> translations;

  final String? imageUrl;
  final DateTime? createdAt;
  final bool isFavorite;

  const Verse({
    required this.id,
    required this.reference,
    this.issueName,
    required this.translations,
    this.imageUrl,
    this.createdAt,
    this.isFavorite = false,
  });

  // ── Generic version lookup ───────────────────────────────────────────────
  /// Returns text for [version] (case-insensitive). Falls back to KJV,
  /// then the first available translation.
  String getVersion(String version) =>
      translations[version.toUpperCase()] ??
      translations['KJV'] ??
      translations.values.firstOrNull ??
      '';

  // ── Backward-compatible getters — existing UI code keeps working ─────────
  String get kjv => translations['KJV'] ?? translations.values.firstOrNull ?? '';
  String? get msg => translations['MSG'];
  String? get amp => translations['AMP'];

  /// Convenience: default text (KJV or first available).
  String get text => kjv;

  Verse copyWith({
    int? id,
    String? reference,
    String? issueName,
    Map<String, String>? translations,
    String? imageUrl,
    DateTime? createdAt,
    bool? isFavorite,
  }) {
    return Verse(
      id: id ?? this.id,
      reference: reference ?? this.reference,
      issueName: issueName ?? this.issueName,
      translations: translations ?? this.translations,
      imageUrl: imageUrl ?? this.imageUrl,
      createdAt: createdAt ?? this.createdAt,
      isFavorite: isFavorite ?? this.isFavorite,
    );
  }

  @override
  List<Object?> get props =>
      [id, reference, issueName, translations, imageUrl, createdAt, isFavorite];
}
