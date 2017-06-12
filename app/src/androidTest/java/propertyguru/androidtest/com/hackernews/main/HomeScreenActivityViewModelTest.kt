package propertyguru.androidtest.com.hackernews.main

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.data.view_model.StoriesViewModel
import propertyguru.androidtest.com.hackernews.data.view_model.StoryViewModel
import propertyguru.androidtest.com.hackernews.ui.activities.HomeScreenActivity

/**
 * Created by archie on 12/6/17.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenActivityViewModelTest {

    val storiesViewModel = StoriesViewModel(InstrumentationRegistry.getTargetContext())
    val storyViewModel = StoryViewModel(InstrumentationRegistry.getTargetContext())


    @Mock
    lateinit var activity: HomeScreenActivity

    val testStory = Story()

    @Before
    fun setup()
    {
        MockitoAnnotations.initMocks(this)
        storiesViewModel.contract = activity
        storyViewModel.contract = activity
        storyViewModel.model = testStory
    }

    @Test
    @Throws(Exception::class)
    fun onClickStory() {
        storyViewModel.onClick.onComplete(null)
        verify(activity).onStorySelect(testStory)
    }

    @Test
    @Throws(Exception::class)
    fun onRefreshScreen() {
        storiesViewModel.refreshListener.onRefresh()
        verify(activity).onRefresh()
    }


}