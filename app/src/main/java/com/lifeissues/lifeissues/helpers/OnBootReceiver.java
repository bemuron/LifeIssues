package com.lifeissues.lifeissues.helpers;

/**
 * Created by Emo on 6/17/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.lifeissues.lifeissues.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import database.DatabaseTable;

public class OnBootReceiver extends BroadcastReceiver {

    private static final String TAG = ComponentInfo.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {

        ReminderManager reminderMgr = new ReminderManager(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        DatabaseTable dbHelper = new DatabaseTable(context);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateToday = df.format(c.getTime());

        Cursor cursor = dbHelper.getDailyVerse(dateToday);
        setReminder(prefs,cursor, reminderMgr);



    }
    private void setReminder(SharedPreferences prefs, Cursor c, ReminderManager manager){
        String defaultVersionKey = "key_daily_verse_version";
        String defaultVersion = prefs.getString(defaultVersionKey, "kjv");

        if (!defaultVersion.equals("kjv")) {
            setUpReminder(c, defaultVersion, prefs, manager);
        }else{
            setUpReminder(c, "kjv", prefs, manager);
        }

    }

    private void setUpReminder(Cursor cursor, String version, SharedPreferences preferences, ReminderManager manager){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateToday = df.format(c.getTime());


        if(cursor != null) {
            cursor.moveToFirst();

            String issueName = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME_ID));
            int verseID = cursor.getInt(cursor.getColumnIndex(DatabaseTable.KEY_ID));
            String bibleVerse = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_AMP));

            switch (version){
                case "kjv":
                    dailyVerseNotification(verseID,bibleVerse,kjvVerseContent, preferences,manager);
                    break;
                case "msg":
                    dailyVerseNotification(verseID,bibleVerse,msgVerseContent, preferences, manager);
                    break;
                case "amp":
                    dailyVerseNotification(verseID,bibleVerse,ampVerseContent, preferences, manager);
                    break;
                default:
                    dailyVerseNotification(verseID,bibleVerse,kjvVerseContent, preferences, manager);
            }

        }
    }

    private void dailyVerseNotification(int verseID, String verse, String content, SharedPreferences prefs,
                                        ReminderManager manager){
        String defaultTimeKey = "key_daily_verse_time";
        String defaultTime = prefs.getString(defaultTimeKey, null);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("hh:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm", Locale.getDefault());
        String currentDate = dateFormat.format(c.getTime());
        //MMM dd, yyyy - h:mm a
        String reminderDateTime;

        if (defaultTime != null) {
            reminderDateTime = currentDate + " - " + defaultTime;
            //notify user
            Date reminderTime = null;
            try {
                reminderTime = dateTimeFormat.parse(reminderDateTime);
                c.setTime(reminderTime);
            } catch (ParseException e) {
                e.getMessage();
                e.printStackTrace();
            }

            System.out.println("*******TIME = "+ c.getTime());
            manager.setReminder(verseID, verse, content, c);
        }

    }
}
