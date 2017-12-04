package eu.alexanderfischer.dvbverspaetungsinfo.services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import eu.alexanderfischer.dvbverspaetungsinfo.MainActivity;
import eu.alexanderfischer.dvbverspaetungsinfo.R;
import eu.alexanderfischer.dvbverspaetungsinfo.helper.WebRequestUtils;
import eu.alexanderfischer.dvbverspaetungsinfo.models.DelayInformation;

public class UpdateTweetsService extends Service {

    private static final String TAG = UpdateTweetsService.class.getSimpleName();

    private static final String LASTID_FILE = "lastid.json";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // Do nothing here
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start webservice
        new JsonWebService().execute();

        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Do nothing here
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void sendNotification(DelayInformation tweet) {
        if (tweet.getLinien().size() > 0) {
            for (String linie : tweet.getLinien()) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                boolean hasActivatedNotifications = sharedPref.getBoolean("notifications", false);
                if (hasActivatedNotifications) {
                    boolean hasActivatedNotificationForLine = sharedPref.getBoolean("linie" + linie, false);

                    if (hasActivatedNotificationForLine) {
                        Random rand = new Random();
                        int mId = rand.nextInt(10000);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(this)
                                        .setSmallIcon(R.drawable.ic_notification)
                                        .setContentTitle("Hinweis")
                                        .setContentText("Für Linie " + linie + " liegt ein neuer Hinweis vor.")
                                        .setAutoCancel(true);

                        if (tweet.getState().equals("negativ")) {
                            mBuilder.setContentTitle("Störung!")
                                    .setContentText("Für Linie " + linie + " liegt eine Störungsmeldung vor!");
                        } else if (tweet.getState().equals("positiv")) {
                            mBuilder.setContentTitle("Entwarnung")
                                    .setContentText("Für Linie " + linie + " liegt eine Entwarnungsmeldung vor.");
                        }


                        Intent resultIntent = new Intent(this, MainActivity.class);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);

                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );

                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        mNotificationManager.notify(mId, mBuilder.build());
                    }
                }
            }
        }
    }

    /**
     * Gets the last set id from a File.
     *
     * @return the last id
     */
    private String getLastId() {
        ObjectMapper mapper = new ObjectMapper();
        String lastId = "";
        final File lastIdFile = new File(getFilesDir(), LASTID_FILE);

        try {
            lastId = mapper.readValue(lastIdFile, String.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastId;
    }

    class JsonWebService extends AsyncTask<Void, Void, Void> {
        ArrayList<DelayInformation> tweetsArray;

        @Override
        protected Void doInBackground(Void... params) {
            String lastIdFromBackend = WebRequestUtils
                    .getStringFromWebRequest("http://alexfi.dubhe.uberspace.de/id.txt");
            if (lastIdFromBackend != null) {
                if (lastIdFromBackend.contains("\n")) {
                    lastIdFromBackend = lastIdFromBackend.replace("\n", "");
                }

                String lastId = getLastId();

                if (!lastId.equals(lastIdFromBackend)) {
                    String dataFromBackend = WebRequestUtils
                            .getStringFromWebRequest("http://alexfi.dubhe.uberspace.de/text.json");

                    JSONArray jsonArray;
                    try {
                        jsonArray = new JSONArray(dataFromBackend);
                        tweetsArray = jsonArrayToObjectArray(jsonArray, lastId);
                    } catch (JSONException e) {
                        Log.d(TAG, "JSONException");
                    } catch (NullPointerException e) {
                        Log.d(TAG, "NullPointerException");
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (tweetsArray != null) {
                if (tweetsArray.size() > 0) {
                    for (DelayInformation tweet : tweetsArray) {
                        sendNotification(tweet);
                    }

                    final File lastIdFile = new File(getFilesDir(), LASTID_FILE);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        mapper.writeValue(lastIdFile, tweetsArray.get(0).getId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    protected ArrayList<DelayInformation> jsonArrayToObjectArray(JSONArray jsonArray, String lastId) {
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
                delayInformation.setDate(jsonObject.getString("created_at"));
                delayInformation.setText(jsonObject.getString("text"));
                delayInformation.setState(jsonObject.getString("state"));
                delayInformation.setLinien(linien);

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

