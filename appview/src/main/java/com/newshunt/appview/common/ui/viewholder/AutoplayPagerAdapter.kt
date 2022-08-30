/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * Base class of Autoplay Carousel Adapter
 *
 * Created  on 22/10/19.
 */
abstract class AutoplayPagerAdapter<VH : AutoplayPagerAdapter.ViewHolder> : PagerAdapter() {

    internal var destroyedItems: LinkedHashMap<Int, VH> = LinkedHashMap()

    abstract class ViewHolder(internal val itemView1: View, override val uniqueScreenId: Int,
                              override val section:String, override val referrer: PageReferrer?):
        SCVViewHolder(itemView1, uniqueScreenId, section, referrer)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var viewHolder: VH? = destroyedItems.get(position)
        if (viewHolder != null) {
            // Re-add existing view before rendering so that we can make change inside getView()
            container.addView(viewHolder.itemView1)
            onBindViewHolder(viewHolder, position)
        } else {
            viewHolder = onCreateViewHolder(container, position)
            onBindViewHolder(viewHolder, position)
            container.addView(viewHolder.itemView1)
        }
        viewHolder.itemView1.tag = "item$position"
        return viewHolder
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView((`object` as VH).itemView1)
        destroyedItems.put(position, `object`)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as VH).itemView1 == view
    }

    /**
     * Create a new view holder
     * @param parent
     * @return view holder
     */
    abstract fun onCreateViewHolder(parent: ViewGroup, pos: Int): VH

    /**
     * Bind data at position into viewHolder
     * @param viewHolder
     * @param position
     */
    abstract fun onBindViewHolder(viewHolder: VH, position: Int)
}