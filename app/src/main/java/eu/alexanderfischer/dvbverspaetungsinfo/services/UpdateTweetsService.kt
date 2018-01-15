package eu.alexanderfischer.dvbverspaetungsinfo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import eu.alexanderfischer.dvbverspaetungsinfo.networking.DelayController
import eu.alexanderfischer.dvbverspaetungsinfo.ui.NotificationHelper
import org.jetbrains.anko.doAsync

class UpdateTweetsService : Service() {

    private val TAG = UpdateTweetsService::class.java.simpleName

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {}

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        doAsync {
            val delaysFromDb = Delay.allDelays()
            val latestIdFromDb = delaysFromDb[0].id.toLong()

            val res = DelayController.syncDelays()
            if (res.isSuccessful) {
                res.body()?.apply {
                    this.filter { it.id.toLong() > latestIdFromDb }
                            .forEach { sendNotification(it) }
                }
            }
        }

        stopSelf()

        return Service.START_NOT_STICKY
    }

    private fun sendNotification(delay: Delay) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val hasActivatedNotifications = sharedPref.getBoolean("notifications", false)

        if (hasActivatedNotifications && delay.linien.size > 0) {
            val helper = NotificationHelper(this)
            helper.sendNotification(delay)
        }
    }

    override fun onDestroy() {}

}

