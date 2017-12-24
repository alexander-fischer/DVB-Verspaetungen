package eu.alexanderfischer.dvbverspaetungsinfo.networking

import eu.alexanderfischer.dvbverspaetungsinfo.models.Delay
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface DelayApiService {
    companion object {
        private val URL = "https://alexfi.dubhe.uberspace.de"

        fun create(): DelayApiService {
            val timeout = 20L

            val client = OkHttpClient.Builder()
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .build()

            val retrofit = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(URL)
                    .client(client)
                    .build()

            return retrofit.create(DelayApiService::class.java)
        }
    }

    /**
     * Gets all the data from backend
     */
    @GET("text.json")
    fun delays(): Call<List<Delay>>
}
