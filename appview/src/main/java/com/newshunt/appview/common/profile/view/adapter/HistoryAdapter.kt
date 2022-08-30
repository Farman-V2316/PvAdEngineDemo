/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.UpdateableCardView
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.appview.databinding.LayoutProfileActivityCardBinding
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.model.entity.HistoryEntity
import com.newshunt.dataentity.news.analytics.ProfileReferrer
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.LiveSharedPreference
import com.newshunt.news.view.ListEditInterface
import com.newshunt.news.view.viewholder.DateViewHolder
import com.newshunt.news.view.viewholder.UserInteractionViewHolder

private const val ITEM_TYPE_HISTORY = 12
private const val ITEM_TYPE_DATE = 13

/**
 * Adapter to show list of stories from history
 * <p>
 * Created by srikanth.ramaswamy on 04/17/2019.
 */
class HistoryAdapter(private val context: Context,
                     private val listEditInterface: ListEditInterface,
                     private val referrerProviderListener : ReferrerProviderlistener?,
                     private val clickHandlingViewModel: ClickHandlingViewModel? = null,
                     private val lifecycleOwner: LifecycleOwner? = null) : RecyclerView
.Adapter<RecyclerView.ViewHolder>() {
    var items :  List<Any>? = null
        set(value)  {
            DiffUtil.calculateDiff(HistoryDiffUtil(items, value)).dispatchUpdatesTo(this)
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)
        return when (viewType) {
            ITEM_TYPE_HISTORY -> {
                val viewBinding = DataBindingUtil.inflate<LayoutProfileActivityCardBinding>(inflater,
                        R.layout.layout_profile_activity_card,
                        parent,
                        false)
                viewBinding.setVariable(BR.vm, clickHandlingViewModel)
                UserInteractionViewHolder(viewBinding,
                        listEditInterface,
                        PageReferrer(ProfileReferrer.HISTORY),
                        LiveSharedPreference.pref(GenericAppStatePreference.SHOW_NSFW_FILTER, context, true),
                        referrerProviderListener)
            }
            else -> {
                DateViewHolder(inflater.inflate(R.layout.layout_profile_activity_group_date, parent, false))
            }
        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        items?.get(position)?.let {
            (holder as? UpdateableCardView?)?.bind(it, lifecycleOwner, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items?.get(position) is String) {
            ITEM_TYPE_DATE
        } else ITEM_TYPE_HISTORY
    }
}

class HistoryDiffUtil(val oldItems: List<Any>?, val newItems: List<Any>?) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (oldItems == null && newItems == null) return true

        oldItems ?: return false
        newItems ?: return false

        if (oldItems[oldItemPosition].javaClass != newItems[newItemPosition].javaClass) {
            return false
        }

        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        if (oldItem is HistoryEntity && newItem is HistoryEntity) {
            return oldItem.id == newItem.id
        } else if (oldItem is String && newItem is String) {
            return oldItem == newItem
        }

        return false
    }

    override fun getOldListSize(): Int {
        return oldItems?.size ?: 0
    }

    override fun getNewListSize(): Int {
        return newItems?.size ?: 0
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems?.get(oldItemPosition)
        val newItem = newItems?.get(newItemPosition)

        if(oldItem is HistoryEntity && newItem is HistoryEntity) {
            return oldItem == newItem
        }

        return true
    }
}