package eu.alexanderfischer.dvbverspaetungsinfo.helper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Alexander Fischer.
 * <p/>
 * Helper class for Date related functions.
 */
public class DateHelper {

    /**
     * Gets the current day of week.
     *
     * @return current day of week.
     */
    public static String getActualDayOfWeek() {
        Date date = new Date();
        String actualDayOfWeek = new SimpleDateFormat("EE", Locale.US).format(date);

        return actualDayOfWeek;
    }

    /**
     * Converts the date, that is given by Twitter to the date in the German timezone.
     *
     * @param dateString String representation of the date from Twitter.
     * @return String representation of the transformed date.
     */
    public static String convertToGermanTimezone(String dateString) {
        Date date;
        try {
            date = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH).parse(dateString);
        } catch (ParseException e) {
            return "";
        }

        DateFormat dtfmt = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
        dtfmt.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        String newDate = dtfmt.format(date);

        return newDate;

    }

    /**
     * Gets the day of week out of a date.
     *
     * @param dateString represents the date.
     * @return the day of week of the date.
     */
    public static String dateToDayOfWeek(String dateString) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.GERMAN).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DateFormat dtfmt = new SimpleDateFormat("EEE", Locale.ENGLISH);
        String dayOfWeek = dtfmt.format(date);
        return dayOfWeek;
    }

    /**
     * Gets current date.
     *
     * @return current date.
     */
    public static Date getCurrentTimeAndDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();

        return date;
    }

}
