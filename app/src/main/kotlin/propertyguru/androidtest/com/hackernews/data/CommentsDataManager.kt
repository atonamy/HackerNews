package propertyguru.androidtest.com.hackernews.data

import android.content.Context
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import propertyguru.androidtest.com.hackernews.data.model.Comment
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * Created by archie on 10/6/17.
 */
class CommentsDataManager(defaultMessage: String): DataManager(defaultMessage) {

    fun loadComments(kids: BlockingQueue<Long>, result: (comments: List<Comment>, done: Boolean) -> Unit): ParamJob {

        return loadItems({
            doReset()
            kids
        }, {
            hackerNewsApi.getCommentItem(it).execute()
        }, result)
    }
}