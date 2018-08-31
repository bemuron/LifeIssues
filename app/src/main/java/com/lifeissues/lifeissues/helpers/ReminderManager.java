package com.lifeissues.lifeissues.helpers;

/**
 * Created by Emo on 6/17/2017.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class ReminderManager {

    private Context mContext;
    private AlarmManager mAlarmManager;

    public ReminderManager(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setReminder(int verseID, String verse, String content, Calendar reminderTime) {

        Intent i = new Intent(mContext, OnAlarmReceiver.class);
        i.putExtra("verseID", verseID);
        i.putExtra("verse", verse);
        i.putExtra("content", content);

        PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pi);
    }

}
