package eu.alexanderfischer.dvbverspaetungsinfo

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import com.google.firebase.analytics.FirebaseAnalytics
import eu.alexanderfischer.dvbverspaetungsinfo.helper.TextHelper
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import eu.alexanderfischer.dvbverspaetungsinfo.models.DvbError
import eu.alexanderfischer.dvbverspaetungsinfo.networking.DelayController
import eu.alexanderfischer.dvbverspaetungsinfo.services.UpdateServiceManager
import eu.alexanderfischer.dvbverspaetungsinfo.ui.DelayAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Alexander Fischer.
 *
 *
 * This is the Main Activity of the app.
 */
class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val AMOUNT_DELAYS = 30

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private var mSwipeLayout: SwipeRefreshLayout? = null

    private var mDelays: ArrayList<Delay> = ArrayList()
    private var mDelayAdapter: DelayAdapter? = null

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

        DelayController.asyncDelays()
    }

    override fun onStart() {
        super.onStart()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().unregister(this)
    }

    // TODO Longpress share
    private fun setupData() {
        val loadedDelays = Delay.allDelays()
        mDelays = if (loadedDelays.size > AMOUNT_DELAYS) {
            ArrayList(loadedDelays.subList(0, AMOUNT_DELAYS))
        } else {
            ArrayList(loadedDelays)
        }

        val liveDelays = Delay.liveResults()
        liveDelays.observe(this, Observer {
            mSwipeLayout?.apply {
                if (isRefreshing) isRefreshing = false
            }

            liveDelays.value?.apply {
                var delays = Delay.getAllFromLiveResults(this)

                if (delays.size > AMOUNT_DELAYS) {
                    delays = delays.subList(0, AMOUNT_DELAYS)
                }

                refreshAdapter(ArrayList(delays))
            }
        })
    }

    private fun setupUi() {
        title = getString(R.string.app_name)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mSwipeLayout = findViewById(R.id.swipe_container)
        mSwipeLayout!!.setOnRefreshListener {
            DelayController.asyncDelays()
        }

        val listView = findViewById<ListView>(R.id.list)
        mDelayAdapter = DelayAdapter(this, mDelays)
        listView.adapter = mDelayAdapter

        // Long click sharing functionality.
        listView.isLongClickable = true
        listView.onItemLongClickListener = delayLongClickListener
    }

    private fun refreshAdapter(delays: ArrayList<Delay>) {
        setSubtitle()

        mDelays = delays
        mDelayAdapter?.refreshData(mDelays)
    }

    private fun setSubtitle() {
        val date = Date()
        val formatter = SimpleDateFormat("HH:mm", Locale.GERMAN)

        supportActionBar?.subtitle = "Letzte Aktualisierung um " + formatter.format(date)
    }

    private fun setErrorSubtitle(message: String) {
        supportActionBar?.subtitle = message
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

    private val delayLongClickListener = AdapterView.OnItemLongClickListener { _, _, pos, _ ->
        val delay = mDelays[pos]
        val infoText = TextHelper.makeInfoText(delay)

        val sendText = if (infoText == "") {
            delay.text + " #DVBVerspätungen"
        } else {
            infoText + " " + delay.text + " #DVBVerspätungen"
        }

        val sendIntent = Intent()

        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, sendText)
        sendIntent.type = "text/plain"

        startActivity(Intent.createChooser(sendIntent, "Information senden an:"))

        true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(error: DvbError) {
        mSwipeLayout?.isRefreshing = false

        if (error.message != null) {
            setErrorSubtitle(error.message)
        } else {
            setErrorSubtitle(getString(R.string.std_err_msg))
        }
    }
}

