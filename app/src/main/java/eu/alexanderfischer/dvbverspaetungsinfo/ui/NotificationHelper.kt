package eu.alexanderfischer.dvbverspaetungsinfo.ui

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import eu.alexanderfischer.dvbverspaetungsinfo.MainActivity
import eu.alexanderfischer.dvbverspaetungsinfo.R
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import java.util.*

class NotificationHelper(private val mContext: Context) : ContextWrapper(mContext) {

    private val NOTIFICATION_DELAY = "40"
    private val NOTIFICATION_CHANNEL_NAME = "Notification Channel"

    private val mNotificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun sendNotification(delay: Delay) {
        sendNotificationNew(delay)

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sendNotificationNew(delay)
        } else {
            sendNotificationOld(delay)
        }*/
    }

    private fun sendNotificationNew(delay: Delay) {
        val channelId = getChannel()
        for (line in delay.linien) {

            if (hasEnabledNotificationFor(line)) {
                val content = createContent(delay, line)
                val title = createTitle(delay)
                val intent = createPendingIntent()

                val builder = NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_NAME)
                        .setChannelId(channelId)
                        .setContentText(content)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentIntent(intent)

                val notification = builder.build()
                mNotificationManager.notify(getRandomId(), notification)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun sendNotificationOld(delay: Delay) {
        for (linie in delay.linien) {

            if (hasEnabledNotificationFor(linie)) {

                val mBuilder = NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Hinweis")
                        .setContentText("Für Linie $linie liegt ein neuer Hinweis vor.")
                        .setAutoCancel(true)


                val resultIntent = Intent(mContext, MainActivity::class.java)

                val stackBuilder = TaskStackBuilder.create(mContext)
                stackBuilder.addParentStack(MainActivity::class.java)
                stackBuilder.addNextIntent(resultIntent)

                val resultPendingIntent = stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                )

                mBuilder.setContentIntent(resultPendingIntent)

                val mNotificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

                mNotificationManager.notify(getRandomId(), mBuilder.build())
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun getChannel(): String {
        val channelId = NOTIFICATION_DELAY

        val channel = if (mNotificationManager.getNotificationChannel(channelId) != null) {
            mNotificationManager.getNotificationChannel(channelId)
        } else {
            NotificationChannel(channelId, NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH)
        }

        channel.enableLights(true)
        channel.lightColor = Color.YELLOW

        mNotificationManager.createNotificationChannel(channel)

        return channelId
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