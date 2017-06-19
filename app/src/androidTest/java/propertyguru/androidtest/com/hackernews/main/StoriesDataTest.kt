package propertyguru.androidtest.com.hackernews.main

import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.realm.RealmList
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import propertyguru.androidtest.com.hackernews.data.StoriesDataHelper
import propertyguru.androidtest.com.hackernews.injection.components.DaggerTestApiComponent
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModuleTest
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import propertyguru.androidtest.com.hackernews.data.DataManager
import propertyguru.androidtest.com.hackernews.data.model.Story


/**
 * Created by archie on 11/6/17.
 */

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class StoriesDataTest {

    @Inject
    lateinit var api: HackerNewsApi

    val storiesDataHelper: StoriesDataHelper = StoriesDataHelper("error")

    @Mock
    lateinit var contract: StoriesDataHelper.Contract

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        DaggerTestApiComponent.builder().dataManagerModule(DataManagerModuleTest()).build().inject(this)
        storiesDataHelper.dataManager.hackerNewsApi = api
        storiesDataHelper.contract = contract
    }

    //Test correct handle error response from API
   @Test
    @Throws(Exception::class)
    fun on1ErrorStoriesTestResult() {
        storiesDataHelper.loadStories()
        Thread.sleep(2000)
        verify(contract).onError("Response.error()")
    }

    //Test corrept empty result response from API
    @Test
    @Throws(Exception::class)
    fun on2EmptyStoriesTestResult() {
        storiesDataHelper.loadStories()
        Thread.sleep(2000)
        verify(contract).init()
        verify(contract).onEmptyResult()
    }



    //Test correct strories quantity and sequence population from API (random size)
    @Test
    @Throws(Exception::class)
    fun on3LoadedStoriesTestResult() {
        testLoadedStories {  }
    }

    //Test suspend/resume fetching from API
    @Test
    @Throws(Exception::class)
    fun on4LoadedStoriesTestResult() {
        testLoadedStories {
            Thread.sleep(1000)
            storiesDataHelper.suspend()
            Thread.sleep(500)
            storiesDataHelper.resume()
        }
    }

    //Test correct handle another error response from API
    @Test
    @Throws(Exception::class)
    fun on5RecordErrorStoriesTestResult() {
        storiesDataHelper.loadStories()
        Thread.sleep(2000)
        verify(contract).init()
        verify(contract).onError("Response.error()")
    }


    @Ignore
    private fun totalStoriesSize(): Callable<Boolean> {
        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                return storiesDataHelper.totalStories > 0
            }
        }
    }

    @Ignore
    private fun loadedStoriesSize(): Callable<Boolean> {

        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                return (storiesDataHelper.loadedStories == storiesDataHelper.totalStories)
            }
        }
    }


    @Ignore
    private inline fun testLoadedStories(body: () -> Unit) {
        storiesDataHelper.loadStories()
        await().atMost(5, TimeUnit.SECONDS).until(totalStoriesSize())
        verify(contract).init()

        val allStories = RealmList<Story>()

        body()

        await().atMost(60, TimeUnit.SECONDS).until(loadedStoriesSize())
        val totalPages = (DataManagerModuleTest.currentStories.size + DataManager.maxPreloadItems - 1) / DataManager.maxPreloadItems
        verify(contract, times(totalPages)).populateStories(check {
            allStories.addAll(it)
        })

        assertEquals(DataManagerModuleTest.currentStories.size, allStories.size/2)
        for (i in 0 until DataManagerModuleTest.currentStories.size)
            assertEquals(DataManagerModuleTest.currentStories[i], allStories[i].id)

        verify(contract).done()
    }
}