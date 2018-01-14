package eu.alexanderfischer.dvbverspaetungsinfo.networking

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
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
                    .addConverterFactory(getGsonConverter())
                    .baseUrl(URL)
                    .client(client)
                    .build()

            return retrofit.create(DelayApiService::class.java)
        }

        fun getGsonConverter(): GsonConverterFactory {
            val gson = GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
            return GsonConverterFactory.create(gson)
        }
    }

    /**
     * Gets all the data from backend
     */
    @GET("text.json")
    fun delays(): Call<List<Delay>>
}
