package propertyguru.androidtest.com.hackernews.data.view_model

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import propertyguru.androidtest.com.hackernews.BR
import propertyguru.androidtest.com.hackernews.ui.helpers.StoryDivider

/**
 * Created by archie on 10/6/17.
 */

open class ProgressListViewModel(protected val ctx: Context): BaseObservable() {

    @get:Bindable
    var maxProgress: Int = 100
        set(value) {
            field = value
            notifyPropertyChanged(BR.maxProgress)
        }

    @get:Bindable
    var progressVisibility: Int = View.GONE
        set(value) {
            field = value
            notifyPropertyChanged(BR.progressVisibility)
        }

    @get:Bindable
    var progress: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.progress)
        }

    @get:Bindable
    var layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(ctx)
        set(value) {
            field = value
            notifyPropertyChanged(BR.layoutManager)
        }

    @get:Bindable
    var itemsVisibility: Int = View.GONE
        set(value) {
            field = value
            notifyPropertyChanged(BR.itemsVisibility)
        }

    @get:Bindable
    var messageVisibility: Int = View.GONE
        set(value) {
            field = value
            notifyPropertyChanged(BR.messageVisibility)
        }
}