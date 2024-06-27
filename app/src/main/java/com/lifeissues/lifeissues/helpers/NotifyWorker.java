package com.lifeissues.lifeissues.helpers;

import android.Manifest;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.ui.activities.BibleVerses;
import com.lifeissues.lifeissues.ui.activities.MainActivity;

public class NotifyWorker extends Worker {
    private Context mContext;
    public static final String NOTIFICATION_CHANNEL_ID = "10002";

    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        mContext = context;
    }
    @NonNull
    @Override
    public Result doWork() {
        // Method to trigger the phrase notification
        triggerPhraseNotification();

        return Result.success();
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    private void triggerPhraseNotification(){
        Log.i("PhraseNotification","Notification triggered");
        int verseId = getInputData().getInt("verseId", 0);
        String bibleVerse = getInputData().getString("bibleVerse");
        String verseContent = getInputData().getString("verse");
        String version = getInputData().getString("version");

        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(mContext);

        Intent notificationIntent = new Intent(mContext, BibleVerses.class);
        notificationIntent.putExtra("V-ID", verseId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntentWithParentStack(notificationIntent);

        PendingIntent pi;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Get the PendingIntent containing the entire back stack
            //pi = PendingIntent.getBroadcast(mContext, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            pi = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pi = stackBuilder.getPendingIntent(0,
                    PendingIntent.FLAG_IMMUTABLE);
            //pi = PendingIntent.getBroadcast(mContext, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        builder.setAutoCancel(true);
        //builder.setTicker("this is ticker text");
        builder.setContentTitle(bibleVerse);
        builder.setContentText(verseContent);
        builder.setSmallIcon(R.mipmap.ic_notification_icon);
        builder.setContentIntent(pi);
        //builder.setDeleteIntent(pi);
        builder.setOngoing(false);
        builder.setSubText("Your Daily Life Issues Verse");   //API level 16
        //builder.setNumber(phraseId);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(verseContent));
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        //builder.setSound();
        //mNotificationManager.notify(( int ) System. currentTimeMillis () , builder.build()) ;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                mNotificationManager.notify(Integer.parseInt(NOTIFICATION_CHANNEL_ID), builder.build());
            }
        }

        mNotificationManager.notify(Integer.parseInt(NOTIFICATION_CHANNEL_ID), builder.build());
    }
}
