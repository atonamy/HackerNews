package propertyguru.androidtest.com.hackernews.data

import android.content.Context
import kotlinx.coroutines.experimental.Job
import propertyguru.androidtest.com.hackernews.data.model.Comment
import propertyguru.androidtest.com.hackernews.data.model.Story
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


/**
 * Created by archie on 10/6/17.
 */
class CommentsDataHelper(defaultMessage: String) {

    interface Contract {
        fun initCurrentStory(): Story
        fun onCommentsLevelChanged(level: Int)
        fun reset()
        fun populateComments(comments: List<Comment>)
        fun done()
        fun clearThreads()
        fun removeLastThread()
        fun addThread(author: String)
        fun onEmptyResult()
        fun onError(message: String)
    }

    val dataManager = CommentsDataManager(defaultMessage)
    private var currentCommentsTask: Job? = null
    private var currentStory: Story? = null
    private var currentComment: Comment? = null
    private var currentKids: BlockingQueue<Long> = LinkedBlockingQueue<Long>()
    private var suspended = false
    private var commentsLevel = 0

    init {
        dataManager.onError = {
            onError(it)
        }
    }

    var loadedComments = 0
    var totalComments: Int? = null
    get() {
        return if (currentComment != null) currentComment?.kids?.size else currentStory?.kids?.size
    }
    private set

    var contract = object: Contract {
        override fun onCommentsLevelChanged(level: Int) {}
        override fun reset() {}
        override fun populateComments(comments: List<Comment>) {}
        override fun done() {}
        override fun clearThreads() {}
        override fun removeLastThread() {}
        override fun addThread(author: String) {}
        override fun onEmptyResult() {}
        override fun initCurrentStory(): Story {return Story()}
        override fun onError(message: String) {}
    }


    private val onError: (message: String) -> Unit= {
        contract.onError(it)
    }

    fun changeCommentsLevel(level: Int){
        if(level == 0) {
            commentsLevel = level
            currentComment = null
            contract.clearThreads()
            if(currentStory == null)
                currentStory = contract.initCurrentStory()
            loadTopLevelComments(currentStory!!)
        }
        else if(level < commentsLevel) {
            var parent = currentComment
            var count = 0
            (level+1..commentsLevel).forEach {
                parent = parent?.parentComment
                count++
                contract.removeLastThread()
            }
            commentsLevel -= count
            currentComment = parent
            if(parent != null) {
                loadComments(parent!!, false)
            }
        }
    }

    fun loadTopLevelComments(story: Story) {
        if(story.kids.size == 0) {
            contract.onEmptyResult()
            return
        }
        currentComment = null
        currentStory = story
        currentCommentsTask?.cancel()
        dataManager.reset()
        contract.reset()

        if(story.fullyLoaded) {
            loadedComments = story.comments.size
            contract.populateComments(story.comments)
            contract.done()
            return
        }

        if(story.pendingKids.size == 0)
            story.pendingKids = LinkedBlockingQueue<Long>(story.kids)

        currentKids = story.pendingKids
        loadedComments = story.kids.size - story.pendingKids.size
        if(story.comments.size > 0)
            contract.populateComments(story.comments)
        suspended = true
        resume()
    }

    fun loadComments(parent: Comment, newThread: Boolean = true) {
        if(parent.kids.size == 0) {
            contract.onEmptyResult()
            return
        }

        currentStory = null
        currentComment = parent
        currentCommentsTask?.cancel()
        dataManager.reset()
        contract.reset()

        if(newThread) {
            commentsLevel++
            contract.onCommentsLevelChanged(commentsLevel)
            contract.addThread(parent.author)
        }

        if(parent.fullyLoaded) {
            loadedComments = parent.comments.size
            contract.populateComments(parent.comments)
            contract.done()
            return
        }

        if(parent.pendingKids.size == 0)
            parent.pendingKids = LinkedBlockingQueue<Long>(parent.kids)


        currentKids = parent.pendingKids
        loadedComments = parent.kids.size - parent.pendingKids.size
        if(parent.comments.size > 0)
            contract.populateComments(parent.comments)
        suspended = true
        resume()
    }


    fun suspend() {
        val task = currentCommentsTask
        if(task != null) {
            dataManager.suspend(task)
            suspended = true
        }
    }

    fun resume() {
        if(!suspended)
            return
        suspended = false
        currentCommentsTask = dataManager.loadComments(currentKids) { comments, done ->
            loadedComments += comments.size
            comments.forEach {
                if(currentStory != null)
                    it.parentStory = currentStory
                else if(currentComment != null) {
                    it.parentComment = currentComment
                    it.parentStory = currentComment?.parentStory
                }
                currentStory?.pendingKids?.remove(it.id)
                currentComment?.pendingKids?.remove(it.id)
            }
            currentStory?.comments?.addAll(comments)
            currentComment?.comments?.addAll(comments)
            contract.populateComments(comments)
            if(done &&
                    (currentComment != null && loadedComments == totalComments &&
                            currentComment!!.comments.size == totalComments) ||
                    (currentStory != null && loadedComments == totalComments &&
                            currentStory!!.comments.size == totalComments)) {
                currentStory?.fullyLoaded = true
                currentComment?.fullyLoaded = true
                contract.done()
            }
            else if(done && currentComment != null) {
                currentComment?.comments?.clear()
                loadComments(currentComment!!)
            }
            else if(done && currentStory != null) {
                currentStory?.comments?.clear()
                loadTopLevelComments(currentStory!!)
            }

        }
    }

    fun release() {
        currentCommentsTask?.cancel()
    }

}