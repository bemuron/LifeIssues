package com.lifeissues.lifeissues.helpers;


import android.util.Log;

import androidx.work.BackoffPolicy;
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
    int repeatInterval = 1; // In days

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

        /*long flexTime = calculateFlex(reminderTime, repeatInterval);

        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest
                .Builder(NotifyWorker.class, repeatInterval, TimeUnit.DAYS, flexTime, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(workTag)
                .build();*/
        //WorkManager workManager = WorkManager.getInstance(context);
        //workManager.enqueueUniquePeriodicWork(PERIODIC_WORK_KEY, ExistingPeriodicWorkPolicy.REPLACE, workRequest);

        //Log.i("WorkRequest","Starting WorkManager instance");
        WorkManager.getInstance(AppController.getContext())
                .enqueueUniquePeriodicWork(
                        "daily_notification_work",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        notificationWork
                );
        //WorkManager.getInstance(AppController.getInstance()).enqueue(notificationWork);
    }

    private long calculateDelay(Calendar c){
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm", Locale.getDefault());
        String currentDate = dateTimeFormat.format(Calendar.getInstance().getTime());
        String reminderDate = dateTimeFormat.format(c.getTime());
        //Log.i("WorkRequest","Reminder time "+reminderDate+" Device time "+currentDate);
        return c.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }

    private long calculateFlex(Calendar c, int periodInDays) {
        int minOfTheHour = 0;
        int hourOfTheDay = 6;
        SimpleDateFormat hourFormat = new SimpleDateFormat("hh", Locale.getDefault());
        SimpleDateFormat minFormat = new SimpleDateFormat("mm", Locale.getDefault());

        String remindHour = hourFormat.format(c.getTime());
        String remindMin = minFormat.format(c.getTime());
        hourOfTheDay = Integer.parseInt(remindHour);
        minOfTheHour = Integer.parseInt(remindMin);

        // Initialize the calendar with today and the preferred time to run the job.
        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        cal1.set(Calendar.MINUTE, minOfTheHour);
        cal1.set(Calendar.SECOND, 0);

        // Initialize a calendar with now.
        Calendar cal2 = Calendar.getInstance();

        if (cal2.getTimeInMillis() < cal1.getTimeInMillis()) {
            // Add the worker periodicity.
            cal2.setTimeInMillis(cal2.getTimeInMillis() + TimeUnit.DAYS.toMillis(periodInDays));
        }

        long delta = (cal2.getTimeInMillis() - cal1.getTimeInMillis());

        return ((delta > PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS) ? delta
                : PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS);
    }


}
