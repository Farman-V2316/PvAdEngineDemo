/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.listeners.LocationFollowClickListener
import com.newshunt.appview.common.viewmodel.PageableTopicViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.view.EntityImageUtils
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.util.NewsConstants

/**
 * ViewHolder for Selected / Suggested Location list in Add Page activity.
 *
 * @author aman.roy
 */
class AddPageLocationSimpleViewHolder(private val view: View,
                                      private val viewOnItemClickListener:
                                      RecyclerViewOnItemClickListener,
                                      private val pageableTopicViewModel: PageableTopicViewModel,
                                      private val locationFollowClickListener: LocationFollowClickListener
) : RecyclerView.ViewHolder(view), View.OnClickListener {
    private val topicTitle: NHTextView = view.findViewById(R.id.topic_title)
    private val isTopicFavorite: NHImageView = view.findViewById(R.id.topic_isfavorite)
    private val topicIcon: NHImageView = view.findViewById(R.id.alltopic_icon)
    private val isTopicFavoriteContainer: FrameLayout = view.findViewById(R.id.topic_isfavorite_container)

    private lateinit var location: Location

    init {

        isTopicFavoriteContainer.setOnClickListener(this)
        view.setOnClickListener(this)
    }

    fun updateTopic(location: Location) {
        this.location = location
        topicTitle.text = location.displayName ?: location.nameEnglish
        isTopicFavorite.setOnClickListener(this)
        isTopicFavorite.isSelected = location.isFollowed

        var iconImageUrl = location.entityImageUrl

        iconImageUrl?.let {
            iconImageUrl = ImageUrlReplacer.getQualifiedImageUrl(iconImageUrl, CommonUtils.getDimension(R.dimen
                    .alltopic_icon_w_h), CommonUtils.getDimension(R.dimen.alltopic_icon_w_h))
            EntityImageUtils.loadImage(iconImageUrl, location.nameEnglish, topicIcon,
                    R.drawable.default_group_thumbnail)
        }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.add_page_topic_list_item_container) {
            goToTopics()
        }

        if (v === isTopicFavoriteContainer || v === isTopicFavorite) {
            onTabAddedOrRemoved(!isTopicFavorite.isSelected)
        }
    }

    private fun goToTopics() {
        val intent = Intent(Constants.ENTITY_OPEN_ACTION)
        intent.setPackage(CommonUtils.getApplication().packageName)
        intent.putExtra(NewsConstants.ENTITY_KEY, location.id)
        intent.putExtra(NewsConstants.ENTITY_TYPE, location.entityType)
        viewOnItemClickListener.onItemClick(intent, adapterPosition)
    }

    private fun onTabAddedOrRemoved(isAdded: Boolean) {
        isTopicFavorite.isSelected = isAdded
        pageableTopicViewModel.onLocationFollowChanged(view, isAdded, location)
        locationFollowClickListener.followed(isAdded, location)
        location.isFollowed = isAdded

    }
}
