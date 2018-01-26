package eu.alexanderfischer.dvbverspaetungsinfo.helper

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Alexander Fischer.
 *
 *
 * Helper class for Date related functions.
 */
object DateHelper {

    /**
     * Gets the current day of week.
     *
     * @return current day of week.
     */
    val actualDayOfWeek: String
        get() {
            val date = Date()
            return SimpleDateFormat("EE", Locale.US).format(date)
        }

    /**
     * Gets current date.
     *
     * @return current date.
     */
    val currentTimeAndDate: Date
        get() {
            val calendar = Calendar.getInstance()
            return calendar.time
        }

    /**
     * Converts the date, that is given by Twitter to the date in the German timezone.
     *
     * @param dateString String representation of the date from Twitter.
     * @return String representation of the transformed date.
     */
    fun convertToGermanTimezone(dateString: String): String {
        val date: Date
        try {
            date = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH).parse(dateString)
        } catch (e: ParseException) {
            return ""
        }

        val dtfmt = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy")
        dtfmt.timeZone = TimeZone.getTimeZone("Europe/Berlin")

        return dtfmt.format(date)

    }

    /**
     * Gets the day of week out of a date.
     *
     * @param dateString represents the date.
     * @return the day of week of the date.
     */
    fun dateToDayOfWeek(dateString: String): String {
        var date = Date()
        try {
            date = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.GERMAN).parse(dateString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val dtfmt = SimpleDateFormat("EEE", Locale.ENGLISH)
        return dtfmt.format(date)
    }

}
