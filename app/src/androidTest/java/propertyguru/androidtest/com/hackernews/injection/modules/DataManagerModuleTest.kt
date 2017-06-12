package propertyguru.androidtest.com.hackernews.injection.modules

import com.nhaarman.mockito_kotlin.mock
import propertyguru.androidtest.com.hackernews.injection.components.DaggerDataManagerModuleTestComponent
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import android.support.test.InstrumentationRegistry
import com.google.gson.Gson
import com.github.salomonbrys.kotson.*
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import propertyguru.androidtest.com.hackernews.data.model.Comment
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.utils.MockCall
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by archie on 11/6/17.
 */

class DataManagerModuleTest: DataManagerModule() {

    @Inject
    lateinit var jsonParser: Gson

    init {
        DaggerDataManagerModuleTestComponent.builder().hackerNewsApiModule(HackerNewsApiModule())
                .build().inject(this)
    }

    override fun provideHackerNewsApi(): HackerNewsApi{
        var count = 0
        return mock<HackerNewsApi> {
            on { topStories } doAnswer {
                count++
                if(count == 1)
                    MockCall<List<Long>>(getJsonObjectFromFile<List<Long>>("topstrories.json") ?: ArrayList<Long>())
                else
                    MockCall<List<Long>>(ArrayList<Long>())
            }
            on { getStoryItem(14529079) } doReturn MockCall<Story>(getJsonObjectFromFile<Story>("stories/14529079.json") ?: Story())
            on { getStoryItem(14529376) } doReturn MockCall<Story>(getJsonObjectFromFile<Story>("stories/14529376.json") ?: Story())
            on { getStoryItem(14530551) } doReturn MockCall<Story>(getJsonObjectFromFile<Story>("stories/14530551.json") ?: Story())
            on { getCommentItem(14531277) } doReturn MockCall<Comment>(Comment(), 500)
            on { getCommentItem(14529448) } doReturn MockCall<Comment>(getJsonObjectFromFile<Comment>("comments/14529448.json") ?: Comment())
            on { getCommentItem(14531272) } doReturn MockCall<Comment>(getJsonObjectFromFile<Comment>("comments/14531272.json") ?: Comment())
            on { getCommentItem(14531275) } doReturn MockCall<Comment>(getJsonObjectFromFile<Comment>("comments/14531275.json") ?: Comment())
        }
    }


    fun simulateStoriesQuantity(stories: List<Long>, newQuantity: Int): List<Long> {
        val result: MutableList<Long> = ArrayList<Long>()

        (1..newQuantity).forEach {
            stories.forEach {
                result.add(it)
            }
        }

        return result
    }

    inline fun <reified T> getJsonObjectFromFile(jsonFileName: String): T? where T: Any {
        val inputStream = InstrumentationRegistry.getContext().assets.open(jsonFileName)
        val jsonString = convertStreamToString(inputStream)
        var jsonObject: T? = null

        try {
            jsonObject = jsonParser.fromJson<T>(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return jsonObject
    }

    inline fun convertStreamToString(`is`: InputStream): String {
        val s = Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}
