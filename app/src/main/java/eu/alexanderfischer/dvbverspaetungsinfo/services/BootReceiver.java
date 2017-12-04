package eu.alexanderfischer.dvbverspaetungsinfo.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * Created by alexf_000 on 26.08.2015.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent uTServiceIntent = new Intent(context, UpdateTweetsService.class);
            PendingIntent uTServicePendingIntent =
                    PendingIntent.getService(context, 0, uTServiceIntent, 0);

            // every 5 minutes
            long interval = DateUtils.MINUTE_IN_MILLIS * 5;

            long firstStart = System.currentTimeMillis() + 10000L;

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setInexactRepeating(AlarmManager.RTC, firstStart, interval,
                    uTServicePendingIntent);

            //Log.e("TEST", "AlarmManager gesetzt");
        }
    }


}
