import 'package:flutter/material.dart';
import 'package:home_widget/home_widget.dart';
import '../../domain/entities/verse.dart';
import '../../domain/usecases/get_daily_verse.dart';
import '../blocs/settings/settings_bloc.dart';

class HomeScreenWidgetProvider {
  static const String androidWidgetName = 'DailyVerseWidgetProvider';
  static const String iosWidgetName = 'DailyVerseWidget';

  static Future<void> updateWidget(
      Verse verse,
      String bibleVersion,
      ) async {
    try {
      String verseText;
      switch (bibleVersion) {
        case 'kjv':
          verseText = verse.kjv;
          break;
        case 'msg':
          verseText = verse.msg!;
          break;
        case 'amp':
          verseText = verse.amp!;
          break;
        default:
          verseText = verse.kjv;
      }

      // Limit verse text to 150 characters for widget display
      if (verseText.length > 150) {
        verseText = '${verseText.substring(0, 147)}...';
      }

      await HomeWidget.saveWidgetData<String>('verse_reference', verse.reference);
      await HomeWidget.saveWidgetData<String>('verse_text', verseText);
      await HomeWidget.saveWidgetData<String>('bible_version', bibleVersion);
      await HomeWidget.saveWidgetData<String>(
        'updated_at',
        DateTime.now().toIso8601String(),
      );

      // Update the widget
      await HomeWidget.updateWidget(
        name: androidWidgetName,
        iOSName: iosWidgetName,
      );
    } catch (e) {
      debugPrint('Error updating home screen widget: $e');
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
      await HomeWidget.saveWidgetData<String>('verse_reference', '');
      await HomeWidget.saveWidgetData<String>('verse_text', '');
      await HomeWidget.updateWidget(
        name: androidWidgetName,
        iOSName: iosWidgetName,
      );
    } catch (e) {
      debugPrint('Error clearing widget: $e');
    }
  }

  static Future<bool> isWidgetSupported() async {
    try {
      // Check if platform supports widgets
      return true; // home_widget package handles platform checks
    } catch (e) {
      return false;
    }
  }
}