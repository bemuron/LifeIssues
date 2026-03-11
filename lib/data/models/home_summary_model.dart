// lib/data/models/home_summary_model.dart

class HomeSummaryModel {
  final int unansweredPrayerCount;
  final LatestTestimonyModel? latestTestimony;

  HomeSummaryModel({
    required this.unansweredPrayerCount,
    this.latestTestimony,
  });

  factory HomeSummaryModel.fromJson(Map<String, dynamic> json) {
    return HomeSummaryModel(
      unansweredPrayerCount: json['unanswered_prayer_count'] as int? ?? 0,
      latestTestimony: json['latest_testimony'] != null
          ? LatestTestimonyModel.fromJson(
          json['latest_testimony'] as Map<String, dynamic>)
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'unanswered_prayer_count': unansweredPrayerCount,
      'latest_testimony': latestTestimony?.toJson(),
    };
  }
}

class LatestTestimonyModel {
  final int id;
  final String title;

  LatestTestimonyModel({
    required this.id,
    required this.title,
  });

  factory LatestTestimonyModel.fromJson(Map<String, dynamic> json) {
    return LatestTestimonyModel(
      id: json['id'] as int,
      title: json['title'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
    };
  }
}