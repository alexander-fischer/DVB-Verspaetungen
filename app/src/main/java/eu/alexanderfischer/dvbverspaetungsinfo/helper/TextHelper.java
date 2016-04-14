package eu.alexanderfischer.dvbverspaetungsinfo.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import eu.alexanderfischer.dvbverspaetungsinfo.models.DelayInformation;

/**
 * Created by Alexander Fischer.
 *
 * Helper class for doing text transformation stuff and adding information to DelayInformation object.
 */
public class TextHelper {

    public static String makeInfoText(Context context, DelayInformation delayInformation,
                                boolean isFilterActivated) {
        String infoText = "";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hasActivatedNotificationForLine = false;

        if (delayInformation.getLinien().size() > 0) {
            switch (delayInformation.getState()) {
                case "negativ":
                    infoText = "Störungsmeldung für die ";
                    break;
                case "positiv":
                    infoText = "Entwarnung für die ";
                    break;
                default:
                    infoText = "Hinweis für die ";
                    break;
            }

            ArrayList<String> linien = delayInformation.getLinien();
            ArrayList<String> filteredLinien = new ArrayList<>();

            if (isFilterActivated) {
                for (String linie : linien) {
                    hasActivatedNotificationForLine = sharedPref.getBoolean("linie" + linie, false);

                    if (hasActivatedNotificationForLine) {
                        filteredLinien.add(linie);
                    }
                }

                if (filteredLinien.size() > 0) {
                    linien = filteredLinien;
                }
            }

            if (linien.size() == 1) {
                String linie = linien.get(0);

                infoText = infoText + "Linie " + linie + ".";
            } else {
                StringBuilder linienText = new StringBuilder();

                infoText = infoText + "Linien ";
                for (int i = 0; i < linien.size(); i++) {

                    String linie = linien.get(i);

                    if (i == (linien.size() - 1)) {
                        linienText.append(linie);
                    } else {
                        linienText.append(linie).append(", ");
                    }

                }
                infoText = infoText + linienText.toString() + ".";
            }
        }

        return infoText;
    }

}
