package com.lifeissues.lifeissues.helpers;

/**
 * Created by Emo on 6/17/2017.
 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.lifeissues.lifeissues.activities.MainActivity;

import com.lifeissues.lifeissues.R;

public class ReminderService extends WakeReminderIntentService {
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;

    public ReminderService() {
        super("ReminderService");
    }

    @Override
    void doReminderWork(Intent intent) {
        Log.d("ReminderService", "Doing work.");
        int vId = intent.getIntExtra("verseID",0);
        String verse = intent.getStringExtra("verse");
        String content = intent.getStringExtra("content");

        //NotificationManager mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE ) ;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("verseID", vId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //notificationIntent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getActivity(this, vId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent.FLAG_ONE_SHOT: Flag indicating that this PendingIntent can be used only once.

        //NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext() , default_notification_channel_id ) ;
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
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new
                    NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(( int ) System. currentTimeMillis () , builder.build()) ;
        //mNotificationManager.notify();
        throw new UnsupportedOperationException( "Not yet implemented" ) ;


    }
}
