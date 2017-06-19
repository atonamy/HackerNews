package propertyguru.androidtest.com.hackernews.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.experimental.*
import propertyguru.androidtest.com.hackernews.R
import propertyguru.androidtest.com.hackernews.data.model.Story
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by archie on 10/6/17.
 */
class StoriesDataManager(private val defaultMessage: String): DataManager(defaultMessage) {

    private val preloadStoriesIds: Deferred<BlockingQueue<Long>?> get() = async(CommonPool) {
        if(suspendedItemsIds != null && suspendedItemsIds!!.size > 0) {
            Log.d(StoriesDataManager::class.java.simpleName, "return existing pending stories")
            suspendedItemsIds
        } else {
            val storiesResult = hackerNewsApi.topStories.execute()
            if (storiesResult.isSuccessful) {
                Log.d(StoriesDataManager::class.java.simpleName, "return new stories")
                LinkedBlockingQueue<Long>(storiesResult.body())
            } else {
                Log.e(StoriesDataManager::class.java.simpleName, storiesResult.message() +
                        storiesResult.errorBody().toString())
                error(storiesResult.message() ?: storiesResult.errorBody()?.string() ?:
                        defaultMessage)
                null
            }
        }
    }

    fun loadStories(wholeStories: (total: Int) -> Unit,
                    result: (stories: List<Story>, done: Boolean) -> Unit): ParamJob {

        return loadItems({
            doReset()
            val stories = preloadStoriesIds.await()
            if(stories != null && (suspendedItemsIds == null ||
                    suspendedItemsIds!!.size == 0)
                    )
                wholeStories(stories.size)
            itemsIds = stories
            itemsIds
        }, {
            hackerNewsApi.getStoryItem(it).execute()
        }, result)
    }
}
