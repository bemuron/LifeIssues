package com.lifeissues.lifeissues.helpers;


import android.util.Log;

import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.lifeissues.lifeissues.app.AppController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WorkRequest {
    //we set a tag to be able to cancel all work of this type if needed
    public static final String workTag = "notificationWork";

    public void setPhraseNotification(int verseId, String bibleVerse, String verse, String version, Calendar reminderTime) {
        //store DBEventID to pass it to the PendingIntent and open the appropriate event page on notification click
        Data inputData = new Data.Builder()
                .putInt("verseId", verseId)
                .putString("bibleVerse", bibleVerse)
                .putString("verse", verse)
                .putString("version", version)
                .build();
        // we then retrieve it inside the NotifyWorker with:
        // final int DBEventID = getInputData().getInt(DBEventIDTag, ERROR_VALUE);

        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest
                .Builder(NotifyWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(calculateDelay(reminderTime), TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(workTag)
                .build();

        Log.i("WorkRequest","Starting WorkManager instance");
        WorkManager.getInstance(AppController.getContext())
                .enqueueUniquePeriodicWork(
                        "daily_notification_work",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        notificationWork
                );
        //WorkManager.getInstance(AppController.getInstance()).enqueue(notificationWork);
    }

    private long calculateDelay(Calendar c){
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm", Locale.getDefault());
        String currentDate = dateTimeFormat.format(Calendar.getInstance().getTime());
        String reminderDate = dateTimeFormat.format(c.getTime());
        Log.i("WorkRequest","Reminder time "+reminderDate+" Device time "+currentDate);
        return c.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }


}
