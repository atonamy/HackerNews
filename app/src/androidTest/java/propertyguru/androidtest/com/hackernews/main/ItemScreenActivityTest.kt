package propertyguru.androidtest.com.hackernews.main

import android.content.Intent
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.awaitility.Awaitility
import org.junit.Before

import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.injection.components.DaggerTestApiComponent
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModuleTest
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import propertyguru.androidtest.com.hackernews.ui.activities.ItemScreenActivity
import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by archie on 12/6/17.
 */

@RunWith(AndroidJUnit4::class)
class ItemScreenActivityTest {

    @get:Rule
    val rule: ActivityTestRule<ItemScreenActivity> = ActivityTestRule(ItemScreenActivity::class.java)

    lateinit var activity: ItemScreenActivity
    val formatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy/HH:mm:ss")

    val testStory = Story().apply {
        author = "raldi"
        url = "https://en.wikipedia.org/wiki/Area_code_710"
        title = "Area code 710"
        time = formatter.parse("11/06/2017/04:33:36")
        kids = listOf(14529448, 14531272)
        score = "273"
        id = 14529079
        descendants = 58
    }

    @Before
    fun setUp() {
        activity = rule.launchActivity(testStory.toIntent(Intent()))
    }

    @Test
    @Throws(Exception::class)
    fun testDisplayComments() {
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(commentsLoaded())
    }

    @Ignore
    private fun commentsLoaded(): Callable<Boolean> {
        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                val adapter = activity.viewModel.adapter
                return (adapter != null && adapter.itemCount > 0)
            }
        }
    }

}