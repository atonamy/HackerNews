package propertyguru.androidtest.com.hackernews.ui.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.realm.RealmList
import io.realm.RealmObject
import propertyguru.androidtest.com.hackernews.data.model.Story
import propertyguru.androidtest.com.hackernews.data.view_model.StoryViewModel
import propertyguru.androidtest.com.hackernews.databinding.StoryItemRowBinding

/**
 * Created by archie on 8/6/17.
 */
abstract class ItemsAdapter<T, M> (private val items: MutableList<M> = RealmList<M>()):
        RecyclerView.Adapter<T>() where T: RecyclerView.ViewHolder, T: ItemsAdapter.ViewHolder<M>,
    M: RealmObject {

    interface ViewHolder<T> where T: RealmObject {
        fun bindView(item: T)
    }

    fun addItems(items: List<M>) {
        if(items.size == 0)
            return
        val position = this.items.size
        this.items.addAll(position, items)
        notifyItemRangeInserted(position, items.size)
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: T?, position: Int) {
        val item = getItem(position)
        if(item != null) {
            holder?.bindView(item)
        }
    }

    fun getItem(position: Int): M? {
        if(position >= 0 && position < items.size)
            return items.get(position);
        return null;
    }

}