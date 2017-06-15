package propertyguru.androidtest.com.hackernews.data

import io.realm.RealmList
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
    private var currentTask: DataManager.ParamJob? = null
    private var currentStory: Story? = null
    private var currentComment: Comment? = null
    private var currentKids: BlockingQueue<Long> = LinkedBlockingQueue<Long>()
    private var suspended = false
    private var commentsLevel = 0

    init {
        dataManager.onError = {
            onError(it)
        }

        dataManager.onSuspended = { items, parameter ->
            val parameter = parameter
            if (parameter != null) {
                when (parameter) {
                    is Story -> adjustKids(items, parameter.pendingKids, {parameter.pendingKids = it})
                    is Comment -> adjustKids(items, parameter.pendingKids, {parameter.pendingKids = it})
                }
            }
        }
    }

    var loadedComments = 0
    val totalComments: Int?
    get() {
        return if (currentComment != null) currentComment?.kids?.size else currentStory?.kids?.size
    }

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

    private inline fun adjustKids(items: BlockingQueue<Long>,
                                  kids: BlockingQueue<Long>,
                                  set: (kids: BlockingQueue<Long>) -> Unit) {
        val temp = LinkedBlockingQueue<Long>(items)
        temp.addAll(kids)
        kids.clear()
        set(temp)
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

    private fun reset(fullyLoaded: Boolean, comments: RealmList<Comment>,
                      body: () -> Unit = {}): Boolean {
        currentTask?.cancel()
        dataManager.reset()
        contract.reset()

        body()

        if(fullyLoaded) {
            loadedComments = comments.size
            contract.populateComments(comments)
            contract.done()
            return true
        }

        return false
    }

    private fun ifEmptyResult(size: Int): Boolean {
        if(size == 0) {
            contract.onEmptyResult()
            return true
        }
        return false
    }

    private inline fun load(kids:  List<Long>,
                     pKids: BlockingQueue<Long>,
                     comments: RealmList<Comment>,
                     initPKids: (kids: LinkedBlockingQueue<Long>) -> BlockingQueue<Long>) {

        var pk = pKids
        if(pk.size == 0)
            pk = initPKids(LinkedBlockingQueue<Long>(kids))

        currentKids = pk
        loadedComments = kids.size - pk.size
        if(comments.size > 0)
            contract.populateComments(comments)
        suspended = true
        resume()
    }

    fun loadTopLevelComments(story: Story) {
        if(ifEmptyResult(story.kids.size))
            return

        currentComment = null
        currentStory = story

        if(reset(story.fullyLoaded, story.comments))
            return


        load(story.kids, story.pendingKids, story.comments) {
            story.pendingKids = it
            it
        }

    }

    fun loadComments(parent: Comment, newThread: Boolean = true) {
        if(ifEmptyResult(parent.kids.size))
            return

        currentStory = null
        currentComment = parent

        if(reset(parent.fullyLoaded, parent.comments) {
            if(newThread) {
                commentsLevel++
                contract.onCommentsLevelChanged(commentsLevel)
                contract.addThread(parent.author)
            }
        })
            return

        load(parent.kids, parent.pendingKids, parent.comments) {
            parent.pendingKids = it
            it
        }
    }


    fun suspend() {
        val task = currentTask
        if(task != null) {
            dataManager.suspend(task)
            suspended = true
        }
    }

    fun resume() {
        if(!suspended)
            return
        suspended = false
        currentTask = dataManager.loadComments(currentKids) { comments, done ->
            loadedComments += comments.size
            comments.forEach {
                if(currentStory != null)
                    it.parentStory = currentStory
                else if(currentComment != null) {
                    it.parentComment = currentComment
                    it.parentStory = currentComment!!.parentStory
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
        currentTask?.parameter = if(currentComment != null) currentComment!! else currentStory!!
    }

    fun release() {
        currentTask?.cancel()
    }

}