package propertyguru.androidtest.com.hackernews.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.data.view_model.StoryViewModel
import propertyguru.androidtest.com.hackernews.databinding.StoryItemRowBinding

/**
 * Created by archie on 10/6/17.
 */

class StoriesAdapter(private val context: Context,
                     private val contract: StoryViewModel.Contract,
                     private val inflater: LayoutInflater = LayoutInflater.from(context)) :
                        ItemsAdapter<StoriesAdapter.StoryViewHolder, Story>() {

    class StoryViewHolder(val binding: StoryItemRowBinding,
                          val contract:  StoryViewModel.Contract,
                          var viewModel: StoryViewModel): RecyclerView.ViewHolder(binding.root),
            ItemsAdapter.ViewHolder<Story>{

        override fun bindView(story: Story) {
            viewModel.contract = contract
            binding.viewModel = viewModel
            viewModel.model = story
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): StoryViewHolder {
        return StoryViewHolder(StoryItemRowBinding.inflate(inflater, parent, false),
                contract,
                StoryViewModel(context))
    }

}