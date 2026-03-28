package com.lifeissues.lifeissues;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.RemoteViews;

import java.io.File;

/**
 * Daily Verse Widget Provider
 *
 * Data is written by Flutter via the home_widget package, which stores values in
 * a SharedPreferences file named "HomeWidgetPreferences" with keys stored as-is
 * (no "flutter." prefix). Keys must match exactly what HomeWidget.saveWidgetData
 * writes in Dart.
 *
 * When a category image is available (downloaded by Flutter and cached locally),
 * it is shown as the widget background with a dark overlay. Otherwise the widget
 * falls back to the deep-purple gradient defined in widget_background.xml.
 */
public class DailyVerseWidgetProvider extends AppWidgetProvider {

    // SharedPreferences file written by the home_widget Flutter package.
    private static final String PREFS_NAME = "HomeWidgetPreferences";

    // Keys — must match exactly what Dart passes to HomeWidget.saveWidgetData.
    private static final String KEY_VERSE_TEXT      = "daily_verse_text";
    private static final String KEY_VERSE_REFERENCE = "daily_verse_reference";
    private static final String KEY_VERSE_VERSION   = "daily_verse_version";
    private static final String KEY_VERSE_CATEGORY  = "daily_verse_category";
    private static final String KEY_VERSE_DATE      = "daily_verse_date";
    private static final String KEY_IMAGE_PATH      = "daily_verse_image_path";

    // Fallback strings shown before Flutter has written any data.
    private static final String DEFAULT_VERSE_TEXT =
            "Open the app to load today\u2019s verse.";

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id);
        }
    }

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String verseText      = prefs.getString(KEY_VERSE_TEXT, DEFAULT_VERSE_TEXT);
        String verseReference = prefs.getString(KEY_VERSE_REFERENCE, "");
        String verseVersion   = prefs.getString(KEY_VERSE_VERSION, "");
        String category       = prefs.getString(KEY_VERSE_CATEGORY, "");
        String date           = prefs.getString(KEY_VERSE_DATE, "");
        String imagePath      = prefs.getString(KEY_IMAGE_PATH, "");

        if (verseText == null || verseText.trim().isEmpty()) {
            verseText = DEFAULT_VERSE_TEXT;
        }

        RemoteViews views = new RemoteViews(
                context.getPackageName(), R.layout.daily_verse_widget);

        // ── Verse text & reference ───────────────────────────────────────────
        views.setTextViewText(R.id.verse_text, verseText);
        views.setTextViewText(R.id.verse_reference,
                verseReference != null ? verseReference : "");
        views.setTextViewText(R.id.verse_version,
                verseVersion != null ? verseVersion : "");

        // ── Date ────────────────────────────────────────────────────────────
        views.setTextViewText(R.id.verse_date,
                date != null && !date.isEmpty() ? date : "");

        // ── Category chip ────────────────────────────────────────────────────
        if (category != null && !category.isEmpty()) {
            views.setTextViewText(R.id.verse_category, category.toUpperCase());
            views.setViewVisibility(R.id.verse_category, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.verse_category, View.GONE);
        }

        // ── Background image ─────────────────────────────────────────────────
        boolean imageLoaded = false;
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists() && imageFile.length() > 0) {
                Bitmap bmp = decodeSampledBitmap(imagePath, 600, 300);
                if (bmp != null) {
                    views.setImageViewBitmap(R.id.widget_bg_image, bmp);
                    views.setViewVisibility(R.id.widget_bg_image, View.VISIBLE);
                    views.setViewVisibility(R.id.widget_dark_overlay, View.VISIBLE);
                    imageLoaded = true;
                }
            }
        }

        if (!imageLoaded) {
            views.setViewVisibility(R.id.widget_bg_image, View.GONE);
            views.setViewVisibility(R.id.widget_dark_overlay, View.GONE);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Decode a bitmap scaled down to fit within reqWidth × reqHeight,
     * reducing memory use for large category images.
     */
    private static Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                              int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width  = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth  = width  / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth  / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onEnabled(Context context) {}

    @Override
    public void onDisabled(Context context) {}
}
