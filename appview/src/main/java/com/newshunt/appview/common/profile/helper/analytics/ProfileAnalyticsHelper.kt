/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.helper.analytics

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.model.entity.ProfileTabType
import com.newshunt.dataentity.model.entity.UserProfile
import com.newshunt.dataentity.news.analytics.NHProfileAnalyticsEvent
import com.newshunt.dataentity.news.analytics.NHProfileAnalyticsEventParam
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.dataentity.news.model.entity.server.asset.GroupType
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.socialfeatures.helper.analytics.NHAnalyticsSocialCommentsEventParam

/**
 * @author santhosh.kc
 */

fun logProfileViewedEvent(profileViewed: UserProfile, signedIn: Boolean,
                          signedUserId: String?,
                          pageReferrer: PageReferrer?,
                          section: NhAnalyticsEventSection = NhAnalyticsEventSection.PROFILE,
                          isFPV: Boolean, referrerRaw: String?, autoFollowFromNotification: Boolean = false) {

    pageReferrer ?: return
    val map = HashMap<NhAnalyticsEventParam, Any?>()
    fillUserProfileInfoAnalyticsParams(profileViewed, signedIn, signedUserId, map, isFPV, referrerRaw)
    if (autoFollowFromNotification) {
        map[NHProfileAnalyticsEventParam.NOTIF_CLICK]  = "follow"
    }
    AnalyticsClient.logDynamic(NHProfileAnalyticsEvent.PROFILE_VIEW, section, map, null, pageReferrer, false)
}

fun logHistoryListViewEvent(pageReferrer: PageReferrer?,
                            referrerProviderlistener: ReferrerProviderlistener?) {
    val map = HashMap<NhAnalyticsEventParam, Any?>()

    map[NhAnalyticsNewsEventParam.TABTYPE] = ProfileTabType.HISTORY.name
    map[NhAnalyticsNewsEventParam.TABITEM_ID] = ProfileTabType.HISTORY.name.toLowerCase()

    referrerProviderlistener?.extraAnalyticsParams?.let {
        map.putAll(it)
    }
    AnalyticsClient.logDynamic(NhAnalyticsNewsEvent.STORY_LIST_VIEW,
            referrerProviderlistener?.referrerEventSection ?: NhAnalyticsEventSection.PROFILE,
            map, null, pageReferrer, false)
}


fun logSavedStoriesListViewEvent(pageReferrer: PageReferrer?,
                            referrerProviderlistener: ReferrerProviderlistener?) {
    val map = HashMap<NhAnalyticsEventParam, Any?>()

    map[NhAnalyticsNewsEventParam.TABTYPE] = PageType.PROFILE_SAVED_DETAIL.pageType
    map[NhAnalyticsNewsEventParam.TABITEM_ID] = PageType.PROFILE_SAVED_DETAIL.pageType.toLowerCase()
    map[AnalyticsParam.GROUP_TYPE] = GroupType.TYPE_NEWS
    AnalyticsClient.logDynamic(NhAnalyticsNewsEvent.STORY_LIST_VIEW,
            referrerProviderlistener?.referrerEventSection ?: NhAnalyticsEventSection.PROFILE,
            map, null, pageReferrer, false)
}

fun getCommonEventParamsForEntity(isFPV: Boolean): MutableMap<NhAnalyticsEventParam, Any?> {
    return mutableMapOf(
            NHProfileAnalyticsEventParam.PROFILE_VIEW_TYPE to if (isFPV) Constants.FPV else Constants.TPV
    )
}


fun fillUserProfileInfoAnalyticsParams(userProfile: UserProfile, signedIn: Boolean,
                                       signedUserId: String?,
                                       map: HashMap<NhAnalyticsEventParam, Any?>,
                                       isFPV: Boolean, referrerRaw: String?) {
    map[NHProfileAnalyticsEventParam.PROFILE_VIEW_TYPE] = if (isFPV) Constants.FPV else Constants.TPV
    map[NHProfileAnalyticsEventParam.USER_ID] = signedUserId
    if (!isFPV) {
        map[NHProfileAnalyticsEventParam.TPV_STATE] = if (userProfile.isPrivateProfile())
            Constants.PROFILE_PRIVATE else Constants.PROFILE_PUBLIC
        map[NHProfileAnalyticsEventParam.TARGET_USER_ID] = userProfile.userId
        map[NHProfileAnalyticsEventParam.TARGET_USER_TYPE] = if (userProfile.isCreator()) Constants.CREATOR else Constants.USER
    }
    if (referrerRaw != null) {
        AnalyticsHelper2.appendReferrerRaw(map, referrerRaw)
    }
}

fun logProfile3DotsMenuViewedEvent(isFPV: Boolean,
                                   referrer: PageReferrer?) {
    val map = HashMap<NhAnalyticsEventParam, Any?>()
    map.putAll(getCommonEventParamsForEntity(isFPV = isFPV))
    AnalyticsClient.logDynamic(NHProfileAnalyticsEvent.DIALOGBOX_VIEWED,
            NhAnalyticsEventSection.PROFILE,
            map,
            null,
            referrer,
            false)
}

fun logProfile3DotsMenuActionEvent(isFPV: Boolean,
                                   referrer: PageReferrer?,
                                   actionString: String) {
    val map = HashMap<NhAnalyticsEventParam, Any?>()
    map.putAll(getCommonEventParamsForEntity(isFPV = isFPV))
    map[NHAnalyticsSocialCommentsEventParam.TYPE] = actionString
    AnalyticsClient.logDynamic(NHProfileAnalyticsEvent.DIALOGBOX_ACTION,
            NhAnalyticsEventSection.PROFILE,
            map,
            null,
            referrer,
            false)
}