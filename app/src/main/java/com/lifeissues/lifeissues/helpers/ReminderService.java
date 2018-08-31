package com.lifeissues.lifeissues.helpers;

/**
 * Created by Emo on 6/17/2017.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.lifeissues.lifeissues.activities.MainActivity;

import com.lifeissues.lifeissues.R;

public class ReminderService extends WakeReminderIntentService {

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    void doReminderWork(Intent intent) {
        Log.d("ReminderService", "Doing work.");
        int vId = intent.getIntExtra("verseID",0);
        String verse = intent.getStringExtra("verse");
        String content = intent.getStringExtra("content");

        NotificationManager mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("verseID", vId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getActivity(this, vId, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        //PendingIntent.FLAG_UPDATE_CURRENT

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        //builder.setTicker("this is ticker text");
        builder.setContentTitle("Life Issues Daily Verse");
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_notification_icon);
        builder.setContentIntent(pi);
        //builder.setDeleteIntent(pi);
        builder.setOngoing(false);
        builder.setSubText(verse);   //API level 16
        //builder.setNumber(vId);
        //builder.setSound();
        //builder.build();

        //myNotication = builder.getNotification();
        //score_explanation = builder.build();
        // An issue could occur if user ever enters over 2,147,483,647 tasks. (Max int value).
        // I highly doubt this will ever happen. But is good to score_explanation.
        //int id = rowId;
        mgr.notify(vId, builder.build());
/*
        Notification score_explanation=new Notification(android.R.drawable.stat_sys_warning, getString(R.string.notify_new_task_message), System.currentTimeMillis());
        score_explanation.setLatestEventInfo(this, getString(R.string.notify_new_task_title), getString(R.string.notify_new_task_message), pi);
        score_explanation.defaults |= Notification.DEFAULT_SOUND;
        score_explanation.flags |= Notification.FLAG_AUTO_CANCEL;
*/



    }
}
