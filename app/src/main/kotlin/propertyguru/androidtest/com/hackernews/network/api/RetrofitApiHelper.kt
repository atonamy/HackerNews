package propertyguru.androidtest.com.hackernews.network.api

import com.google.gson.Gson
import okhttp3.OkHttpClient
import propertyguru.androidtest.com.hackernews.App
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

/**
 * Created by archie on 6/6/17.
 */
abstract class RetrofitApiHelper<out T> {

    @Inject
    lateinit var httpClient: OkHttpClient

    @Inject
    lateinit var jsonParser: Gson

    protected open fun createRetrofit(baseApiUrl: String): Retrofit
    {
        return Retrofit.Builder()
                .baseUrl(baseApiUrl)
                .addConverterFactory(GsonConverterFactory.create(jsonParser))
                .client(httpClient)
                .build()
    }

    abstract fun create(): T

}