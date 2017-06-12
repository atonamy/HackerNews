package propertyguru.androidtest.com.hackernews.main

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.view.View
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import propertyguru.androidtest.com.hackernews.data.model.Comment
import propertyguru.androidtest.com.hackernews.data.view_model.CommentViewModel
import propertyguru.androidtest.com.hackernews.data.view_model.CommentsViewModel
import propertyguru.androidtest.com.hackernews.ui.activities.ItemScreenActivity

/**
 * Created by archie on 12/6/17.
 */
@RunWith(AndroidJUnit4::class)
class ItemScreenActivityViewModelTest {

    val commentsViewModel = CommentsViewModel(InstrumentationRegistry.getTargetContext())
    val commentViewModel = CommentViewModel()

    val testComment = Comment()

    @Mock
    lateinit var activity: ItemScreenActivity

    @Before
    fun setup(){
        MockitoAnnotations.initMocks(this)
        commentsViewModel.contract = activity
        commentViewModel.contract = activity
        commentViewModel.model = testComment
    }

    @Test
    @Throws(Exception::class)
    fun onViewStoryClick() {
        commentsViewModel.onClick.onClick(null)
        verify(activity).onReadStoryClick("")
    }

    @Test
    @Throws(Exception::class)
    fun onCommentsLevelChenged() {
        commentsViewModel.currentCommentLevel = 1
        verify(activity).onCommentLevelChanged(1)
    }

    @Test
    @Throws(Exception::class)
    fun onViewStoryVisibilityChanged() {
        commentsViewModel.viewStoryVisibility = View.GONE
        verify(activity).onViewStoryVisibilityChanged(View.GONE)
    }

    @Test
    @Throws(Exception::class)
    fun onSubCommentsClick() {
        commentViewModel.onClick.onClick(null)
        verify(activity).onSubCommentsClick(testComment)
    }

}

