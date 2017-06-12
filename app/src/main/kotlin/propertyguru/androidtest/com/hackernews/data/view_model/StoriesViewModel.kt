package propertyguru.androidtest.com.hackernews.data.view_model

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.BindingAdapter
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout
import propertyguru.androidtest.com.hackernews.BR
import propertyguru.androidtest.com.hackernews.R
import propertyguru.androidtest.com.hackernews.ui.adapters.StoriesAdapter
import propertyguru.androidtest.com.hackernews.ui.helpers.StoryDivider

/**
 * Created by archie on 8/6/17.
 */
class StoriesViewModel(ctx: Context): ProgressListViewModel(ctx) {

    interface Contract {
        fun onRefresh()
    }

    var contract: Contract = object: Contract {
        override fun onRefresh() {}
    }

    val refreshListener: WaveSwipeRefreshLayout.OnRefreshListener
            = WaveSwipeRefreshLayout.OnRefreshListener {
        refreshing = true
        contract.onRefresh()
    }

    @get:Bindable
    var refreshing: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.refreshing)
        }

    @get:Bindable
    var adapter: StoriesAdapter? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.adapter)
        }

    @get:Bindable
    var storyDecoration: RecyclerView.ItemDecoration = StoryDivider(ctx, LinearLayoutManager.VERTICAL)
        set(value) {
            field = value
            notifyPropertyChanged(BR.storyDecoration)
        }


    var loadingColor: Int = ContextCompat.getColor(ctx, R.color.colorPrimaryDark)
}

@BindingAdapter("itemDecoration")
fun setItemDecoration(view: RecyclerView, decoration: RecyclerView.ItemDecoration) {
    view.addItemDecoration(decoration)
}

@BindingAdapter("loadingColor")
fun setWaveSwipeRefreshLayoutColor(view: WaveSwipeRefreshLayout, color: Int) {
    view.setColorSchemeColors(Color.WHITE)
    view.setWaveColor(color)
}
