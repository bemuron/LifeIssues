import 'dart:io';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:home_widget/home_widget.dart';
import 'package:intl/intl.dart';
import 'package:path_provider/path_provider.dart';
import '../../core/constants/image_config.dart';
import '../../domain/entities/verse.dart';
import '../../domain/usecases/get_daily_verse.dart';

class HomeScreenWidgetProvider {
  static const String androidWidgetName = 'DailyVerseWidgetProvider';
  static const String iosWidgetName = 'DailyVerseWidget';

  static Future<void> updateWidget(
    Verse verse,
    String bibleVersion,
  ) async {
    try {
      String verseText = verse.getVersion(bibleVersion);

      // Limit verse text to 200 characters for widget display
      if (verseText.length > 200) {
        verseText = '${verseText.substring(0, 197)}...';
      }

      // Format today's date
      final dateStr = DateFormat('MMM d, yyyy').format(DateTime.now());

      // Save core verse data
      await HomeWidget.saveWidgetData<String>('daily_verse_reference', verse.reference);
      await HomeWidget.saveWidgetData<String>('daily_verse_text', verseText);
      await HomeWidget.saveWidgetData<String>('daily_verse_version', bibleVersion);
      await HomeWidget.saveWidgetData<String>('daily_verse_date', dateStr);
      await HomeWidget.saveWidgetData<String>(
        'daily_verse_category',
        verse.issueName ?? '',
      );
      await HomeWidget.saveWidgetData<String>(
        'daily_verse_updated_at',
        DateTime.now().toIso8601String(),
      );

      // Try to download and cache the category background image
      final imagePath = await _cacheBackgroundImage(verse);
      await HomeWidget.saveWidgetData<String>(
        'daily_verse_image_path',
        imagePath ?? '',
      );

      await HomeWidget.updateWidget(
        name: androidWidgetName,
        iOSName: iosWidgetName,
      );
    } catch (e) {
      debugPrint('Error updating home screen widget: $e');
    }
  }

  /// Downloads the issue/category image and caches it locally.
  /// Returns the local file path on success, null on failure.
  static Future<String?> _cacheBackgroundImage(Verse verse) async {
    try {
      // Build the image URL from the issue name
      final issueName = verse.issueName;
      if (issueName == null || issueName.isEmpty) return null;

      final imageUrl = ImageConfig.getIssueImageUrl(issueName.toLowerCase());
      if (imageUrl.isEmpty) return null;

      final dir = await getApplicationDocumentsDirectory();
      final filePath = '${dir.path}/widget_bg.jpg';

      final dio = Dio();
      await dio.download(
        imageUrl,
        filePath,
        options: Options(receiveTimeout: const Duration(seconds: 10)),
      );

      // Verify the file was written
      final file = File(filePath);
      if (await file.exists() && await file.length() > 0) {
        return filePath;
      }
      return null;
    } catch (e) {
      debugPrint('Widget image cache failed: $e');
      return null;
    }
  }

  static Future<void> updateDailyVerseWidget(
    GetDailyVerse getDailyVerse,
    String bibleVersion,
  ) async {
    try {
      final verse = await getDailyVerse();
      await updateWidget(verse, bibleVersion);
    } catch (e) {
      debugPrint('Error updating daily verse widget: $e');
    }
  }

  static Future<void> clearWidget() async {
    try {
      await HomeWidget.saveWidgetData<String>('daily_verse_reference', '');
      await HomeWidget.saveWidgetData<String>('daily_verse_text', '');
      await HomeWidget.saveWidgetData<String>('daily_verse_category', '');
      await HomeWidget.saveWidgetData<String>('daily_verse_image_path', '');
      await HomeWidget.updateWidget(
        name: androidWidgetName,
        iOSName: iosWidgetName,
      );
    } catch (e) {
      debugPrint('Error clearing widget: $e');
    }
  }

  static Future<bool> isWidgetSupported() async {
    return true; // home_widget package handles platform checks
  }
}
