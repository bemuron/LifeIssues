package com.example.life_issues;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import com.example.life_issues.R;

/**
 * Daily Verse Widget Provider
 * Displays the daily Bible verse on the home screen.
 *
 * Data flow:
 *   Flutter (shared_preferences / home_widget package)
 *     → writes "daily_verse_text" and "daily_verse_reference"
 *       into the SharedPreferences file named
 *       "FlutterSharedPreferences" (the default file used by the
 *       Flutter shared_preferences plugin, prefixed with "flutter.").
 *     → calls HomeWidget.updateWidget() to trigger onUpdate here.
 *
 *   This provider reads those keys and populates the widget views.
 *
 * Key names must match exactly what Flutter writes.  The
 * shared_preferences plugin stores every key with a "flutter." prefix,
 * so a Dart key of "daily_verse_text" becomes
 * "flutter.daily_verse_text" in the native file.
 */
public class DailyVerseWidgetProvider extends AppWidgetProvider {

    // Name of the SharedPreferences file written by the Flutter
    // shared_preferences plugin.
    private static final String PREFS_NAME = "FlutterSharedPreferences";

    // Keys — must match the Dart keys used when saving, prefixed with
    // "flutter." because that is what the plugin prepends automatically.
    private static final String KEY_VERSE_TEXT      = "flutter.daily_verse_text";
    private static final String KEY_VERSE_REFERENCE = "flutter.daily_verse_reference";

    // Fallback strings shown before Flutter has written any data.
    private static final String DEFAULT_VERSE_TEXT      = "Open the app to load today\u2019s verse.";
    private static final String DEFAULT_VERSE_REFERENCE = "";

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private static void updateAppWidget(Context context,
                                        AppWidgetManager appWidgetManager,
                                        int appWidgetId) {
        // --- Read verse data saved by Flutter ----------------------------
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String verseText = prefs.getString(KEY_VERSE_TEXT, DEFAULT_VERSE_TEXT);
        String verseReference = prefs.getString(KEY_VERSE_REFERENCE, DEFAULT_VERSE_REFERENCE);

        // Guard: show fallback if Flutter wrote an empty string
        if (verseText == null || verseText.trim().isEmpty()) {
            verseText = DEFAULT_VERSE_TEXT;
        }

        // --- Populate the widget layout ----------------------------------
        RemoteViews views = new RemoteViews(
                context.getPackageName(), R.layout.daily_verse_widget);

        views.setTextViewText(R.id.verse_text, verseText);
        views.setTextViewText(R.id.verse_reference, verseReference);

        // --- Push update to the widget manager --------------------------
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onEnabled(Context context) {
        // Called when the first instance of the widget is placed.
        // Nothing extra needed; onUpdate will fire immediately after.
    }

    @Override
    public void onDisabled(Context context) {
        // Called when the last instance of the widget is removed.
    }
}