package it.saezzt.avisalbissole.util;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import it.saezzt.avisalbissole.R;

public class Service extends IntentService {
    public Service(String name) {
        super(name);
    }
    public Service() {
        super(null);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String intentType = intent.getExtras().getString("caller");
        if(intentType == null) return;
        int i;
        Log.i("AVISBOOT","service");
        if(intentType.equals("RebootReceiver")){
            int NOTIFICATION_ID = 1234;
            String CHANNEL_ID = "AVIS";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.applogo03)
                    .setContentTitle("R.string.ambunotify")
                    .setContentText("R.string.donazione")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    //.setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.i("AVISBOOT","notifysummoned");
        }
           i = 0;
        //Do reboot stuff
        //handle other types of callers, like a notification.
    }
}
