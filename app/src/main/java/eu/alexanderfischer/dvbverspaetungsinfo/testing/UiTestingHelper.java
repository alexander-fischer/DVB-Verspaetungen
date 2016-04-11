package eu.alexanderfischer.dvbverspaetungsinfo.testing;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

import eu.alexanderfischer.dvbverspaetungsinfo.MainActivity;
import eu.alexanderfischer.dvbverspaetungsinfo.R;

/**
 * Created by Alexander Fischer.
 * Class for testing UI specific functionalities.
 */
public class UiTestingHelper {

    /**
     * Tests the Notifications.
     * @param context Activity context.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void testNotification(Context context) {
        Random rand = new Random();
        int mId = rand.nextInt(10000);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Hinweis")
                        .setContentText("Test")
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mId, mBuilder.build());
    }

}
