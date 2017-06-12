package propertyguru.androidtest.com.hackernews.data.view_model

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.BindingAdapter
import android.opengl.Visibility
import android.support.design.widget.AppBarLayout
import android.view.View
import android.widget.ArrayAdapter
import propertyguru.androidtest.com.hackernews.BR
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.ui.adapters.CommentsAdapter

/**
 * Created by archie on 8/6/17.
 */
class CommentsViewModel(ctx: Context): ProgressListViewModel(ctx) {

    interface Contract {
        fun onReadStoryClick(url: String)
        fun onCommentLevelChanged(level: Int)
        fun onViewStoryVisibilityChanged(visibility: Int)
    }

    private var isToolbarShow = false
    private var toolbarScrollRange = -1

    val onClick: View.OnClickListener = View.OnClickListener {
        contract.onReadStoryClick(url)
    }

    var url: String = ""
    var contract: Contract = object: Contract {
        override fun onReadStoryClick(url: String) {}
        override fun onCommentLevelChanged(level: Int) {}
        override fun onViewStoryVisibilityChanged(visibility: Int) {}
    }

    @get:Bindable
    var loadingVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            notifyPropertyChanged(BR.loadingVisibility)
        }

    @get:Bindable
    var viewStoryVisibility: Int = View.VISIBLE
        set(value) {
            field = value
            notifyPropertyChanged(BR.viewStoryVisibility)
            viewStoryMenuButtonVisibility = value == View.VISIBLE
        }

    var viewStoryMenuButtonVisibility: Boolean = true
        set(value) {
            field = value
            contract.onViewStoryVisibilityChanged(if (value) View.VISIBLE else View.GONE)
        }

            @get:Bindable
    var commentsLevelAdapter: ArrayAdapter<String> = ArrayAdapter(ctx,
            android.R.layout.simple_spinner_dropdown_item)
        set(value) {
            field = value
            notifyPropertyChanged(BR.commentsLevelAdapter)
        }

    @get:Bindable
    var adapter: CommentsAdapter? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.adapter)
        }

    @get:Bindable
    var currentCommentLevel: Int = 0
        set (value) {
            field = value
            notifyPropertyChanged(BR.currentCommentLevel)
            contract.onCommentLevelChanged(value)
        }

}