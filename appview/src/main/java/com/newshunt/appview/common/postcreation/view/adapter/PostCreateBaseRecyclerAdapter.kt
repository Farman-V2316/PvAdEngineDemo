/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.postcreation.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.common.helper.common.Logger


abstract class PostCreateBaseRecyclerAdapter<T, VH : RecyclerView.ViewHolder> :
    androidx.recyclerview.widget.RecyclerView.Adapter<VH>() {
    private var layoutInflator: LayoutInflater? = null;
    private val TAG = PostCreateBaseRecyclerAdapter::class.java.simpleName

    var dataItems = mutableListOf<T>()
        set(value) {
            if (value == null) return
            field.addAll(value)
        }
        get


    abstract fun createBasicItemViewHolder(
        layoutInflater: LayoutInflater?, viewGroup: ViewGroup?,
        viewType: Int
    ): VH

    abstract fun bindBasicItemViewHolder(viewHolder: VH, data: T?, position: Int)

    abstract fun getBasicItemType(position: Int): Int

    abstract fun uniqueIdItem(position: Int): Long

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        if (layoutInflator == null) {
            layoutInflator = LayoutInflater.from(parent.context)
        }
        Logger.d(TAG, "creating viewholder..  ")
        return createBasicItemViewHolder(layoutInflator, parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        Logger.d(TAG, "binding viewholder for position :: $position")
        bindBasicItemViewHolder(holder, getItemAtPosition(position), position)
    }

    fun getItemAtPosition(position: Int): T? {
        if (dataItems.size > 0) {
            try {
                return dataItems[position]
            } catch (e: Exception) {
                return null
            }
        } else {
            return null
        }
    }


    fun clearItems() {
        dataItems.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataItems.size
    }

    fun addItemAtPosition(position: Int, item: T) {
        if (position >= 0 && position <= dataItems.size) {
            dataItems.add(position, item)
            notifyItemInserted(position)
        }
    }

    fun removedItemAtPosition(position: Int): Boolean {
        if (position >= 0 && position < dataItems.size) {
            try {
                dataItems.removeAt(position)
                notifyItemRemoved(position)
                return true
            } catch (e: Exception) {
            }
        }
        return false
    }

    fun removeItemRange(startPos: Int, endPos: Int): Boolean {
        val iterator = dataItems.iterator()
        var index = 0

        if (startPos >= dataItems.size) {
            return true
        }

        while (iterator.hasNext()) {
            val item = iterator.next()
            if (index >= startPos) {
                iterator.remove()
            }
            index++
        }
        notifyItemRangeChanged(startPos, dataItems.size)
        return true
    }

    fun addItemRange(startPos: Int, collection: List<T>) {
        dataItems.addAll(collection)
        notifyItemRangeChanged(startPos, collection.size)
    }

    override fun getItemId(position: Int): Long {
        return uniqueIdItem(position)
    }

    override fun getItemViewType(position: Int): Int {
        return getBasicItemType(position)
    }

    fun getDataDiffUtilPar(): Pair<MutableList<T>, DiffUtil.DiffResult> {
        return Pair.create(dataItems, null)
    }

}