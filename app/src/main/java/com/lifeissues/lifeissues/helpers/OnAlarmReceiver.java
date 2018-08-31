package com.lifeissues.lifeissues.helpers;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.util.Log;

/**
 * Created by Emo on 6/17/2017.
 */

public class OnAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = ComponentInfo.class.getCanonicalName();


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received wake up from alarm manager.");

        int vId = intent.getIntExtra("verseID",0);
        String verse = intent.getStringExtra("verse");
        String content = intent.getStringExtra("content");

        WakeReminderIntentService.acquireStaticLock(context);

        Intent i = new Intent(context, ReminderService.class);
        i.putExtra("verseID", vId);
        i.putExtra("verse", verse);
        i.putExtra("content", content);
        context.startService(i);

    }
}
