package it.saezzt.avisalbissole.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AvisBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        /*
        if(action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED) ) {
                Log.i("AVISBOOT","receiver");
                //TODO: Code to handle BOOT COMPLETED EVENT
                //TODO: I can start an Service.. display a notification... start an activity
                SharedPreferences sharedPreferences = context.getSharedPreferences("setup",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Boolean set = sharedPreferences.getBoolean("AlarmSet",false);
                Long time = sharedPreferences.getLong("AlarmTime",new Long(0));
                if(set){
                    //inizializzo broadcast per lancio notifiche NOTIFY
                    Intent serviceIntent = new Intent(context, Service.class);
                    serviceIntent.putExtra("caller", "RebootReceiver");
                    context.startService(serviceIntent);

                    //Intent newintent = new Intent(context, AvisReceiver.class);
                    //newintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newintent, 0);
                    //AlarmManager manager = (AlarmManager) getSystemService(context.ALARM_SERVICE);
                    //manager.setExact(AlarmManager.RTC_WAKEUP,time,pendingIntent);

                }
            }
        }*/
    }
}
