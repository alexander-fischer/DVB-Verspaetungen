package eu.alexanderfischer.dvbverspaetungsinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import eu.alexanderfischer.dvbverspaetungsinfo.helpers.FileHelper;
import eu.alexanderfischer.dvbverspaetungsinfo.helpers.JsonHelper;
import eu.alexanderfischer.dvbverspaetungsinfo.models.DelayInformation;
import eu.alexanderfischer.dvbverspaetungsinfo.services.UpdateServiceManager;
import eu.alexanderfischer.dvbverspaetungsinfo.ui.DelayInformationAdapter;

/**
 * Created by Alexander Fischer.
 * <p/>
 * This is the Main Activity of the app.
 */
public class MainActivity extends AppCompatActivity {

    // File names for locally saved data.
    private static final String TWEETS_JSON = "tweets.json";
    private static final String LASTID_JSON = "lastid.json";

    TextView subTitle;
    ListView listView;
    SwipeRefreshLayout swipeLayout;
    Toolbar toolbar;

    boolean isFilterActivated = false;

    ArrayList<DelayInformation> tweetsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTutorial();

        setupUI();
        setupData();

        startWebService();
        UpdateServiceManager.startUpdateService(this);
    }

    /**
     * Start tutorial if app is started the first time.
     */
    private void startTutorial() {
        boolean hasConfiguredSettings = hasConfiguredSettings();
        if (!hasConfiguredSettings) {
            startConfiguringSettings();
        }
    }

    /**
     * Executes the WebService for getting the raw Twitter feed data.
     */
    private void startWebService() {
        new GetTweetsWebService().execute();
    }

    /**
     * Setting up the UI for MainActivity.
     */
    private void setupUI() {
        // Adds toolbar for AppCombat support.
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Defines the View elements.
        listView = (ListView) findViewById(R.id.list);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        subTitle = (TextView) findViewById(R.id.subTitle);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(new View(this));

        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.primary), getResources().getColor(R.color.textColor));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetTweetsWebService().execute();
            }
        });

        // Long click sharing functionality.
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final DelayInformation delayInformation = tweetsArray.get(position);
                String sendText = "";

                if (delayInformation.getInfoText().equals("")) {
                    sendText = delayInformation.getText() + " #DVBVerspätungen";
                } else {
                    sendText = delayInformation.getInfoText() + " " + delayInformation.getText() + " #DVBVerspätungen";
                }

                Intent sendIntent = new Intent();

                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, sendText);
                sendIntent.setType("text/plain");

                startActivity(Intent.createChooser(sendIntent, "Information senden an:"));

                return true;
            }
        });
    }

    /**
     * Sets up the data that is saved locally.
     */
    private void setupData() {
        subTitle.setText("Lädt neue Störungsmeldungen...");

        final File file = new File(getFilesDir(), TWEETS_JSON);
        tweetsArray = FileHelper.convertTweetJsonToArray(file);
        if (tweetsArray.size() != 0) {
            final DelayInformationAdapter preAdapter = new DelayInformationAdapter(MainActivity.this,
                    R.layout.list_layout, R.id.list_layout_textview, tweetsArray, isFilterActivated);
            listView.setAdapter(preAdapter);
        }
    }

    /**
     * Web service for getting the tweets from an REST API.
     */
    class GetTweetsWebService extends AsyncTask<Void, Void, Void> {
        ArrayList<DelayInformation> tweets = new ArrayList<>();
        ArrayList<DelayInformation> newTweets;

        boolean hasFailed = false;
        String lastId = "0";

        @Override
        protected Void doInBackground(Void... params) {
            String lastIdFromBackend = getLastId();

            if (lastIdFromBackend.contains("\n")) {
                lastIdFromBackend = lastIdFromBackend.replace("\n", "");
            }

            final File file = new File(getFilesDir(), TWEETS_JSON);
            ArrayList<DelayInformation> tweetsArrayOld = FileHelper.convertTweetJsonToArray(file);

            if (tweetsArrayOld.size() > 0) {
                DelayInformation firstTweet = tweetsArrayOld.get(0);
                lastId = firstTweet.getId();
            }

            if (!lastId.equals(lastIdFromBackend)) {
                newTweets = getDataFromBackend(lastId);

                if (newTweets.size() > 0) {
                    for (DelayInformation tweet : newTweets) {
                        tweets.add(tweet);
                    }

                    final File lastIdFile = new File(getFilesDir(), LASTID_JSON);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        mapper.writeValue(lastIdFile, newTweets.get(0).getId());
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Fehler beim Schreiben von Datei.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                if (tweets.size() <= 15) {
                    int maxSize = 15 - tweets.size();

                    if (tweets.size() + tweetsArrayOld.size() >= 15) {
                        for (int i = 0; i < maxSize; i++) {
                            DelayInformation tweet = tweetsArrayOld.get(i);
                            tweets.add(tweet);
                        }
                    } else {
                        for (DelayInformation tweet : tweetsArrayOld) {
                            tweets.add(tweet);
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (hasFailed) {
                Toast.makeText(MainActivity.this,
                        "Fehler beim Aktualisieren. Überprüfe deine Internetverbindung.",
                        Toast.LENGTH_LONG).show();

                swipeLayout.setRefreshing(false);

                subTitle.setText("Fehler beim Aktualisieren.");
            } else {
                if (tweets.size() > 0) {

                    tweetsArray = tweets;

                    final DelayInformationAdapter adapter = new DelayInformationAdapter(MainActivity.this,
                            R.layout.list_layout, R.id.list_layout_textview, tweets, isFilterActivated);
                    listView.setAdapter(adapter);

                    ObjectMapper mapper = new ObjectMapper();
                    final File file = new File(getFilesDir(), TWEETS_JSON);
                    try {
                        mapper.writeValue(file, tweets);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), "Fehler beim Schreiben von Datei.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.GERMAN);
                subTitle.setText("Letzte Aktualisierung um " + formatter.format(date));
            }

            swipeLayout.setRefreshing(false);
        }

        /**
         * Gets the data from the backend and converts it to an array list.
         *
         * @param lastId Last id of the newest DelayInformation, that is saved locally.
         * @return list of all DelayInformation objects.
         */
        protected ArrayList<DelayInformation> getDataFromBackend(String lastId) {
            HttpGet httpGet = new HttpGet("http://alexfi.dubhe.uberspace.de/text.json");
            InputStream inputStream = null;
            ArrayList<DelayInformation> newTweetsArray = new ArrayList<>();
            String jsonString = "";

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                jsonString = sb.toString();
                JSONArray jsonArray = new JSONArray(jsonString);

                newTweetsArray = JsonHelper.jsonArrayToObjectArray(MainActivity.this, jsonArray, lastId, isFilterActivated);

            } catch (JSONException | IOException e) {
                hasFailed = true;
            }

            return newTweetsArray;
        }

        /**
         * Get the last id from the backend.
         * @return last id.
         */
        protected String getLastId() {
            HttpGet httpGet = new HttpGet("http://alexfi.dubhe.uberspace.de/id.txt");
            InputStream inputStream = null;
            String lastIdFromBackend = "";

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                lastIdFromBackend = sb.toString();
            } catch (IOException e) {
                hasFailed = true;
            }

            return lastIdFromBackend;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.filter) {
            filterTweets();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Start the onboarding process.
     */
    private void startConfiguringSettings() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("hasConfiguredSettings", true);
        editor.apply();

        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("HAS_CONFIGURED_SETTINGS", true);
        startActivity(intent);
    }


    /**
     * Checks if the user is already "onboarded".
     *
     * @return true if the user has already configured the settings and false if not.
     */
    private boolean hasConfiguredSettings() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean hasConfiguredSettings = sharedPref.getBoolean("hasConfiguredSettings", false);

        return hasConfiguredSettings;
    }

    /**
     * Filters the tweets after activating the filter button.
     */
    private void filterTweets() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        final File file = new File(getFilesDir(), TWEETS_JSON);
        ArrayList<DelayInformation> tweetsArray = FileHelper.convertTweetJsonToArray(file);
        ArrayList<DelayInformation> filteredTweets = new ArrayList<>();

        ActionMenuItemView item = (ActionMenuItemView) findViewById(R.id.filter);

        if (!isFilterActivated) {
            for (DelayInformation delayInformation : tweetsArray) {
                boolean hasActivatedNotificationForLine = false;

                ArrayList<String> linien = delayInformation.getLinien();
                for (String linie : linien) {
                    hasActivatedNotificationForLine = sharedPref.getBoolean("linie" + linie, false);
                    if (hasActivatedNotificationForLine) {
                        break;
                    }
                }
                if (hasActivatedNotificationForLine) {
                    filteredTweets.add(delayInformation);
                }
            }

            if (filteredTweets.size() != 0) {
                final DelayInformationAdapter preAdapter = new DelayInformationAdapter(MainActivity.this,
                        R.layout.list_layout, R.id.list_layout_textview, filteredTweets, isFilterActivated);
                listView.setAdapter(preAdapter);

                item.setIcon(getResources().getDrawable(R.drawable.ic_filter_empy));
            } else {
                subTitle.setText("Keine Informationen über deine Linien.");
            }

            isFilterActivated = true;
        } else {
            if (tweetsArray.size() != 0) {
                final DelayInformationAdapter preAdapter = new DelayInformationAdapter(MainActivity.this,
                        R.layout.list_layout, R.id.list_layout_textview, tweetsArray, isFilterActivated);
                listView.setAdapter(preAdapter);
            }

            item.setIcon(getResources().getDrawable(R.drawable.ic_filter));

            isFilterActivated = false;
        }
    }
}

