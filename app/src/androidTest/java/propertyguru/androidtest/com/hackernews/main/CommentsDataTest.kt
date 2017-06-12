package propertyguru.androidtest.com.hackernews.main

import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.awaitility.Awaitility
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import propertyguru.androidtest.com.hackernews.data.CommentsDataHelper
import propertyguru.androidtest.com.hackernews.data.model.Story
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
import propertyguru.androidtest.com.hackernews.data.model.Comment

/**
 * Created by archie on 12/6/17.
 */
@RunWith(AndroidJUnit4::class)
class CommentsDataTest {
    @Inject
    lateinit var api: HackerNewsApi

    val commentsDataHelper: CommentsDataHelper = CommentsDataHelper("error")

    @Mock
    lateinit var contract: CommentsDataHelper.Contract

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
    fun setup() {
        MockitoAnnotations.initMocks(this)
        DaggerTestApiComponent.builder().dataManagerModule(DataManagerModuleTest()).build().inject(this)
        commentsDataHelper.dataManager.hackerNewsApi = api
        commentsDataHelper.contract = contract
        whenever(contract.initCurrentStory()).thenReturn(testStory)
    }

    @Test
    @Throws(Exception::class)
    fun onStartLoadComments() {
        commentsDataHelper.loadTopLevelComments(testStory)
        verify(contract).reset()
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(loadedCommentsSize())
        verify(contract).populateComments(check {
            verifyTopLevelComments(it)
        })
        verify(contract).done()
        testLevelCommentChange()
    }

    @Test
    @Throws(Exception::class)
    fun onEmptyLoadComments() {
        commentsDataHelper.loadTopLevelComments(Story().apply {
            author = "raldi"
            url = "https://en.wikipedia.org/wiki/Area_code_710"
            title = "Area code 710"
            time = formatter.parse("11/06/2017/04:33:36")
            score = "273"
            id = 14529079
            descendants = 58
        })
        verify(contract).onEmptyResult()

    }

    @Test
    @Throws(Exception::class)
    fun onErrorLoadComments() {
        commentsDataHelper.loadTopLevelComments(Story().apply {
            author = "error author"
            url = "https://test-error.com"
            title = "Testing error"
            time = formatter.parse("11/06/2017/04:33:36")
            score = "273"
            kids = listOf(14531277)
            id = 14530552
            descendants = 58
        })
        Thread.sleep(2000)
        verify(contract).onError("Response.error()")
    }

    @Test
    @Throws(Exception::class)
    fun onSubLoadComments() {
        commentsDataHelper.loadComments(Comment().apply {
            author = "b0rsuk"
            id = 14531272
            kids = listOf(14531275)
            parent = 14529079
            time = formatter.parse("11/06/2017/00:30:12")
        })
        verify(contract).reset()
        verify(contract).addThread("b0rsuk")
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(loadedCommentsSize())
        verify(contract).populateComments(check {
            assertEquals(it.size, 1)
            assertEquals(it[0].author, "blazespin")
            assertEquals(it[0].id, 14531275)
            assertEquals(it[0].parent, 14531272)
        })
        verify(contract).done()
    }

    @Ignore
    fun verifyTopLevelComments(it: List<Comment>) {
        assertEquals(it.size, 2)
        assertEquals(it[0].author, "epistasis")
        assertEquals(it[0].id, 14529448)
        assertEquals(it[0].parent, 14529079)
        assertEquals(it[1].author, "b0rsuk")
        assertEquals(it[1].id, 14531272)
        assertEquals(it[1].parent, 14529079)
        assertEquals(it[1].kids.size, 1)
    }

    @Ignore
    fun testLevelCommentChange() {
        commentsDataHelper.changeCommentsLevel(0)
        verify(contract).clearThreads()
        verify(contract, times(2)).reset()
        verify(contract, times(2)).populateComments(check {
            verifyTopLevelComments(it)
        })
        verify(contract, times(2)).done()
    }

    @Ignore
    private fun loadedCommentsSize(): Callable<Boolean> {
        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                return commentsDataHelper.loadedComments > 0
            }
        }
    }

}