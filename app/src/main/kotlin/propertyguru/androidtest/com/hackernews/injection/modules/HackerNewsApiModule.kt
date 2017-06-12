package propertyguru.androidtest.com.hackernews.injection.modules

import com.google.gson.*
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by archie on 6/6/17.
 */

@Module
open class HackerNewsApiModule {


    val timeoutConnectionMinutes: Long = 1
    val timeoutReadMinutes: Long = 1


    @Singleton
    @Provides
    open fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
                .connectTimeout(timeoutConnectionMinutes, TimeUnit.MINUTES)
                .readTimeout(timeoutReadMinutes, TimeUnit.MINUTES)
                .build()
    }

    @Singleton
    @Provides
    open fun provideGson(): Gson {
        val builder = GsonBuilder()
        builder.registerTypeAdapter(Date::class.java, object : JsonDeserializer<Date> {
            @Throws(JsonParseException::class)
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Date {
                return Date(json.asJsonPrimitive.asLong * 1000)
            }
        })
        return builder.create()
    }
}