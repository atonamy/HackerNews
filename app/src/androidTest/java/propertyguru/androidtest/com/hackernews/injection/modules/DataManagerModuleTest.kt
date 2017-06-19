package propertyguru.androidtest.com.hackernews.injection.modules

import com.nhaarman.mockito_kotlin.mock
import propertyguru.androidtest.com.hackernews.injection.components.DaggerDataManagerModuleTestComponent
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import android.support.test.InstrumentationRegistry
import android.util.Log
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

    companion object {

        var counter = 0

        private val kidsBuffer: Queue<Long> by lazy {
            val result = LinkedList<Long>()
            for(i in 999999999L..1000099999L)
                result.add(i)
            Collections.shuffle(result)
            result
        }

        private fun generateKids(newQuantity: Int): List<Long> {
            val result: MutableList<Long> = ArrayList<Long>()

            (1..newQuantity).forEach {
                result.add(kidsBuffer.remove())
            }

            return result
        }

        private val random: Random = Random()

        val currentStories: List<Long> by lazy {
            generateKids(random.nextInt(36)+1)
        }

    }


    override fun provideHackerNewsApi(): HackerNewsApi{
        return mock<HackerNewsApi> {

            on { topStories } doAnswer {
                counter++
                Log.w(DataManagerModuleTest::class.java.simpleName, "execution count: $counter")
                if(counter == 1)
                    MockCall<List<Long>>(ArrayList<Long>(), 502)
                else if(counter == 2)
                    MockCall<List<Long>>(ArrayList<Long>())
                else if(counter == 3 || counter == 4)
                    MockCall<List<Long>>(currentStories)
                else
                    MockCall<List<Long>>(listOf(14530552))
            }

            currentStories.forEach {
                val comments = generateKids(random.nextInt(10)+1)
                on { getStoryItem(it) } doReturn MockCall<Story>(setStoryKids(it,
                        getJsonObjectFromFile<Story>("stories/14529079.json") ?: Story(), comments))
                comments.forEach {
                    val subComments = generateKids(1)
                    on { getCommentItem(it) } doReturn MockCall<Comment>(setCommentKids(it,
                            getJsonObjectFromFile<Comment>("comments/14529448.json") ?: Comment(),
                            subComments))
                    subComments.forEach {
                        val lastLevelComments = generateKids(1)
                        on { getCommentItem(it) } doReturn MockCall<Comment>(setCommentKids(it,
                                getJsonObjectFromFile<Comment>("comments/14531272.json") ?: Comment(),
                                lastLevelComments))
                        lastLevelComments.forEach {
                            on { getCommentItem(it) } doReturn MockCall<Comment>(setCommentKids(it,
                                    getJsonObjectFromFile<Comment>("comments/14531275.json") ?: Comment(),
                                    ArrayList<Long>()))
                        }
                    }
                }
            }

            on { getStoryItem(14530552) } doReturn MockCall<Story>(Story(), 500)
            on { getCommentItem(14531277) } doReturn MockCall<Comment>(Comment(), 404)
        }
    }


    fun setStoryKids(id: Long, story: Story, kids: List<Long>): Story {
        story.id = id
        story.kids = kids
        return story
    }

    fun setCommentKids(id: Long, comment: Comment, kids: List<Long>): Comment {
        comment.id = id
        comment.kids = kids
        return comment
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

    fun convertStreamToString(`is`: InputStream): String {
        val s = Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}
