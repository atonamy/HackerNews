package propertyguru.androidtest.com.hackernews.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import propertyguru.androidtest.com.hackernews.data.model.Comment
import propertyguru.androidtest.com.hackernews.data.view_model.CommentViewModel
import propertyguru.androidtest.com.hackernews.databinding.CommentItemRowBinding

/**
 * Created by archie on 8/6/17.
 */
class CommentsAdapter(private val context: Context,
                      private val contract: CommentViewModel.Contract,
                      private val inflater: LayoutInflater = LayoutInflater.from(context)):
        ItemsAdapter<CommentsAdapter.CommentViewHolder, Comment>(){

    class CommentViewHolder(val binding: CommentItemRowBinding,
                            val contract:  CommentViewModel.Contract,
                            var viewModel: CommentViewModel): RecyclerView.ViewHolder(binding.root),
            ItemsAdapter.ViewHolder<Comment>{

        override fun bindView(comment: Comment) {
            viewModel.contract = contract
            binding.viewModel = viewModel
            viewModel.model = comment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CommentViewHolder {
        return CommentViewHolder(CommentItemRowBinding.inflate(inflater, parent, false),
                contract,
                CommentViewModel())
    }
}