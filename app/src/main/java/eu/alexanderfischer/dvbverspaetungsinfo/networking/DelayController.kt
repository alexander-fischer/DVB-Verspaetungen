package eu.alexanderfischer.dvbverspaetungsinfo.networking

import android.util.Log
import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object DelayController : Callback<List<Delay>> {

    private val TAG = DelayController::class.java.simpleName!!

    fun callBackend() {
        val delayCall = DelayApiService.create().getDelays()
        delayCall.enqueue(this)
    }

    override fun onResponse(call: Call<List<Delay>>?, response: Response<List<Delay>>?) {
        val success = response?.isSuccessful?: false
        if (success) {

            val delaysFromBackend = response!!.body()
            val del = delaysFromBackend?.get(0)
            Log.d(TAG, del.toString())
        } else {

            Log.e(TAG, response?.errorBody()?.toString())
        }
    }

    override fun onFailure(call: Call<List<Delay>>?, t: Throwable?) {
        Log.e(TAG, t?.localizedMessage)
    }

}