/*
 * Copyright (c) 2020 . All rights reserved.
 */

package com.newshunt.news.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.LayoutFeedCommentRepostItemBindingImpl
import com.newshunt.dataentity.common.asset.CommonAsset

/**
 * Created by helly.patel on 3/6/20.
 */

class CommentsRepostFeedCardAdapter(val vm: CardsViewModel?) :
        RecyclerView.Adapter<CommentsRepostFeedCardsViewHolder>() {

    private var items: List<CommonAsset>? = null
    private var parent: CommonAsset? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsRepostFeedCardsViewHolder {
        val viewBinding = DataBindingUtil.inflate<LayoutFeedCommentRepostItemBindingImpl>(LayoutInflater.from(parent.context),
                R.layout.layout_feed_comment_repost_item, parent, false)
        viewBinding.setVariable(BR.vm, vm)
        return CommentsRepostFeedCardsViewHolder(viewBinding)
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: CommentsRepostFeedCardsViewHolder, position: Int) {
        val cardItem = items?.get(position)
        (holder as? CommentsRepostFeedCardsViewHolder)?.bind(cardItem)
    }

    fun setItems(data: List<CommonAsset>?) {
        this.items = data
        notifyDataSetChanged()
    }

    fun setParent(data: CommonAsset?) {
        this.parent = data

    }
}

class CommentsRepostFeedCardsViewHolder(val view: LayoutFeedCommentRepostItemBindingImpl) : RecyclerView.ViewHolder(view.root) {

    fun bind(cardItem: CommonAsset?) {
        view.setVariable(BR.item, cardItem)
        view.executePendingBindings()
    }
}