package propertyguru.androidtest.com.hackernews.main

import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.verify
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import propertyguru.androidtest.com.hackernews.data.StoriesDataHelper
import propertyguru.androidtest.com.hackernews.injection.components.DaggerTestApiComponent
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModuleTest
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.runner.RunWith


/**
 * Created by archie on 11/6/17.
 */
@RunWith(AndroidJUnit4::class)
class StoriesDataTest {

    @Inject
    lateinit var api: HackerNewsApi

    val storiesDataHelper: StoriesDataHelper = StoriesDataHelper("error")

    @Mock
    lateinit var contract: StoriesDataHelper.Contract

    val formatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy/HH:mm:ss")

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        DaggerTestApiComponent.builder().dataManagerModule(DataManagerModuleTest()).build().inject(this)
        storiesDataHelper.dataManager.hackerNewsApi = api
        storiesDataHelper.contract = contract
    }

    @Test
    @Throws(Exception::class)
    fun onStartLoadStories() {
        storiesDataHelper.loadStories()
        await().atMost(5, TimeUnit.SECONDS).until(totalStoriesSize())
        verify(contract).init()
        await().atMost(5, TimeUnit.SECONDS).until(loadedStoriesSize())
        verify(contract).populateStories(check {
            assertEquals(it.size, 3)
            assertEquals(it[0].id, 14529376)
            assertEquals(it[0].author, "tambourine_man")
            assertEquals(it[0].title, "Please Make Google AMP Optional")
            assertEquals(it[0].descendants, 287)
            assertEquals(it[0].kids.size, 2)
            assertEquals(it[0].kids[0], 14529448)
            assertEquals(it[0].kids[1], 14531272)
            assertEquals(it[0].score, "671")
            assertEquals(it[0].time, formatter.parse("11/06/2017/05:50:00"))
            assertEquals(it[0].url, "https://www.alexkras.com/please-make-google-amp-optional/")
        })
        verify(contract).done()
        testEmptyResult()
    }


    @Ignore
    fun testEmptyResult() {
        storiesDataHelper.loadStories()
        await().atMost(5, TimeUnit.SECONDS).until(loadedStoriesZero())
        verify(contract).onEmptyResult()
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
                return storiesDataHelper.loadedStories > 0
            }
        }
    }

    private fun loadedStoriesZero(): Callable<Boolean> {
        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                return storiesDataHelper.loadedStories == 0
            }
        }
    }
}