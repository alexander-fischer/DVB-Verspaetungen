package eu.alexanderfischer.dvbverspaetungsinfo.testing

import android.content.Context
import android.util.Log
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import eu.alexanderfischer.dvbverspaetungsinfo.ui.NotificationHelper
import io.realm.RealmList

/**
 * Created by Alexander Fischer.
 * Class for testing UI specific functionalities.
 */
class UiTestingHelper(context: Context) {
    private val TAG = UiTestingHelper::class.java.simpleName

    init {
        Log.e(TAG, "Test notifications. Not for production.")

        val helper = NotificationHelper(context)

        val negativeDelay = testNegativeDelay()
        helper.sendNotification(negativeDelay)

        val positiveDelay = testPositiveDelay()
        helper.sendNotification(positiveDelay)
    }

    private fun testNegativeDelay(): Delay {
        val delay = Delay()
        delay.state = "negativ"

        val lines = RealmList<String>()
        lines.add("4")

        delay.linien = lines

        return delay
    }

    private fun testPositiveDelay(): Delay {
        val delay = Delay()
        delay.state = "positiv"

        val lines = RealmList<String>()
        lines.add("6")

        delay.linien = lines

        return delay
    }

}
