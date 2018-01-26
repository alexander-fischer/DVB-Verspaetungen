package eu.alexanderfischer.dvbverspaetungsinfo.helper

import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alexander Fischer.
 *
 * Helper class for doing text transformation stuff and adding information to DelayInformation object.
 */
object TextHelper {

    fun makeInfoText(delay: Delay): String {
        var infoText = ""

        if (delay.linien.size > 0) {
            infoText = when (delay.state) {
                "negativ" -> "Störungsmeldung für die "
                "positiv" -> "Entwarnung für die "
                else -> "Hinweis für die "
            }

            val linien = delay.linien

            if (linien.size == 1) {
                val linie = linien[0]

                infoText = infoText + "Linie " + linie + "."
            } else {
                val linienText = StringBuilder()

                infoText += "Linien "
                for (i in linien.indices) {

                    val linie = linien[i]

                    if (i == linien.size - 1) {
                        linienText.append(linie)
                    } else {
                        linienText.append(linie).append(", ")
                    }

                }
                infoText = infoText + linienText.toString() + "."
            }
        }

        return infoText
    }

    fun makeDayText() {
        val currentDate = DateHelper.currentTimeAndDate
        val currentDayOfWeek = SimpleDateFormat("EE", Locale.ENGLISH).format(currentDate.time)
    }

}
