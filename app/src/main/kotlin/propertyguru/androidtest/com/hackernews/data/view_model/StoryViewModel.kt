package propertyguru.androidtest.com.hackernews.data.view_model

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.BindingAdapter
import android.support.v4.content.ContextCompat
import android.view.View
import com.andexert.library.RippleView
import org.ocpsoft.prettytime.PrettyTime
import propertyguru.androidtest.com.hackernews.BR
import propertyguru.androidtest.com.hackernews.R
import propertyguru.androidtest.com.hackernews.data.model.Story
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by archie on 8/6/17.
 */
class StoryViewModel(private val ctx: Context): BaseObservable() {

    interface Contract {
        fun onStorySelect(story: Story)
    }

    var contract: Contract = object: Contract{
        override fun onStorySelect(story: Story) {}
    }

    @get:Bindable
    var model: Story = Story()
        set(value) {
            field = value
            notifyPropertyChanged(BR.model)
            try {
                host = URL(field.url).host
            } catch(ex: MalformedURLException){
                host = ""
            }
            prettyDate = PrettyTime().format(model.time)
            iconText = if(model.title.isEmpty()) "" else model.title.substring(0, 1)
        }

    @get:Bindable
    var host: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.host)
        }

    @get:Bindable
    var prettyDate: String = PrettyTime().format(model.time)
        set(value) {
            field = value
            notifyPropertyChanged(BR.prettyDate)
        }

    @get:Bindable
    var iconText: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.iconText)
        }

    @get:Bindable
    var iconColorFilter: Int = ContextCompat.getColor(ctx,  R.color.icon_tint_selected)
        set(value) {
            field = value
            notifyPropertyChanged(BR.iconColorFilter)
        }

    val onClick: RippleView.OnRippleCompleteListener
        get() = RippleView.OnRippleCompleteListener {
            contract.onStorySelect(model)
        }
}

@BindingAdapter("clickComplete")
fun setRippleListener(view: RippleView, listener: RippleView.OnRippleCompleteListener) {
    view.setOnRippleCompleteListener(listener)
}