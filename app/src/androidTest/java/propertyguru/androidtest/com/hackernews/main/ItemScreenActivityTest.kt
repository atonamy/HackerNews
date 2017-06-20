package propertyguru.androidtest.com.hackernews.main

import android.content.Intent
import android.content.res.Resources
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.awaitility.Awaitility
import org.junit.*

import org.junit.runner.RunWith
import propertyguru.androidtest.com.hackernews.R
import propertyguru.androidtest.com.hackernews.data.model.Comment
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.injection.components.DaggerTestApiComponent
import propertyguru.androidtest.com.hackernews.injection.modules.DataManagerModuleTest
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import propertyguru.androidtest.com.hackernews.ui.activities.ItemScreenActivity
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import android.support.v7.widget.RecyclerView
import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


/**
 * Created by archie on 12/6/17.
 */

@RunWith(AndroidJUnit4::class)
class ItemScreenActivityTest {


    @get:Rule
    val rule: ActivityTestRule<ItemScreenActivity> = ActivityTestRule(ItemScreenActivity::class.java)

    @Inject
    lateinit var api: HackerNewsApi
    lateinit var activity: ItemScreenActivity

    lateinit var testStory: Story
    lateinit var testComment: Comment

    fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
        return RecyclerViewMatcher(recyclerViewId)
    }

    inner class RecyclerViewMatcher(internal val mRecyclerViewId: Int) {

        fun atPosition(position: Int): Matcher<View> {
            return atPositionOnView(position, -1)
        }

        fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {

            return object : TypeSafeMatcher<View>() {
                internal var resources: Resources? = null
                internal var childView: View? = null

                override fun describeTo(description: Description) {
                    val id = if (targetViewId == -1) mRecyclerViewId else targetViewId
                    var idDescription = Integer.toString(id)
                    if (this.resources != null) {
                        try {
                            idDescription = this.resources!!.getResourceName(id)
                        } catch (var4: Resources.NotFoundException) {
                            idDescription = String.format("%s (resource name not found)", id)
                        }

                    }

                    description.appendText("with id: " + idDescription)
                }

                override fun matchesSafely(view: View): Boolean {

                    this.resources = view.getResources()

                    if (childView == null) {
                        val recyclerView: RecyclerView = view.getRootView().findViewById(mRecyclerViewId) as RecyclerView
                        if (recyclerView != null) {

                            childView = recyclerView.findViewHolderForAdapterPosition(position).itemView
                        } else {
                            return false
                        }
                    }

                    if (targetViewId == -1) {
                        return view === childView
                    } else {
                        val targetView = childView!!.findViewById(targetViewId)
                        return view === targetView
                    }

                }
            }
        }
    }

    @Before
    fun setUp() {
        DaggerTestApiComponent.builder().dataManagerModule(DataManagerModuleTest()).build().inject(this)
        ItemScreenActivity.suspendStart = true
        DataManagerModuleTest.counter = 3
        testStory = api.getStoryItem(DataManagerModuleTest.currentStories[0]).execute().body()!!
        testComment = api.getCommentItem(testStory.kids[0]).execute().body()!!
        activity = rule.launchActivity(testStory.toIntent(Intent()))
        activity.commentsHelper.dataManager.hackerNewsApi = api
    }

    @Test
    @Throws(Exception::class)
    fun testDisplayCommentsAndDisplayStoryUrl() {
        activity.runOnUiThread {
            activity.start()
        }
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(topLevelCommentsLoaded())
        onView(withRecyclerView(R.id.comments_list).atPositionOnView(0, R.id.view_replies)).perform(click())
        Awaitility.await().atMost(30, TimeUnit.SECONDS).until(subCommentsLoaded())

        DataManagerModuleTest.counter = 0
        onView(withId(R.id.view_story)).perform(click())
        Thread.sleep(5000)
    }


    @Ignore
    private fun topLevelCommentsLoaded(): Callable<Boolean> {
        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                val adapter = activity.viewModel.adapter
                return (adapter != null && adapter.itemCount == testStory.kids.size)
            }
        }
    }

    @Ignore
    private fun subCommentsLoaded(): Callable<Boolean> {
        return object : Callable<Boolean> {
            @Throws(Exception::class)
            override fun call(): Boolean {
                val adapter = activity.viewModel.adapter
                return (adapter != null && adapter.itemCount == testComment.kids.size)
            }
        }
    }

}