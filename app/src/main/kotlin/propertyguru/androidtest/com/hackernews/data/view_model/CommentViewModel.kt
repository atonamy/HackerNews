package propertyguru.androidtest.com.hackernews.data.view_model

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.text.Html
import android.view.View
import org.ocpsoft.prettytime.PrettyTime
import propertyguru.androidtest.com.hackernews.BR
import propertyguru.androidtest.com.hackernews.data.model.Comment
import android.text.Spanned



/**
 * Created by archie on 8/6/17.
 */
class CommentViewModel: BaseObservable() {

    interface Contract {
        fun onSubCommentsClick(parent: Comment)
    }

    var contract: Contract = object: Contract{
        override fun onSubCommentsClick(parent: Comment) {}
    }

    @get:Bindable
    var viewChildCommentsVisibility: Int = View.GONE
        set(value) {
            field = value
            notifyPropertyChanged(BR.viewChildCommentsVisibility)
        }

    @get:Bindable
    var model: Comment = Comment()
        set(value) {
            field = value
            notifyPropertyChanged(BR.model)
            prettyDate = PrettyTime().format(value.time)
            formattedText = fromHtml(value.text)
            if(value.kids.size > 0)
                viewChildCommentsVisibility = View.VISIBLE
            else
                viewChildCommentsVisibility = View.GONE
        }

    @get:Bindable
    var prettyDate: String = PrettyTime().format(model.time)
        set(value) {
            field = value
            notifyPropertyChanged(BR.prettyDate)
        }

    @get:Bindable
    var formattedText: Spanned = fromHtml(model.text)
        set(value) {
            field = value
            notifyPropertyChanged(BR.prettyDate)
        }

    @get:Bindable
    val onClick: View.OnClickListener = View.OnClickListener {
            contract.onSubCommentsClick(model)
        }

    @SuppressWarnings("deprecation")
    fun fromHtml(html: String): Spanned {
        val result: Spanned
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        } else {
            result = Html.fromHtml(html)
        }
        return result
    }


}