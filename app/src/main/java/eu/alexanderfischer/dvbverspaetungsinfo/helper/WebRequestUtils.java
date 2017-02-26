package eu.alexanderfischer.dvbverspaetungsinfo.helper;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebRequestUtils {

    private final static String TAG = WebRequestUtils.class.getSimpleName();

    public static String getStringFromWebRequest(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            return null;
        }
    }

}
