package eu.alexanderfischer.dvbverspaetungsinfo.helpers;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import eu.alexanderfischer.dvbverspaetungsinfo.models.DelayInformation;

/**
 * Created by Alexander Fischer.
 *
 * Helper class for transforming JSON files.
 */
public class JsonHelper {

    public static ArrayList<DelayInformation> jsonArrayToObjectArray(Context context, JSONArray jsonArray, String lastId, boolean isFilterActivated) {
        ArrayList<DelayInformation> newTweetsArray = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);

                ArrayList<String> linien = new ArrayList<>();
                JSONArray jsonLinien = jsonObject.getJSONArray("linien");

                for (int j = 0; j < jsonLinien.length(); j++) {
                    linien.add(jsonLinien.get(j).toString());
                }

                DelayInformation delayInformation = new DelayInformation();
                delayInformation.setId(jsonObject.getString("id"));
                delayInformation.setDate(DateHelper.convertToGermanTimezone(jsonObject.getString("created_at")));
                delayInformation.setText(jsonObject.getString("text"));
                delayInformation.setState(jsonObject.getString("state"));
                delayInformation.setDayOfWeek(DateHelper.dateToDayOfWeek(delayInformation.getDate()));
                delayInformation.setLinien(linien);
                delayInformation.setInfoText(TextHelper.makeInfoText(context, delayInformation, isFilterActivated));

                if (delayInformation.getText().contains("&amp;")) {
                    delayInformation.setText(delayInformation.getText().replace("&amp;", "&"));
                }

                // Stops when the id is the same
                if (delayInformation.getId().equals(lastId)) {
                    break;
                }

                newTweetsArray.add(delayInformation);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newTweetsArray;
    }
}
