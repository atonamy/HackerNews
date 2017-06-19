package propertyguru.androidtest.com.hackernews.main

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.awaitility.Awaitility.await
import org.junit.Before

import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
    val rule: ActivityTestRule<HomeScreenActivity> = object: ActivityTestRule<HomeScreenActivity>(HomeScreenActivity::class.java) {
        override fun beforeActivityLaunched() {
            HomeScreenActivity.suspendStart = true
            DataManagerModuleTest.counter = 3
        }
    }


    @Inject
    lateinit var api: HackerNewsApi
    lateinit var activity: HomeScreenActivity

    @Before
    fun setUp() {
        DaggerTestApiComponent.builder().dataManagerModule(DataManagerModuleTest()).build().inject(this)
        activity = rule.activity
        activity.storiesHelper.dataManager.hackerNewsApi = api
    }


    @Test
    @Throws(Exception::class)
    fun testDisplayStories() {
        activity.runOnUiThread {
            activity.start()
        }
        await().atMost(30, TimeUnit.SECONDS).until(storiesLoaded())
        DataManagerModuleTest.counter = 0
    }

    @Ignore
    private fun storiesLoaded(): Callable<Boolean> {
        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                val adapter = activity.viewModel.adapter
                return (adapter != null && adapter.itemCount == DataManagerModuleTest.currentStories.size)
            }
        }
    }

}