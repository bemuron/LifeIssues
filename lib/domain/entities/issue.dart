// lib/domain/entities/issue.dart
class Issue {
  final int id;
  final String name;
  final String description;
  final bool isFavorite;
  final String? image;

  Issue({
    required this.id,
    required this.name,
    required this.description,
    this.isFavorite = false,
    this.image,
  });
}