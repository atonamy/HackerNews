package propertyguru.androidtest.com.hackernews.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.sync.Mutex
import propertyguru.androidtest.com.hackernews.App
import propertyguru.androidtest.com.hackernews.R
import propertyguru.androidtest.com.hackernews.network.api.HackerNewsApi
import retrofit2.Response
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by archie on 6/6/17.
 */
open class DataManager(private val defaultMessage: String = "") {

    @Inject
    lateinit var hackerNewsApi: HackerNewsApi

    init {
        App.dataManagerComponent.inject(this)
    }

    var onError: (message: String) -> Unit= {}
    private val error: (message: String) -> Unit= {
        launch(UI){
            onError(it)
        }
    }


    private val singleThread = Mutex()
    private val maxPreloadItems = 25
    protected var reset = false

    protected var itemsIds: BlockingQueue<Long>? = null
    protected var suspendedItemsIds: BlockingQueue<Long>? = null


    fun suspend(task: Job) {
        if(itemsIds != null && itemsIds!!.size > 0) {
            suspendedItemsIds = LinkedBlockingQueue<Long>(itemsIds)
            itemsIds?.clear()
            task.cancel()
        }
    }

    protected fun clearItems() {
        itemsIds?.clear()
        suspendedItemsIds?.clear()
        reset = false
        Log.w(DataManager::class.java.simpleName, "items cleared")
    }

    fun reset() {
        reset = true
    }

    protected suspend fun doReset() {
        if(reset) {
            delay(1500)
            clearItems()
        }
    }

    private inline suspend fun <T> populateItems(itemsOutput: () -> ConcurrentHashMap<Long, T>,
                                                 result: (items: MutableMap<Long, T>) -> Unit,
                                                 reset: (output: ConcurrentHashMap<Long, T>) -> Unit) {
        singleThread.lock()
        try {
            val items = itemsOutput()
            reset(ConcurrentHashMap<Long, T>())
            if (items.size > 0) {
                Log.d(DataManager::class.java.simpleName, "drop items: ${items.size}")
                result(items)
            }
            Log.d(DataManager::class.java.simpleName, "drop items: ${items.size}")
        } finally {
            singleThread.unlock()
        }
    }

    private fun <T> preloadItems(ids: BlockingQueue<Long>, result: (items: MutableMap<Long, T>) -> Unit,
                                 getItem: (id: Long) -> Response<T>?): Deferred<MutableMap<Long, T>> = async(CommonPool) {
        var itemsOutput = ConcurrentHashMap<Long, T>()
        val tasks = ArrayList<Job>()
        var iterator = ids.iterator()
        val parentTask = this
        var cancel = false

        val populateItems: suspend () -> Unit = {
            populateItems({itemsOutput}, result) {
                itemsOutput = it
            }
            Log.d(DataManager::class.java.simpleName, "output size: ${itemsOutput.size}")
        }

        while(iterator.hasNext() && isActive){
            val id = iterator.next()

            tasks.add(launch(CommonPool) {
                val kid = id
                Log.w(DataManager::class.java.simpleName, "kid: $kid started")
                Log.d(DataManager::class.java.simpleName, "current parent coroutine status: ${parentTask.isActive}")
                Log.d(DataManager::class.java.simpleName, "current child coroutine status: $isActive")
                if(isActive && parentTask.isActive && !cancel) {
                    var itemResult: Response<T>? = getItem(kid)

                    if (itemResult != null && itemResult.isSuccessful) {
                        val item = itemResult.body()

                        if(ids.contains(kid)) {
                            ids.remove(kid)
                            itemsOutput.put(kid, item!!)
                        }

                        Log.d(DataManager::class.java.simpleName, "remain items: ${ids.size}")
                    }
                    else if(itemResult != null) {
                        cancel = true
                        Log.e(DataManager::class.java.simpleName, itemResult.message() +
                                itemResult.errorBody().toString())
                        error(itemResult.message() ?: defaultMessage)
                    }
                    else
                        cancel = true

                    if(cancel || ids.size == 0 && !parentTask.isActive)
                        populateItems()
                }
            })

            if (tasks.size == maxPreloadItems || !isActive || !iterator.hasNext()) {
                tasks.forEach {
                    if (isActive)
                        it.join()
                    else
                        it.cancel()
                }
                if(tasks.size == maxPreloadItems)
                    populateItems()
                tasks.clear()
            }
        }

        itemsOutput
    }


    protected fun <T> loadItems(kids: suspend () -> BlockingQueue<Long>?,
                              getItem: (id: Long) -> Response<T>?,
                              result: (items: List<T>, done: Boolean) -> Unit,
                              failed: () -> Unit = {}): Job {
        var task: Deferred<MutableMap<Long, T>>? = null
        var exception = false
        val job = launch(UI) {
            var kidsItems: BlockingQueue<Long>? = null
            var pendingItems: BlockingQueue<Long>? = null
            try {
                kidsItems = kids()
                pendingItems = LinkedBlockingQueue<Long>(kidsItems)
            } catch(ex: Exception) {
                if(ex !is CancellationException) {
                    error(ex.message ?: defaultMessage)
                    exception = true
                }
                failed()
                Log.e(DataManager::class.java.simpleName, ex.toString())
            }

            if(kidsItems != null && kidsItems.size > 0) {
                var items: MutableMap<Long, T>? = null
                try {
                    task = preloadItems(kidsItems, {
                        if(it.size == maxPreloadItems) {
                            Log.d(DataManager::class.java.simpleName, "result items: " + it.size)
                            launch(UI) { result(getItemsResult(it, pendingItems!!), false) }
                        }
                        else {
                            if(suspendedItemsIds != null) {
                                val temp = LinkedBlockingQueue<Long>(it.keys)
                                temp.addAll(suspendedItemsIds!!)
                                suspendedItemsIds?.clear()
                                suspendedItemsIds = temp
                            }
                        }
                    }, {
                        try {
                            getItem(it)
                        } catch(ex: Exception) {
                            Log.e(DataManager::class.java.simpleName, ex.toString())
                            if(!exception) {
                                exception = true
                                error(ex.message ?: defaultMessage)
                                failed()
                            }
                            null
                        }
                    })
                    items = task!!.await()
                } catch(ex: Exception) {
                    if(ex !is CancellationException) {
                        error(ex.message ?: defaultMessage)
                        exception = true
                    }
                    failed()
                    Log.e(DataManager::class.java.simpleName, ex.toString())
                }

                Log.d(DataManager::class.java.simpleName, "final result items: " + items?.size)
                if(items != null && !exception)
                    result(getItemsResult(items, pendingItems!!), true)
            }

        }

        job.invokeOnCompletion {
            Log.d(DataManager::class.java.simpleName, "items completed")
            task?.cancel()
        }

        return job
    }

    private fun <T> getItemsResult(items: MutableMap<Long, T>, pendingItems: BlockingQueue<Long>): List<T> {
        val itemsResult = ArrayList<T>()
        val size = items.size
        (1..size).forEach {
            val id = pendingItems.remove()
            val value = items[id]
            if(value != null)
                itemsResult.add(value)
            else
                Log.e(DataManager::class.java.simpleName, "item result id: $id error")
        }
        items.clear()
        return itemsResult
    }

}