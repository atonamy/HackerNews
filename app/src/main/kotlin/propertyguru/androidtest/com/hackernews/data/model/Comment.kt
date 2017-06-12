package propertyguru.androidtest.com.hackernews.data.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.ArrayList

/**
 * Created by archie on 6/6/17.
 */
open class Comment: RealmObject() {

    @PrimaryKey
    @SerializedName("id")
    var id: Long = 0

    @SerializedName("parent")
    var parent: Long = 0

    @SerializedName("by")
    var author: String = ""

    @SerializedName("time")
    var time: Date = Date()

    @SerializedName("text")
    var text: String = ""

    @Ignore
    @SerializedName("kids")
    var kids: List<Long> = ArrayList<Long>()

    @Expose
    var parentComment: Comment? = null
    @Expose
    var parentStory: Story? = null

    @Ignore
    @Expose
    var pendingKids: BlockingQueue<Long> = LinkedBlockingQueue<Long>()

    @Expose
    var fullyLoaded: Boolean = false

    @Expose
    var comments: RealmList<Comment> = RealmList<Comment>()
}