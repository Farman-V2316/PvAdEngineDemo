/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.adapter

import android.os.Bundle
import androidx.paging.PagedList
import androidx.paging.PagedListWrapperAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.view.helper.AdBinderRepo
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.Format

/**
 *@author amit.chaudhary
 * */
abstract class CustomPagedAdapter<T, VH : RecyclerView.ViewHolder>(
        diffCallback: DiffUtil.ItemCallback<T>) : PagedListWrapperAdapter<T, VH>(diffCallback) {

    private val footer: MutableList<FooterPriorityHolder<T>> = mutableListOf()
    private val adsReplaceMapping: MutableMap<String, String> = mutableMapOf()
    private var updateId = 0
    private var updateRunning = false

    override fun getItem(position: Int): T? {
        val extraPosition = position - super.getItemCount()
        if (extraPosition >= 0 && extraPosition < footer.size) {
            return footer[extraPosition].data
        }
        if (position < super.getItemCount() && position >= 0) {
            val originalItem = super.getItem(position)
            if (originalItem is CommonAsset && originalItem.i_format() == Format.AD &&
                    originalItem !is BaseAdEntity) {
                val adId = adsReplaceMapping[originalItem.i_id()] ?: originalItem.i_id()
                return AdBinderRepo.getAdById(adId) as T?
            } else {
                return originalItem
            }
        } else {
            return null
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + footer.size
    }

    fun getOriginalItemCount(): Int {
        return super.getItemCount()
    }

    /**
     * Replace the adEntity from this list. Cases like video ad failure.
     *
     * @param old old ad
     * @param new new Ad
     * @return replace success
     */
    fun replaceAdsMapping(old: T, new: T) : Boolean {
        if (old is BaseAdEntity && new is BaseAdEntity) {
            adsReplaceMapping[old.i_id()] = new.i_id()
            val positionChanged = getSnapshot()?.indexOfFirst { it is CommonAsset && it.i_id() == old.i_id() }
            if (positionChanged != null && positionChanged > 0) {
                notifyItemChanged(positionChanged, Bundle())
            }
            return true
        }
        return false
    }

    fun showFooterItem(item: T?, positionPriority: Int) {
        val footerPosition = super.getItemCount()
        val oldSize = footer.size
        val oldFooter = footer.find { it.priority == positionPriority }
        footer.remove(oldFooter)
        if (item != null) {
            footer.add(FooterPriorityHolder(positionPriority, item))
        }
        val newSize = footer.size
        if (newSize < oldSize) {
            notifyItemRangeChanged(footerPosition, newSize)
            notifyItemRangeRemoved(newSize + footerPosition, oldSize - newSize)
        } else if (newSize > oldSize) {
            notifyItemRangeChanged(footerPosition, oldSize)
            notifyItemRangeInserted(oldSize + footerPosition, newSize - oldSize)
        } else {
            notifyItemRangeChanged(footerPosition, oldSize)
        }
    }

    override fun submitList(pagedList: PagedList<T>?, commitCallback: Runnable?, clearExtra: Boolean) {
        updateId++
        val id = updateId
        updateRunning = true
        super.submitList(pagedList, Runnable {
            if (id == updateId) {
                updateRunning = false
            }
            commitCallback?.run()

        }, clearExtra)
    }
}

data class FooterPriorityHolder<T>(val priority: Int,
                                   val data: T)