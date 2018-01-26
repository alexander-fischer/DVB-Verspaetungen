package eu.alexanderfischer.dvbverspaetungsinfo.ui

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import eu.alexanderfischer.dvbverspaetungsinfo.MainActivity
import eu.alexanderfischer.dvbverspaetungsinfo.R
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import java.util.*

class NotificationHelper(private val mContext: Context) : ContextWrapper(mContext) {

    private val mNotificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun sendNotification(delay: Delay) {
        for (line in delay.linien) {

            if (hasEnabledNotificationFor(line)) {
                val content = createContent(delay, line)
                val title = createTitle(delay)
                val intent = createPendingIntent()

                val builder = NotificationCompat.Builder(mContext)
                        .setContentText(content)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(intent)
                        .setAutoCancel(true)

                val notification = builder.build()
                mNotificationManager.notify(getRandomId(), notification)
            }
        }
    }

    private fun createContent(delay: Delay, line: String): String {
        return when (delay.state) {
            "negativ" -> "Für Linie $line liegt eine Störungsmeldung vor!"
            "positiv" -> "Für Linie $line liegt eine Entwarnungsmeldung vor."
            else -> "Für Linie $line liegt ein neuer Hinweis vor."
        }
    }

    private fun createTitle(delay: Delay): String {
        return when (delay.state) {
            "negativ" -> "Störung!"
            "positiv" -> "Entwarnung"
            else -> "Hinweis"
        }
    }

    private fun hasEnabledNotificationFor(line: String): Boolean {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext)
        return sharedPref.getBoolean("linie" + line, false)
    }

    private fun createPendingIntent(): PendingIntent {
        val resultIntent = Intent(mContext, MainActivity::class.java)

        val stackBuilder = TaskStackBuilder.create(mContext)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getRandomId(): Int {
        val rand = Random()
        return rand.nextInt(10000)
    }
}