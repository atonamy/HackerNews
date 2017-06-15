package propertyguru.androidtest.com.hackernews.data

import android.content.Context
import kotlinx.coroutines.experimental.Job
import propertyguru.androidtest.com.hackernews.data.model.Story

/**
 * Created by archie on 11/6/17.
 */
class StoriesDataHelper(defaultMessage: String) {

    interface Contract {
        fun init()
        fun populateStories(stories: List<Story>)
        fun done()
        fun onEmptyResult()
        fun onError(message: String)
    }

    val dataManager = StoriesDataManager(defaultMessage)
    private var currentStoryTask: DataManager.ParamJob? = null
    private var suspended = false
    var storiesLoaded = false
    var totalStories = 0
    var loadedStories = 0

    init {
        dataManager.onError = {
            onError(it)
        }
    }

    private val onError: (message: String) -> Unit= {
        contract.onError(it)
    }

    var contract = object: Contract {
        override fun init() {}
        override fun populateStories(stories: List<Story>) {}
        override fun done() {}
        override fun onEmptyResult() {}
        override fun onError(message: String) {}
    }

    fun loadStories() {
        currentStoryTask?.cancel()
        dataManager.reset()
        storiesLoaded = false
        suspended = true
        resume()
    }


    fun resume() {
        if(!storiesLoaded && suspended) {
            suspended = false
            currentStoryTask = dataManager.loadStories({
                loadedStories = 0
                totalStories = it
                contract.init()
                if(it == 0)
                    contract.onEmptyResult()

            }) { stories, done ->
                loadedStories += stories.size
                contract.populateStories(stories)
                if(done && loadedStories == totalStories) {
                    storiesLoaded = true
                    contract.done()
                }
                else if(done)
                    loadStories()
            }
        }
    }

    fun suspend() {
        if(currentStoryTask != null && !storiesLoaded) {
            suspended = true
            dataManager.suspend(currentStoryTask!!)
        }
    }

    fun release() {
        currentStoryTask?.cancel()
    }

}