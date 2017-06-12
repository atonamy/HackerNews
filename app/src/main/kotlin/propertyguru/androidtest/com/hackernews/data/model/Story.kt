package propertyguru.androidtest.com.hackernews.data.model

import android.content.Context
import android.content.Intent
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


 open class Story : RealmObject() {

    @PrimaryKey
    @SerializedName("id")
    var id: Long = 0

    @SerializedName("by")
    var author: String = ""

    @SerializedName("score")
    var score: String = ""

    @SerializedName("time")
    var time: Date = Date()

    @SerializedName("title")
    var title: String = ""

    @SerializedName("descendants")
    var descendants: Int = 0

    @SerializedName("url")
    var url: String = ""

    @Ignore
    @SerializedName("kids")
    var kids: List<Long> = ArrayList<Long>()

    @Expose
    var comments: RealmList<Comment> = RealmList<Comment>()

    @Ignore
    @Expose
    var pendingKids: BlockingQueue<Long> = LinkedBlockingQueue<Long>()

    @Expose
    var fullyLoaded: Boolean = false

    fun toIntent(intent: Intent): Intent {
      intent.putExtra("id", id)
      intent.putExtra("author", author)
      intent.putExtra("score", score)
      intent.putExtra("time", time)
      intent.putExtra("title", title)
      intent.putExtra("url", url)
      intent.putExtra("descendants", descendants)
      intent.putExtra("kids", kids.toLongArray())
      return intent
    }

    fun toIntent(): Array<Pair<String, Any>> {
        return arrayOf(
                Pair("id", id),
                Pair("author", author),
                Pair("score", score),
                Pair("time", time),
                Pair("title", title),
                Pair("url", url),
                Pair("descendants", descendants),
                Pair("kids", kids.toLongArray())
        )
    }

    companion object {
      fun fromIntent(intent: Intent) : Story? {

          var notStory = false
          run breaker@ {
              arrayOf("author", "descendants", "score",  "time", "title", "descendants", "url",
                      "kids").forEach {
                  if (!intent.hasExtra(it)) {
                      notStory = true
                      return@breaker
                  }
              }
          }

          if(notStory)
              return null

          val story = Story()
          story.author = intent.getStringExtra("author")
          story.descendants = intent.getIntExtra("descendants", 0)
          story.score = intent.getStringExtra("score")
          story.time = intent.getSerializableExtra("time") as Date
          story.title = intent.getStringExtra("title")
          story.descendants = intent.getIntExtra("descendants", 0)
          story.url = intent.getStringExtra("url")
          story.kids = intent.getLongArrayExtra("kids").toList()

          return story
      }
    }

}


