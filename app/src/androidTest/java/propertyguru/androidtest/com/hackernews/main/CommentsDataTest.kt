package propertyguru.androidtest.com.hackernews.main

import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.*
import io.realm.RealmList
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
import org.junit.FixMethodOrder
import org.junit.Ignore
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import propertyguru.androidtest.com.hackernews.data.DataManager
import propertyguru.androidtest.com.hackernews.data.model.Comment

/**
 * Created by archie on 12/6/17.
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CommentsDataTest {
    @Inject
    lateinit var api: HackerNewsApi

    val commentsDataHelper: CommentsDataHelper = CommentsDataHelper("error")

    @Mock
    lateinit var contract: CommentsDataHelper.Contract

    val formatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy/HH:mm:ss")

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        DaggerTestApiComponent.builder().dataManagerModule(DataManagerModuleTest()).build().inject(this)
        whenever(contract.initCurrentStory()).doAnswer {
            getStory()!!
        }
        commentsDataHelper.dataManager.hackerNewsApi = api
        commentsDataHelper.contract = contract
    }

    //Test correct handle error response from API top comments
    @Test
    @Throws(Exception::class)
    fun on01ErrorTopCommentTestResult() {
        commentsDataHelper.loadTopLevelComments(Story().apply {
            author = "some author"
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


    //Test correct handle error response from API sub comments
    @Test
    @Throws(Exception::class)
    fun on02ErrorCommentTestResult() {
        commentsDataHelper.loadComments(Comment().apply {
            author = "some author"
            text = "Testing error"
            time = formatter.parse("11/06/2017/04:33:36")
            kids = listOf(14531277)
        })
        Thread.sleep(2000)
        verify(contract).onError("Response.error()")
    }


    //Test correct empty result response from API top comments
    @Test
    @Throws(Exception::class)
    fun on03EmptyTopCommentTestResult() {
        commentsDataHelper.loadTopLevelComments(Story().apply {
            author = "some author"
            url = "https://test-error.com"
            title = "Testing error"
            time = formatter.parse("11/06/2017/04:33:36")
            score = "273"
            kids = listOf()
            id = 14530552
            descendants = 58
        })
        Thread.sleep(2000)
        verify(contract).onEmptyResult()
    }

    //Test correct empty result response from API sub comments
    @Test
    @Throws(Exception::class)
    fun on04EmptyCommentTestResult() {
        commentsDataHelper.loadComments(Comment().apply {
            author = "some author"
            text = "Testing error"
            time = formatter.parse("11/06/2017/04:33:36")
            kids = listOf()
        })
        Thread.sleep(2000)
        verify(contract).onEmptyResult()
    }

    //Test correct top level comments quantity and sequence population from API
    @Test
    @Throws(Exception::class)
    fun on05LoadedTopCommentsTestResult() {
        var story: Story? = getStory()
        testLoadedComments(Pair(0, 0), story?.kids, 1, {}) {
            commentsDataHelper.loadTopLevelComments(story!!)
        }

    }

    //Test correct top level comments quantity and sequence population with suspend/resume from API
    @Test
    @Throws(Exception::class)
    fun on06LoadedTopCommentsSuspendResumeTestResult() {
        var story: Story? = getStory()
        testLoadedComments(Pair(0, 0), story?.kids, 1, {
            Thread.sleep(1000)
            commentsDataHelper.suspend()
            Thread.sleep(500)
            commentsDataHelper.resume()
        }) {
            commentsDataHelper.loadTopLevelComments(story!!)
        }
    }

    //Test correct sub comments quantity and sequence population from API
    @Test
    @Throws(Exception::class)
    fun on07LoadedCommentsTestResult() {
        var comment: Comment? = getComment()
        testLoadedComments(Pair(0, 0), comment?.kids, 1, {
            verify(contract).onCommentsLevelChanged(1)
            verify(contract).addThread("epistasis")
        }) {
            commentsDataHelper.loadComments(comment!!)
        }
    }


    //Test correct sub comments quantity and sequence population from API
    @Test
    @Throws(Exception::class)
    fun on08LoadedCommentsSuspendResumeTestResult() {
        var comment: Comment? = getComment()
        testLoadedComments(Pair(0, 0), comment?.kids, 1, {
            verify(contract).onCommentsLevelChanged(1)
            verify(contract).addThread("epistasis")
            Thread.sleep(1000)
            commentsDataHelper.suspend()
            Thread.sleep(500)
            commentsDataHelper.resume()
        }) {
            commentsDataHelper.loadComments(comment!!)
        }
    }


    //Test correct change to top level comments
    @Test
    @Throws(Exception::class)
    fun on09ChangeToTopLevelCommentsTestResult() {
        commentsDataHelper.changeCommentsLevel(0)
        verify(contract).initCurrentStory()
        verify(contract).clearThreads()
        val story = getStory()
        testLoadedComments(Pair(0, 0), story!!.kids, 1, {}, {})
    }


    //Test correct change to middle level comments
    @Test
    @Throws(Exception::class)
    fun on10ChangeToSecondLevelCommentsTestResult() {
        var comment: Comment? = getComment()

        val topLevelSize = testLoadedComments(Pair(0, 0), comment?.kids, 1, {
            verify(contract).onCommentsLevelChanged(1)
            verify(contract).addThread("epistasis")
        }) {
            commentsDataHelper.loadComments(comment!!)
        }

        comment = getSubComment()

        testLoadedComments(topLevelSize, comment?.kids, 2, {
            verify(contract, times(1)).onCommentsLevelChanged(2)
            verify(contract, times(1)).addThread("b0rsuk")
        }) {
            commentsDataHelper.loadComments(comment!!)
        }

        commentsDataHelper.changeCommentsLevel(1)
        verify(contract).removeLastThread()
        verify(contract, times(3)).done()
    }



    @Ignore
    private fun getStory(): Story? {
        var story: Story? = null
        for(kid in DataManagerModuleTest.currentStories){
            val s = api.getStoryItem(kid).execute().body()!!
            if(s.kids.size > 0) {
                story = s
                break;
            }
        }

        return story
    }

    @Ignore
    private fun getComment(): Comment? {
        var comment: Comment? = null
        for(kid in DataManagerModuleTest.currentStories){
            val s = api.getStoryItem(kid).execute().body()!!
            if(s.kids.size > 0) {
                for (subKid in s.kids){
                    val c = api.getCommentItem(subKid).execute().body()!!
                    if(c.kids.size > 0)
                    {
                        comment = c
                        break;
                    }
                }
                if(comment != null)
                    break;
            }
        }

        return comment
    }

    @Ignore
    private fun getSubComment(): Comment? {
        var comment: Comment? = null
        for(kid in DataManagerModuleTest.currentStories){
            val s = api.getStoryItem(kid).execute().body()!!
            if(s.kids.size > 0) {
                for (subKid in s.kids){
                    val c = api.getCommentItem(subKid).execute().body()!!
                    if(c.kids.size > 0)
                    {
                        comment = api.getCommentItem(c.kids[0]).execute().body()!!
                        break;
                    }
                }
                if(comment != null)
                    break;
            }
        }

        return comment
    }

    @Ignore
    private fun loadedCommentsSize(): Callable<Boolean> {

        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                return (commentsDataHelper.loadedComments == commentsDataHelper.totalComments)
            }
        }
    }


    @Ignore
    private inline fun testLoadedComments(pagesAndSize: Pair<Int, Int>, kids: List<Long>?, times: Int, body: () -> Unit,
                                          load: () -> Unit): Pair<Int, Int> {
        assertNotNull(kids)
        val allComments  = RealmList<Comment>()
        load()
        verify(contract, times(times)).reset()
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(loadedCommentsSize())
        val totalPages = (commentsDataHelper.totalComments!! + DataManager.maxPreloadItems - 1) / DataManager.maxPreloadItems
        verify(contract, times(totalPages+pagesAndSize.first)).populateComments(check {
            allComments.addAll(it)
        })
        body()
        val realSize = allComments.size/2 - pagesAndSize.second
        assertEquals(kids!!.size, realSize)
        for (i in 0 until kids.size)
            assertEquals(kids[i], allComments[i+pagesAndSize.second].id)
        verify(contract, times(times)).done()

        return Pair(totalPages, realSize)
    }
}