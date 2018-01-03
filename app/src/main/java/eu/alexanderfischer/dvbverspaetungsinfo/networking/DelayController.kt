package eu.alexanderfischer.dvbverspaetungsinfo.networking

import android.util.Log
import eu.alexanderfischer.dvbverspaetungsinfo.helper.DvbEventBus
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import eu.alexanderfischer.dvbverspaetungsinfo.models.DvbError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object DelayController : Callback<List<Delay>> {

    private val TAG = DelayController::class.java.simpleName!!

    /**
     * Calls backend for delays async.
     */
    fun asyncDelays() {
        val delayCall = DelayApiService.create().delays()
        delayCall.enqueue(this)
    }

    override fun onResponse(call: Call<List<Delay>>?, response: Response<List<Delay>>?) {
        val success = response?.isSuccessful?: false
        if (success) {

            val delaysFromBackend = response!!.body()

            if (delaysFromBackend != null) {
                for (delay in delaysFromBackend) {
                    delay.save()
                }
            } else {
                DvbEventBus.broadcast(DvbError(null))
            }

        } else {
            Log.e(TAG, response?.errorBody()?.toString())
            DvbEventBus.broadcast(DvbError(null))
        }
    }

    override fun onFailure(call: Call<List<Delay>>?, t: Throwable?) {
        Log.e(TAG, t?.localizedMessage)
        DvbEventBus.broadcast(DvbError(null))
    }

}