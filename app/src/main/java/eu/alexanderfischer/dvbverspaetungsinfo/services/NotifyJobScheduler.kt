package eu.alexanderfischer.dvbverspaetungsinfo.services

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context


object NotifyJobScheduler {
    val TAG = NotifyJobScheduler::class.java.simpleName

    fun scheduleJob(context: Context) {
        // 5 minutes
        val interval = (1000 * 60 * 5).toLong()

        val serviceComponent = ComponentName(context, NotifyJobService::class.java)

        val builder = JobInfo.Builder(0, serviceComponent)
        builder.setMinimumLatency(interval)

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }

}