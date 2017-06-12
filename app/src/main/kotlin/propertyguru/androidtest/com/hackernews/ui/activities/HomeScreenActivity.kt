package propertyguru.androidtest.com.hackernews.ui.activities

import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import org.jetbrains.anko.longToast
import propertyguru.androidtest.com.hackernews.App
import propertyguru.androidtest.com.hackernews.R
import propertyguru.androidtest.com.hackernews.data.StoriesDataHelper
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.data.view_model.StoriesViewModel
import propertyguru.androidtest.com.hackernews.data.view_model.StoryViewModel
import propertyguru.androidtest.com.hackernews.databinding.ActivityHomeScreenBinding
import propertyguru.androidtest.com.hackernews.ui.adapters.StoriesAdapter


open class HomeScreenActivity : AppCompatActivity(), StoriesViewModel.Contract, StoryViewModel.Contract,
        StoriesDataHelper.Contract {

        val storiesHelper: StoriesDataHelper by lazy {
            StoriesDataHelper(getString(R.string.error_message))
        }

        lateinit var viewModel: StoriesViewModel


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            initView()
        }

        fun initView() {
            viewModel = StoriesViewModel(this)
            DataBindingUtil.setContentView<ActivityHomeScreenBinding>(this, R.layout.activity_home_screen)
                    .viewModel = viewModel
            viewModel.itemsVisibility = View.VISIBLE
            viewModel.refreshing = true
            viewModel.contract = this
            storiesHelper.contract = this
            storiesHelper.loadStories()
        }

        fun initProgressBar(total: Int) {
            viewModel.progress = 0
            viewModel.progressVisibility = View.VISIBLE
            viewModel.maxProgress = total
        }

        fun initAdapter() {
            if(viewModel.adapter == null)
                viewModel.adapter = StoriesAdapter(this, this)
            else
                viewModel.adapter?.clearItems()
        }

        override fun onStart() {
            super.onStart()
            storiesHelper.resume()
        }

        override fun onDestroy() {
            super.onDestroy()
            storiesHelper.release()
            App.refWatcher.watch(this)
        }

        override fun onStop() {
            super.onStop()
            storiesHelper.suspend()
        }

        override fun onError(message: String) {
            longToast(message)
            viewModel.refreshing = false
        }


        override fun onRefresh() {
            viewModel.progressVisibility = View.GONE
            storiesHelper.loadStories()
        }

        override fun onStorySelect(story: Story) {
            startActivity(story.toIntent(Intent(this, ItemScreenActivity::class.java)))
        }

        override fun init() {
            initAdapter()
            initProgressBar(storiesHelper.totalStories)
        }

        override fun populateStories(stories: List<Story>) {
            viewModel.refreshing = false
            viewModel.progress = storiesHelper.loadedStories
            viewModel.adapter?.addItems(stories)
        }

        override fun done() {
            if(storiesHelper.loadedStories == storiesHelper.totalStories)
                viewModel.progressVisibility = View.GONE
            else
                Log.e(HomeScreenActivity::class.java.simpleName, "problem with loading stories: ${storiesHelper.loadedStories} != ${storiesHelper.totalStories}")
        }

        override fun onEmptyResult() {
            viewModel.messageVisibility = View.VISIBLE
        }

}
