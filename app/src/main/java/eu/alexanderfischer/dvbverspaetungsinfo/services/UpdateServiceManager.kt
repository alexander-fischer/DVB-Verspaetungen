package eu.alexanderfischer.dvbverspaetungsinfo.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils

/**
 * Created by Alexander Fischer.
 *
 * Class for handling background jobs for the app.
 */
object UpdateServiceManager {

    /**
     * Starts the service to request data in a background job. Is started every time because the
     * AlarmManager service is sometimes removed.
     */
    fun startUpdateService(context: Context) {
        val uTServiceIntent = Intent(context, UpdateTweetsService::class.java)
        val uTServicePendingIntent = PendingIntent.getService(context, 0, uTServiceIntent, 0)

        // every 5 minutes because of battery saving.
        val interval = DateUtils.MINUTE_IN_MILLIS * 5

        val firstStart = System.currentTimeMillis() + 10000L

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setInexactRepeating(AlarmManager.RTC, firstStart, interval,
                uTServicePendingIntent)
    }

}
