package it.saezzt.avisalbissole.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import it.saezzt.avisalbissole.Main;
import it.saezzt.avisalbissole.R;

public class AvisReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1234;
    private static final String CHANNEL_ID = "AVIS";

    @Override
    public void onReceive(Context context, Intent intent) {
        String SP = context.getResources().getString(R.string.SP_name);
        SharedPreferences sharedPref = context.getSharedPreferences(SP, Context.MODE_PRIVATE);
        String title = sharedPref.getString(context.getResources().getString(R.string.AlarmSet),"");
        String details = "Clicca ed entra per controllare i dettagli dell'evento";

        Log.i("Alarm","onReceiver");
        Log.i("createNotification","summoned");
        // Create an explicit intent for an Activity in your app
        Intent actionIntent = new Intent(context, Main.class);
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, actionIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.applogo03)
                .setContentTitle(title)
                .setContentText(details)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //qui il codice per mostrare la notifica
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        //Remember to save the notification ID that you pass to NotificationManagerCompat.notify() because you'll need it later if you want to update or remove the notification.
    }
}
