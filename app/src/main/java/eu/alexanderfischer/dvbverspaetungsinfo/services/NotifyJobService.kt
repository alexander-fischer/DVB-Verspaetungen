package eu.alexanderfischer.dvbverspaetungsinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.preference.PreferenceManager
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import eu.alexanderfischer.dvbverspaetungsinfo.networking.DelayController
import eu.alexanderfischer.dvbverspaetungsinfo.ui.NotificationHelper
import org.jetbrains.anko.doAsync


class NotifyJobService : JobService() {
    val TAG = NotifyJobService::class.java.simpleName

    override fun onStartJob(params: JobParameters): Boolean {
        startNotifyJob(params)

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    private fun startNotifyJob(params: JobParameters) {

        doAsync {
            val delaysFromDb = Delay.allDelays()
            val latestIdFromDb = delaysFromDb[0].id.toLong()

            val res = DelayController.syncDelays()

            if (res.isSuccessful) {
                res.body()?.apply {
                    this.filter {
                        it.id.toLong() > latestIdFromDb
                    }.forEach {
                        it.save()
                        sendNotification(it)
                    }
                }

            }
        }

        scheduleRefresh()
        jobFinished(params, false)
    }

    private fun sendNotification(delay: Delay) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val hasActivatedNotifications = sharedPref.getBoolean("notifications", false)

        if (hasActivatedNotifications && delay.linien.size > 0) {
            val helper = NotificationHelper(this)
            helper.sendNotification(delay)
        }
    }

    private fun scheduleRefresh() {
        NotifyJobScheduler.scheduleJob(applicationContext)
    }

}