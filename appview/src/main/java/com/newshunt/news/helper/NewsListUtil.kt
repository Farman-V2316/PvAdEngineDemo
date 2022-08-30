/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.view.ViewGroup
import android.widget.LinearLayout
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.appview.R
import com.newshunt.dataentity.news.model.entity.PageType

/**
 * @author santhosh.kc
 */
fun getMoreNewsToolTip(newsStoriesCount: Int, showCount: Boolean, pageReferrer: PageReferrer?,
                       pageTypeStr: String?, logTag: String): String? {

    val pageType = PageType.fromName(pageTypeStr)
    pageType ?: return null

    if (pageType == PageType.INVALID) {
        return null
    }

    return if (CommonNavigator.isProfileReferrer(pageReferrer)) {
        getMoreNewsToolForProfile(pageType)
    } else if (showCount && newsStoriesCount <= 0) {
        Logger.e(logTag, "showMoreStoriesToolTip: not showing $newsStoriesCount items")
        null
    } else {
        val isViralTab = PageType.VIRAL == pageType

        val pluralResourceId = if (isViralTab) R.plurals.viral_more_stories else R.plurals.new_stories_top
        val resourceId = if (isViralTab) R.string.viral_more_news_top else R.string.more_news_top

        if (showCount)
            CommonUtils.getQuantifiedString(pluralResourceId, newsStoriesCount, newsStoriesCount)
        else
            CommonUtils.getString(resourceId)
    }
}

private fun getMoreNewsToolForProfile(pageType: PageType) : String? {
    return when(pageType) {
        PageType.PROFILE_ACTIVITY -> CommonUtils.getString(R.string.more_activities)
        PageType.PROFILE_TPV_RESPONSES -> CommonUtils.getString(R.string.more_responses)
        PageType.PROFILE_MY_POSTS, PageType.PROFILE_TPV_POSTS -> CommonUtils.getString(R.string.more_posts)
        PageType.PROFILE_SAVED, PageType.PROFILE_SAVED_DETAIL -> CommonUtils.getString(R.string.more_saved_stories)
        else -> CommonUtils.getString(R.string.more_news_top)
    }
}

fun showHeaderView(pageType: PageType?) : Boolean {
    return when(pageType) {
        null, PageType.FOLLOW, PageType.EXPLORE, PageType.PROFILE_ACTIVITY, PageType
                .PROFILE_TPV_RESPONSES, PageType.PROFILE_SAVED, PageType.PROFILE_SAVED_DETAIL -> false
        else -> return true
    }
}

fun showNetworkErrorToast(pageType: PageType?) : Boolean {
    return when(pageType) {
        null, PageType.FOLLOW, PageType.EXPLORE, PageType.PROFILE_ACTIVITY, PageType
                .PROFILE_TPV_RESPONSES, PageType.PROFILE_SAVED, PageType.PROFILE_SAVED_DETAIL -> false
        else -> return true
    }
}

fun getFullPageErrorContentLayoutParams(pageType: PageType?): LinearLayout.LayoutParams? {
    return when (pageType) {
        PageType.FOLLOW, PageType.EXPLORE, PageType.PROFILE_ACTIVITY, PageType
                .PROFILE_TPV_RESPONSES, PageType.PROFILE_SAVED, PageType.PROFILE_SAVED_DETAIL -> {
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT)
            params.topMargin = CommonUtils.getDimension(R.dimen.profile_tab_full_page_error_marginTop)
            params
        }
        else -> return null
    }
}