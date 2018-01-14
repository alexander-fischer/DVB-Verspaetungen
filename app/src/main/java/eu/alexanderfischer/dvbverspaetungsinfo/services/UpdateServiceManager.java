package eu.alexanderfischer.dvbverspaetungsinfo.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

/**
 * Created by Alexander Fischer.
 *
 * Class for handling background jobs for the app.
 */
public class UpdateServiceManager {

    /**
     * Starts the service to request data in a background job. Is started every time because the
     * AlarmManager service is sometimes removed.
     */
    public static void startUpdateService(Context context) {
        Intent uTServiceIntent = new Intent(context, UpdateTweetsService.class);
        PendingIntent uTServicePendingIntent =
                PendingIntent.getService(context, 0, uTServiceIntent, 0);

        // every 5 minutes because of battery saving.
        long interval = DateUtils.MINUTE_IN_MILLIS * 1;

        long firstStart = System.currentTimeMillis() + 10000L;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC, firstStart, interval,
                uTServicePendingIntent);
    }

}
