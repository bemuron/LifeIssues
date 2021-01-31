package com.lifeissues.lifeissues.helpers;

/**
 * Created by Emo on 6/17/2017.
 */

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ComponentInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.data.database.DatabaseTable;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;

public class OnBootReceiver extends BroadcastReceiver {

    private static final String TAG = ComponentInfo.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {

        ReminderManager reminderMgr = new ReminderManager(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        LifeIssuesRepository issuesRepository = new LifeIssuesRepository((Application) context);

        Cursor cursor = null;

        new getDailyVerseOnBootAsync(reminderMgr,prefs,issuesRepository, cursor).execute();



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

    //async task to get daily verse from daily verse table
    private class getDailyVerseOnBootAsync extends AsyncTask<Void, Void, Void> {
        private ReminderManager rm;
        private SharedPreferences pref;
        private LifeIssuesRepository repo;
        private Cursor cursor;

        getDailyVerseOnBootAsync(ReminderManager reminderManager, SharedPreferences prefs,
                                         LifeIssuesRepository issuesRepository, Cursor cursor){
            this.rm = reminderManager;
            this.pref = prefs;
            this.repo = issuesRepository;
            this.cursor = cursor;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateToday = df.format(c.getTime());

            cursor = repo.getDailyVerse(dateToday);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e(TAG,"In post execute");
            setReminder(pref,cursor, rm);
        }
    }
}
