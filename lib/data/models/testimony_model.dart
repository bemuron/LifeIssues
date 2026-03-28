// lib/data/models/testimony_model.dart

import '../../core/constants/image_config.dart';
import '../../domain/entities/testimony.dart';

class TestimonyModel {
  final int id;
  final int userId;
  final String posterName;
  final String title;
  final String body;
  final String? category;
  final String status;
  final int praiseCount;
  final bool hasPraised;
  final int? prayerId;
  final LinkedPrayerModel? linkedPrayer;
  final DateTime createdAt;
  final String? profileImageUrl;
  final bool isEdited;

  TestimonyModel({
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
    this.isEdited = false,
  });

  factory TestimonyModel.fromJson(Map<String, dynamic> json) {
    // user_id may be returned as String (e.g. "1") or int
    final rawUserId = json['user_id'];
    final int userId = (rawUserId is int
        ? rawUserId
        : int.tryParse(rawUserId?.toString() ?? '')) ?? 0;

    // poster_name: use direct field or fall back to nested user.name
    final userMap = json['user'] as Map<String, dynamic>?;
    String posterName = json['poster_name'] as String? ?? '';
    if (posterName.isEmpty) {
      posterName = userMap?['name'] as String? ?? '';
    }

    final rawImagePath = userMap?['profile_image_path'] as String?;
    final profileImageUrl = ImageConfig.getProfileImageUrl(rawImagePath);

    return TestimonyModel(
      id: json['id'] as int,
      userId: userId,
      posterName: posterName,
      title: json['title'] as String,
      body: json['body'] as String,
      category: json['category'] as String?,
      status: json['status'] as String,
      praiseCount: json['praise_count'] as int? ?? 0,
      hasPraised: json['has_praised'] as bool? ?? false,
      prayerId: json['prayer_id'] as int?,
      linkedPrayer: json['linked_prayer'] != null
          ? LinkedPrayerModel.fromJson(json['linked_prayer'] as Map<String, dynamic>)
          : null,
      createdAt: DateTime.parse(json['created_at'] as String),
      profileImageUrl: profileImageUrl.isEmpty ? null : profileImageUrl,
      isEdited: json['is_edited'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'user_id': userId,
      'poster_name': posterName,
      'title': title,
      'body': body,
      'category': category,
      'status': status,
      'praise_count': praiseCount,
      'has_praised': hasPraised,
      'prayer_id': prayerId,
      'linked_prayer': linkedPrayer?.toJson(),
      'created_at': createdAt.toIso8601String(),
    };
  }

  Testimony toEntity() {
    return Testimony(
      id: id,
      userId: userId,
      posterName: posterName,
      title: title,
      body: body,
      category: category,
      status: status,
      praiseCount: praiseCount,
      hasPraised: hasPraised,
      prayerId: prayerId,
      linkedPrayer: linkedPrayer?.toEntity(),
      createdAt: createdAt,
      profileImageUrl: profileImageUrl,
      isEdited: isEdited,
    );
  }
}

class LinkedPrayerModel {
  final int id;
  final String bodyExcerpt;
  final int prayCount;
  final bool isAnonymous;

  LinkedPrayerModel({
    required this.id,
    required this.bodyExcerpt,
    required this.prayCount,
    required this.isAnonymous,
  });

  factory LinkedPrayerModel.fromJson(Map<String, dynamic> json) {
    // API may return 'body_excerpt' (in feed) or 'body' (in detail/my-testimonies)
    final excerpt = json['body_excerpt'] as String? ?? json['body'] as String? ?? '';
    return LinkedPrayerModel(
      id: json['id'] as int,
      bodyExcerpt: excerpt,
      prayCount: json['pray_count'] as int? ?? 0,
      isAnonymous: json['is_anonymous'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'body_excerpt': bodyExcerpt,
      'pray_count': prayCount,
      'is_anonymous': isAnonymous,
    };
  }

  LinkedPrayer toEntity() {
    return LinkedPrayer(
      id: id,
      bodyExcerpt: bodyExcerpt,
      prayCount: prayCount,
      isAnonymous: isAnonymous,
    );
  }
}