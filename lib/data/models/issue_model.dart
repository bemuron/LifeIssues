import '../../domain/entities/issue.dart';

class IssueModel {
  final int id;
  final String name;
  final String description;
  final bool isFavorite;
  final String? image;

  IssueModel({
    required this.id,
    required this.name,
    required this.description,
    this.isFavorite = false,
    this.image,
  });

  factory IssueModel.fromMap(Map<String, dynamic> map) {
    return IssueModel(
      id: map['issue_id'] as int,
      name: map['name'] as String,
      description: map['description'] as String? ?? '',
      isFavorite: (map['is_favorite'] as int?) == 1,
      image: map['image'] as String?,
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'issue_id': id,
      'name': name,
      'description': description,
      'is_favorite': isFavorite ? 1 : 0,
      'image': image,
    };
  }

  Issue toEntity() {
    return Issue(
      id: id,
      name: name,
      description: description,
      isFavorite: isFavorite,
      image: image,
    );
  }

  factory IssueModel.fromEntity(Issue issue) {
    return IssueModel(
      id: issue.id,
      name: issue.name,
      description: issue.description,
      isFavorite: issue.isFavorite,
      image: issue.image,
    );
  }
}