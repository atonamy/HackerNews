package propertyguru.androidtest.com.hackernews.ui.activities


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_item_screen.*

import propertyguru.androidtest.com.hackernews.R
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.data.view_model.CommentsViewModel
import propertyguru.androidtest.com.hackernews.databinding.CommentsLevelSelectionBinding
import android.databinding.DataBindingUtil
import android.os.Handler
import android.util.Log
import android.view.View
import org.jetbrains.anko.longToast
import propertyguru.androidtest.com.hackernews.App
import propertyguru.androidtest.com.hackernews.data.CommentsDataHelper
import propertyguru.androidtest.com.hackernews.data.model.Comment
import propertyguru.androidtest.com.hackernews.data.view_model.CommentViewModel
import propertyguru.androidtest.com.hackernews.data.view_model.StoryViewModel
import propertyguru.androidtest.com.hackernews.databinding.ActivityItemScreenBinding
import propertyguru.androidtest.com.hackernews.ui.adapters.CommentsAdapter
import propertyguru.androidtest.com.hackernews.ui.helpers.CustomTabsHelper


open class ItemScreenActivity : AppCompatActivity(), CommentsViewModel.Contract, CommentViewModel.Contract,
    CommentsDataHelper.Contract {

        companion object {
            var suspendStart = false
        }

        lateinit var viewModel: CommentsViewModel
        lateinit var storyModel: Story
        val commentsHelper: CommentsDataHelper by lazy {
            CommentsDataHelper(getString(R.string.error_message))
        }


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            if(!suspendStart)
                start()
        }

        fun start() {
            initView()
            initActionBar()
        }

        fun initView() {
            val m = Story.fromIntent(intent)
            if(m == null) {
                finish()
                return;
            }

            storyModel = m
            viewModel = CommentsViewModel(this)
            viewModel.url = storyModel.url
            viewModel.contract = this
            if(viewModel.url.trim().isEmpty())
                viewModel.viewStoryVisibility = View.GONE
            viewModel.commentsLevelAdapter.add(getString(R.string.top_level_comments))
            val binding = DataBindingUtil.setContentView<ActivityItemScreenBinding>(this, R.layout.activity_item_screen)
            binding.viewModel = viewModel
            binding.story = StoryViewModel(this).apply {
                model = storyModel
            }
            initAdapter()
            commentsHelper.contract = this
        }

        fun initActionBar() {
            setSupportActionBar(toolbar)
            supportActionBar?.setHomeButtonEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            title = " "
        }

        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            val inflater = menuInflater
            inflater.inflate(R.menu.comments_menu, menu)
            val menuBinding = CommentsLevelSelectionBinding.bind(menu.findItem(R.id.action_comment_level).actionView)
            menuBinding.viewModel = viewModel
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_view_story -> viewModel.contract.onReadStoryClick(viewModel.url)
                android.R.id.home -> finish()
            }
            return super.onOptionsItemSelected(item)
        }

        override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
            menu?.findItem(R.id.action_view_story)?.isVisible = viewModel.viewStoryMenuButtonVisibility
            return super.onPrepareOptionsMenu(menu)
        }

        override fun onError(message: String) {
            commentsHelper.suspend()
            longToast(message)
            viewModel.loadingVisibility = View.GONE
        }

        fun initProgressBar(total: Int) {
            viewModel.progress = 0
            viewModel.progressVisibility = View.VISIBLE
            viewModel.maxProgress = total
        }

        fun initAdapter() {
            if(viewModel.adapter == null)
                viewModel.adapter = CommentsAdapter(this, this)
            else
                viewModel.adapter?.clearItems()
        }

        override fun onStart() {
            super.onStart()
            commentsHelper.resume()
        }

        override fun onStop() {
            super.onStop()
            commentsHelper.suspend()
        }

        override fun onReadStoryClick(url: String) {
            CustomTabsHelper(this).openTab(url) {
                onError(it)
            }
        }

        override fun onCommentLevelChanged(level: Int) {
            commentsHelper.changeCommentsLevel(level)
        }

        override fun onViewStoryVisibilityChanged(visibility: Int) {
            invalidateOptionsMenu()
        }

        override fun onSubCommentsClick(parent: Comment) {
            commentsHelper.loadComments(parent)
        }

        override fun onCommentsLevelChanged(level: Int) {
            viewModel.currentCommentLevel = level
        }

        override fun populateComments(comments: List<Comment>) {

            viewModel.loadingVisibility = View.GONE
            viewModel.itemsVisibility = View.VISIBLE

            viewModel.progress = commentsHelper.loadedComments
            viewModel.adapter?.addItems(comments)
        }

        override fun done() {
            if(commentsHelper.loadedComments == commentsHelper.totalComments)
                viewModel.progressVisibility = View.GONE
            else {
                Log.e(ItemScreenActivity::class.java.simpleName, "problem with loading comments: ${commentsHelper.loadedComments} != ${commentsHelper.totalComments}")
            }
        }

        override fun reset() {
            viewModel.adapter?.clearItems()
            viewModel.loadingVisibility = View.VISIBLE
            if(commentsHelper.totalComments != null && commentsHelper.totalComments!! > 0)
                initProgressBar(commentsHelper.totalComments!!)
            else
                viewModel.messageVisibility = View.VISIBLE
        }

        override fun clearThreads() {
            viewModel.commentsLevelAdapter?.clear()
            viewModel.commentsLevelAdapter?.add(String.format(getString(R.string.top_level_comments), storyModel.kids.size))
        }

        override fun removeLastThread() {
            viewModel.commentsLevelAdapter?.remove(viewModel.commentsLevelAdapter?.getItem(viewModel.commentsLevelAdapter?.count-1))
        }

        override fun addThread(author: String) {
            viewModel.commentsLevelAdapter?.add(String.format(getString(R.string.comments_under), author, commentsHelper.totalComments))
        }

        override fun onEmptyResult() {
            viewModel.loadingVisibility = View.GONE
            viewModel.messageVisibility = View.VISIBLE
        }

        override fun initCurrentStory(): Story {
            return storyModel
        }

        override fun onDestroy() {
            super.onDestroy()
            commentsHelper.release()
            App.refWatcher.watch(this)
        }
}
