package eu.alexanderfischer.dvbverspaetungsinfo

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.analytics.FirebaseAnalytics
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import eu.alexanderfischer.dvbverspaetungsinfo.networking.DelayController
import eu.alexanderfischer.dvbverspaetungsinfo.services.UpdateServiceManager

/**
 * Created by Alexander Fischer.
 *
 *
 * This is the Main Activity of the app.
 */
class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var mSwipeLayout: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        UpdateServiceManager.startUpdateService(this)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        startTutorial()

        setupData()
        setupUi()
    }

    override fun onResume() {
        super.onResume()
        DelayController.getDelays()
    }

    private fun setupData() {
        val liveDelays = Delay.liveResults()
        liveDelays.observe(this, Observer {
            Log.d(TAG, "New size = " + liveDelays.value?.size)
            mSwipeLayout?.apply {
                isRefreshing = false
            }
        })

    }

    private fun setupUi() {
        title = getString(R.string.app_name)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.subtitle = "HALLO"


        mSwipeLayout = findViewById(R.id.swipe_container)
        mSwipeLayout!!.setOnRefreshListener {
            DelayController.getDelays()
        }
    }

    /**
     * Start tutorial if app is started the first time.
     */
    private fun startTutorial() {
        val hasConfiguredSettings = hasConfiguredSettings()
        val bundle = Bundle()

        if (!hasConfiguredSettings) {
            bundle.putString(FirebaseAnalytics.Param.CONTENT, "App opened first time")
            mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)

            startConfiguringSettings()
        } else {
            bundle.putString(FirebaseAnalytics.Param.CONTENT, "App opened")
            mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.filter) {
            //filterTweets();
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Start the onboarding process.
     */
    private fun startConfiguringSettings() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putBoolean("hasConfiguredSettings", true)
        editor.apply()

        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("HAS_CONFIGURED_SETTINGS", true)
        startActivity(intent)
    }


    /**
     * Checks if the user is already "onboarded".
     *
     * @return true if the user has already configured the settings and false if not.
     */
    private fun hasConfiguredSettings(): Boolean {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)

        return sharedPref.getBoolean("hasConfiguredSettings", false)
    }
}

