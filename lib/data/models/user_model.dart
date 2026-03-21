// lib/data/models/user_model.dart

import '../../core/config/api_config.dart';
import '../../core/constants/image_config.dart';
import '../../domain/entities/user.dart';

class UserModel {
  final int id;
  final String name;
  final String email;
  final String? token;
  final String? profileImageUrl;

  UserModel({
    required this.id,
    required this.name,
    required this.email,
    this.token,
    this.profileImageUrl,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    // profile_image_path is the key used by the backend (e.g. "profile_images/abc.jpg")
    // and needs the /storage/ base URL.  Other possible keys are resolved via host.
    final rawPath = json['profile_image_path'] as String?;
    String? resolvedImageUrl;
    if (rawPath != null && rawPath.isNotEmpty) {
      final url = ImageConfig.getProfileImageUrl(rawPath);
      resolvedImageUrl = url.isEmpty ? null : url;
    } else {
      resolvedImageUrl = _resolveImageUrl(
        json['profile_image_url'] as String? ??
            json['profile_pic'] as String? ??
            json['avatar'] as String?,
      );
    }

    return UserModel(
      id: json['id'] as int,
      name: json['name'] as String,
      email: json['email'] as String,
      token: json['token'] as String?,
      profileImageUrl: resolvedImageUrl,
    );
  }

  /// If the API returns a relative path (e.g. /storage/…) instead of a full
  /// URL, prepend the configured host so it can be loaded as an image.
  static String? _resolveImageUrl(String? raw) {
    if (raw == null || raw.isEmpty) return null;
    if (raw.startsWith('http://') || raw.startsWith('https://')) return raw;
    final path = raw.startsWith('/') ? raw : '/$raw';
    return '${ApiConfig.host}$path';
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'token': token,
      'profile_image_url': profileImageUrl,
    };
  }

  User toEntity() {
    return User(
      id: id,
      name: name,
      email: email,
      token: token,
      profileImageUrl: profileImageUrl,
    );
  }

  factory UserModel.fromEntity(User user) {
    return UserModel(
      id: user.id,
      name: user.name,
      email: user.email,
      token: user.token,
      profileImageUrl: user.profileImageUrl,
    );
  }
}
