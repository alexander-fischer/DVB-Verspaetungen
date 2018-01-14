package eu.alexanderfischer.dvbverspaetungsinfo.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val uTServiceIntent = Intent(context, UpdateTweetsService::class.java)
            val uTServicePendingIntent = PendingIntent.getService(context, 0, uTServiceIntent, 0)

            // every 5 minutes
            val interval = DateUtils.MINUTE_IN_MILLIS * 5

            val firstStart = System.currentTimeMillis() + 10000L

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setInexactRepeating(AlarmManager.RTC, firstStart, interval,
                    uTServicePendingIntent)
        }
    }
}
