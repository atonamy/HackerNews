package propertyguru.androidtest.com.hackernews.main

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.junit.Before

import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import propertyguru.androidtest.com.hackernews.injection.components.DaggerTestApiComponent
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModuleTest
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import propertyguru.androidtest.com.hackernews.ui.activities.HomeScreenActivity
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by archie on 12/6/17.
 */

@RunWith(AndroidJUnit4::class)
class HomeScreenActivityTest {

    @get:Rule
    val rule: ActivityTestRule<HomeScreenActivity> = ActivityTestRule(HomeScreenActivity::class.java)

    lateinit var activity: HomeScreenActivity

    @Before
    fun setUp() {
        activity = rule.activity
    }


    @Test
    @Throws(Exception::class)
    fun testDisplayStories() {
        await().atMost(30, TimeUnit.SECONDS).until(storiesLoaded())
    }

    @Ignore
    private fun storiesLoaded(): Callable<Boolean> {
        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                val adapter = activity.viewModel.adapter
                return (adapter != null && adapter.itemCount > 0)
            }
        }
    }

}