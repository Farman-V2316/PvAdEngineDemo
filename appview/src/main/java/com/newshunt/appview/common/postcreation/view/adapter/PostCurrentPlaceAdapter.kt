package com.newshunt.appview.common.postcreation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.databinding.PostCreateLocationHeaderItemVhBinding
import com.newshunt.appview.databinding.PostCreateLocationItemVhBinding
import com.newshunt.common.view.customview.HeaderRecyclerViewAdapter
import com.newshunt.dataentity.common.asset.PostCurrentPlace


class PostCurrentPlaceAdapter(private val clickListener: PostCurrentPlaceClickListener?) :
    HeaderRecyclerViewAdapter() {

    val dataItems = mutableListOf<PostCurrentPlace>()

    override fun useHeader(): Boolean = false

    override fun onCreateHeaderViewHolder(parent: ViewGroup?, viewType: Int)
            : RecyclerView.ViewHolder {
        TODO("No implementation")
    }

    override fun onBindHeaderView(holder: RecyclerView.ViewHolder?, position: Int) {
    }

    override fun useFooter(): Boolean = true

    override fun onCreateFooterViewHolder(parent: ViewGroup?, viewType: Int)
            : RecyclerView.ViewHolder {
        val rootView = LayoutInflater.from(parent?.context)
            .inflate(R.layout.post_create_location_google_attribution, parent, false)
        return PostCreationFooterImageVH(rootView)
    }

    override fun onBindFooterView(holder: RecyclerView.ViewHolder?, position: Int) {

    }

    override fun onCreateBasicItemViewHolder(parent: ViewGroup?, viewType: Int)
            : RecyclerView.ViewHolder {
        return getViewHolder(viewType, parent!!, clickListener)
    }

    override fun onBindBasicItemView(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as? PostCreationCurrentLocationVH)?.bind(dataItems[position])
    }

    override fun getBasicItemCount(): Int {
        return dataItems.size
    }

    override fun getBasicItemType(position: Int): Int {
        return findCardTypeIndex(position, dataItems[position])
    }

}

interface PostCurrentPlaceClickListener {
    fun onCurrentPlaceItemClick(data: PostCurrentPlace)
    fun onLocationDeleted()
}

interface UpdatableLocationView {
    fun bind(item: Any)
}

private fun getViewHolder(
    displayCardTypeIndex: Int, parent: ViewGroup, callback: PostCurrentPlaceClickListener?
): RecyclerView.ViewHolder {
    val viewDataBinding: ViewDataBinding = getViewBinding(displayCardTypeIndex, parent)
    return PostCreationCurrentLocationVH(viewDataBinding, callback)
}

fun getViewBinding(displayCardTypeIndex: Int, parent: ViewGroup): ViewDataBinding {
    val layoutInflater = LayoutInflater.from(parent.context)
    return when (displayCardTypeIndex) {
        0 -> {
            DataBindingUtil.inflate<PostCreateLocationHeaderItemVhBinding>(
                layoutInflater, R.layout.post_create_location_header_item_vh, parent, false
            )
        }

        else -> {
            DataBindingUtil.inflate<PostCreateLocationItemVhBinding>(
                layoutInflater, R.layout.post_create_location_item_vh, parent, false
            )
        }
    }

}

class PostCreationCurrentLocationVH(
    private val viewBinding: ViewDataBinding,
    private val callback: PostCurrentPlaceClickListener?
) : RecyclerView.ViewHolder(viewBinding.root), UpdatableLocationView {

    override fun bind(item: Any) {
        if (item !is PostCurrentPlace) return
        itemView.setOnClickListener { callback?.onCurrentPlaceItemClick(item) }
        viewBinding.setVariable(BR.item, item)
        viewBinding.setVariable(BR.callback, callback)
        viewBinding.executePendingBindings()
    }
}

class PostCreationFooterImageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

}

enum class PostLocDisplayType(val index: Int) {
    USER_SELECTED(0),
    DEFAULT(1)
}

private fun findCardTypeIndex(pos: Int, asset: Any): Int {
    if (asset is PostCurrentPlace && asset.isAutoLocation.not()) {
        return when (pos) {
            0 -> PostLocDisplayType.USER_SELECTED.index
            else -> PostLocDisplayType.DEFAULT.index
        }
    }
    return PostLocDisplayType.DEFAULT.index
}