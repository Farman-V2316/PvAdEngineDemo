/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.ui.viewholder.AbstractAutoplayViewHolder
import com.newshunt.appview.common.ui.viewholder.CardsViewHolder
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.PostDisplayType

/**
 * Helper class to identify the views to animate from list to detail
 * <p>
 * Created by srikanth.ramaswamy on 08/17/2022
 */
object ListToDetailTransitionHelper {
    //For each view type, find the main image view and animate that from list to detail
    private val animationViewIdMap = mapOf(PostDisplayType.IMAGES_5.index to R.id.gallery5_photo_grp,
        PostDisplayType.SIMPLE_POST_LOW.index to R.id.image,
        PostDisplayType.IMAGES_2.index to R.id.gallery2_photo_grp,
        PostDisplayType.IMAGES_4.index to R.id.gallery4_photo_grp,
        PostDisplayType.IMAGES_3.index to R.id.gallery3_photo_grp,
        PostDisplayType.VIRAL.index to R.id.image,
        PostDisplayType.SIMPLE_POST_DYNAMIC_HERO.index to R.id.news_image,
    )

    fun findViewForAnimation(viewHolder: RecyclerView.ViewHolder, parentStoryId: String, childStoryId: String): View {
        when (viewHolder) {
            is AbstractAutoplayViewHolder -> viewHolder.getSharedElementView()
            is CollectionViewHolder -> {
                return viewHolder.getViewForAnimationByItemId(childStoryId) ?: run {
                    Logger.e(NavigationHelper.FRAGMENT_TRANSITION_TAG, "Could not find the clicked image view for ${viewHolder.javaClass.simpleName}")
                    viewHolder.itemView
                }
            }
            else -> {
                (viewHolder as? CardsViewHolder)?.let { cardsViewHolder ->
                    val viewId = animationViewIdMap[cardsViewHolder.displayTypeIndex] ?: -1
                    if (viewId != -1) {
                        Logger.d(NavigationHelper.FRAGMENT_TRANSITION_TAG, "Finding right view to animate for displayTypeIndex: ${cardsViewHolder.displayTypeIndex}, viewHolder: ${cardsViewHolder.javaClass.simpleName}")
                        return viewHolder.itemView.findViewById(viewId) ?: kotlin.run {
                            Logger.e(NavigationHelper.FRAGMENT_TRANSITION_TAG, "Could not find image view for animation. Display type: ${cardsViewHolder.displayTypeIndex}, viewId: $viewId, viewHolder: ${cardsViewHolder.javaClass.simpleName}")
                            viewHolder.itemView
                        }
                    } else {
                        Logger.e(NavigationHelper.FRAGMENT_TRANSITION_TAG, "Could not find view id for Display type: ${cardsViewHolder.displayTypeIndex}")
                    }
                }
            }
        }
        Logger.d(NavigationHelper.FRAGMENT_TRANSITION_TAG, "Returning default itemview for ${viewHolder.javaClass.simpleName}")
        return viewHolder.itemView
    }
}