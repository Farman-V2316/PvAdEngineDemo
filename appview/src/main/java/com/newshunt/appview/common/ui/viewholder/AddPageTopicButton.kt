/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.common.helper.listener.RecyclerViewOnItemClickListener
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.pages.PageableTopicsEntity
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.news.helper.NewsExploreButtonType
import com.newshunt.news.util.NewsConstants

/**
 * ViewHolder for button in Add Page activity.
 *
 * @author aman.roy
 */
class AddPageTopicButton(private val context: Context, private val view: View,
                         private val viewOnItemClickListener: RecyclerViewOnItemClickListener) : RecyclerView.ViewHolder(view), View.OnClickListener {

    private lateinit var pageableTopicsEntity: PageableTopicsEntity
    private val topicButton: NHTextView = view.findViewById(R.id.topic_button)
    init {
        view.setOnClickListener(this)
    }

    override fun onClick(v: View) {

        // Logging the ExploreButtonClickEvent by default provides News Section.
        AnalyticsHelper2.logExploreButtonClickEvent(PageReferrer(NewsReferrer.LOCATION_SELECTION_PAGE),
                NewsExploreButtonType.PLUS_SECTION_LOCATIONS,"")
        val intent = Intent(DHConstants.OPEN_LOCATION_SELECTION)
        intent.setPackage(AppConfig.getInstance().packageName)
//        This will hide the Next Button on Location Selection Page.
        intent.putExtra(Constants.BUNDLE_LAUNCHED_FROM_SETTINGS, true)
        ( context as Activity).startActivityForResult(intent, NewsConstants
                .REQUEST_CODE_LOCATION_SEARCH)
    }

    fun updateButton(text :String) {
        topicButton.text = text
    }

}
