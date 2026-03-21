// lib/data/models/prayer_model.dart

import '../../core/constants/image_config.dart';
import '../../domain/entities/prayer.dart';

class PrayerModel {
  final int id;
  final int? userId;
  final String? posterName;
  final String body;
  final String? category;
  final bool isAnonymous;
  final String status;
  final int prayCount;
  final bool hasPrayed;
  final bool answered;
  final LinkedTestimonyModel? linkedTestimony;
  final DateTime createdAt;
  final String? profileImageUrl;

  PrayerModel({
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
    this.profileImageUrl,
  });

  factory PrayerModel.fromJson(Map<String, dynamic> json) {
    // user_id may be returned as String (e.g. "1") or int
    final rawUserId = json['user_id'];
    final int? userId = rawUserId is int
        ? rawUserId
        : int.tryParse(rawUserId?.toString() ?? '');

    // poster_name: use direct field or fall back to nested user.name
    final isAnonymous = json['is_anonymous'] as bool? ?? false;
    final userMap = json['user'] as Map<String, dynamic>?;
    String? posterName = json['poster_name'] as String?;
    if (posterName == null && !isAnonymous) {
      posterName = userMap?['name'] as String?;
    }

    final rawImagePath = userMap?['profile_image_path'] as String?;
    final profileImageUrl = ImageConfig.getProfileImageUrl(rawImagePath);

    return PrayerModel(
      id: json['id'] as int,
      userId: userId,
      posterName: posterName,
      body: json['body'] as String,
      category: json['category'] as String?,
      isAnonymous: isAnonymous,
      status: json['status'] as String,
      prayCount: json['pray_count'] as int? ?? 0,
      hasPrayed: json['has_prayed'] as bool? ?? false,
      answered: json['answered'] as bool? ?? false,
      linkedTestimony: json['linked_testimony'] != null
          ? LinkedTestimonyModel.fromJson(json['linked_testimony'] as Map<String, dynamic>)
          : null,
      createdAt: DateTime.parse(json['created_at'] as String),
      profileImageUrl: profileImageUrl.isEmpty ? null : profileImageUrl,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'user_id': userId,
      'poster_name': posterName,
      'body': body,
      'category': category,
      'is_anonymous': isAnonymous,
      'status': status,
      'pray_count': prayCount,
      'has_prayed': hasPrayed,
      'answered': answered,
      'linked_testimony': linkedTestimony?.toJson(),
      'created_at': createdAt.toIso8601String(),
    };
  }

  Prayer toEntity() {
    return Prayer(
      id: id,
      userId: userId,
      posterName: posterName,
      body: body,
      category: category,
      isAnonymous: isAnonymous,
      status: status,
      prayCount: prayCount,
      hasPrayed: hasPrayed,
      answered: answered,
      linkedTestimony: linkedTestimony?.toEntity(),
      createdAt: createdAt,
      profileImageUrl: profileImageUrl,
    );
  }
}

class LinkedTestimonyModel {
  final int id;
  final String title;
  final int praiseCount;

  LinkedTestimonyModel({
    required this.id,
    required this.title,
    required this.praiseCount,
  });

  factory LinkedTestimonyModel.fromJson(Map<String, dynamic> json) {
    return LinkedTestimonyModel(
      id: json['id'] as int,
      title: json['title'] as String,
      praiseCount: json['praise_count'] as int? ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'praise_count': praiseCount,
    };
  }

  LinkedTestimony toEntity() {
    return LinkedTestimony(
      id: id,
      title: title,
      praiseCount: praiseCount,
    );
  }
}