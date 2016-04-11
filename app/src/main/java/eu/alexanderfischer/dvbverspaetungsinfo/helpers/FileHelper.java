package eu.alexanderfischer.dvbverspaetungsinfo.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import eu.alexanderfischer.dvbverspaetungsinfo.models.DelayInformation;

/**
 * Created by Alexander Fischer.
 *
 * Helper class for file related functions.
 */
public class FileHelper {

    /**
     * Reads JSON File and converts it to a ArrayList.
     */
    public static ArrayList<DelayInformation> convertTweetJsonToArray(File file) {
        final ObjectMapper mapper = new ObjectMapper();
        ArrayList<DelayInformation> tweetsArray = new ArrayList<>();

        try {
            tweetsArray = mapper.readValue(file, new TypeReference<ArrayList<DelayInformation>>() {
            });
        } catch (IOException e) {
            // Do nothing here
        }

        return tweetsArray;
    }

}
