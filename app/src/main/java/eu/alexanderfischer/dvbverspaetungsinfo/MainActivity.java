package eu.alexanderfischer.dvbverspaetungsinfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import eu.alexanderfischer.dvbverspaetungsinfo.helper.FileHelper;
import eu.alexanderfischer.dvbverspaetungsinfo.helper.JsonHelper;
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

    private FirebaseAnalytics mFirebaseAnalytics;

    boolean isFilterActivated = false;

    // Stores the array of DelayInformation for the Activity.
    ArrayList<DelayInformation> tweetsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        startTutorial();

        setupUI();
        setupData();

        UpdateServiceManager.startUpdateService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startWebServices();
    }

    /**
     * Start tutorial if app is started the first time.
     */
    private void startTutorial() {
        boolean hasConfiguredSettings = hasConfiguredSettings();
        Bundle bundle = new Bundle();

        if (!hasConfiguredSettings) {
            bundle.putString(FirebaseAnalytics.Param.CONTENT, "App opened first time");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

            startConfiguringSettings();
        } else {
            bundle.putString(FirebaseAnalytics.Param.CONTENT, "App opened");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        }
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
                startWebServices();
            }
        });

        // Long click sharing functionality.
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final DelayInformation delayInformation = tweetsArray.get(position);
                String sendText;

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
     * Static Object to store the last id for passing between the two WebServices
     */
    static class IdObject {
        String lastId;
    }

    /**
     * Executes the WebService for getting the last id.
     */
    private void startWebServices() {
        final RequestQueue queue = Volley.newRequestQueue(this);

        final String idUrl = getString(R.string.lastid_url);
        final String dataUrl = getString(R.string.di_url);

        final File file = new File(getFilesDir(), TWEETS_JSON);
        final ArrayList<DelayInformation> localSavedDelayInformation = FileHelper.convertTweetJsonToArray(file);

        final IdObject idObject = new IdObject();

        final StringRequest getDataFromBackend = new StringRequest(Request.Method.GET, dataUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String encodedResponse = null;
                        try {
                            encodedResponse = new String(response.getBytes("ISO-8859-1"), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            return;
                        }
                        ArrayList<DelayInformation> backendDelayInformation;

                        JSONArray jsonArray;
                        try {
                            jsonArray = new JSONArray(encodedResponse);
                        } catch (JSONException e) {
                            return;
                        }

                        backendDelayInformation = JsonHelper.jsonArrayToObjectArray(MainActivity.this, jsonArray,
                                idObject.lastId, isFilterActivated);

                        if (backendDelayInformation.size() > 0) {

                            final File lastIdFile = new File(getFilesDir(), LASTID_JSON);
                            ObjectMapper mapper = new ObjectMapper();
                            try {
                                mapper.writeValue(lastIdFile, backendDelayInformation.get(0).getId());
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "Fehler beim Schreiben von Datei.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        if (backendDelayInformation.size() <= 15) {
                            int maxSize = 15 - backendDelayInformation.size();

                            if (backendDelayInformation.size() + localSavedDelayInformation.size() >= 15) {
                                for (int i = 0; i < maxSize; i++) {
                                    DelayInformation tweet = localSavedDelayInformation.get(i);
                                    backendDelayInformation.add(tweet);
                                }
                            } else {
                                for (DelayInformation tweet : localSavedDelayInformation) {
                                    backendDelayInformation.add(tweet);
                                }
                            }
                        } else {
                            while (backendDelayInformation.size() > 15) {
                                backendDelayInformation.remove(15);
                            }

                        }

                        if (backendDelayInformation.size() > 0) {

                            tweetsArray = backendDelayInformation;

                            final DelayInformationAdapter adapter = new DelayInformationAdapter(MainActivity.this,
                                    R.layout.list_layout, R.id.list_layout_textview, backendDelayInformation, isFilterActivated);
                            listView.setAdapter(adapter);

                            ObjectMapper mapper = new ObjectMapper();
                            final File file = new File(getFilesDir(), TWEETS_JSON);
                            try {
                                mapper.writeValue(file, backendDelayInformation);
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "Fehler beim Schreiben von Datei.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        Date date = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.GERMAN);
                        subTitle.setText("Letzte Aktualisierung um " + formatter.format(date));


                        swipeLayout.setRefreshing(false);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Keine Internetverbindung", Toast.LENGTH_LONG).show();
            }
        });

        // Start WebService to get the latest Id from backend.
        StringRequest getIdFromBackend = new StringRequest(Request.Method.GET, idUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String lastIdFromBackend = response;
                        idObject.lastId = "";

                        if (lastIdFromBackend.contains("\n")) {
                            lastIdFromBackend = lastIdFromBackend.replace("\n", "");
                        }

                        if (localSavedDelayInformation.size() > 0) {
                            DelayInformation firstTweet = localSavedDelayInformation.get(0);
                            idObject.lastId = firstTweet.getId();
                        }

                        if (idObject.lastId.equals(lastIdFromBackend)) {
                            Date date = new Date();
                            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.GERMAN);
                            subTitle.setText("Letzte Aktualisierung um " + formatter.format(date));

                            swipeLayout.setRefreshing(false);
                        } else {
                            swipeLayout.setRefreshing(false);

                            queue.add(getDataFromBackend);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Keine Internetverbindung", Toast.LENGTH_LONG).show();
            }
        });

        queue.add(getIdFromBackend);
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

