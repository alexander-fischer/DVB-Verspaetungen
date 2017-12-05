package eu.alexanderfischer.dvbverspaetungsinfo.networking

import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface DelayApiService {
    companion object {
        private val URL = "https://alexfi.dubhe.uberspace.de"

        fun create(): DelayApiService {
            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(URL)
                    .build()

            return retrofit.create(DelayApiService::class.java)
        }
    }

    /**
     * Gets all the data from backend
     */
    @GET("text.json")
    fun getDelays(): Call<List<Delay>>
}
