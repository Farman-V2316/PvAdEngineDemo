/**
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.analytics

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer

/**
 * @author santhosh.kc
 */
public enum class FollowReferrer(private val referrerName: String,
                          private val referrerSource: NHReferrerSource) : NhAnalyticsReferrer {
    FE_SOURCE("fe_source", FollowReferrerSource.FOLLOW_HOME_VIEW),
    FE_TOPIC("fe_topic", FollowReferrerSource.FOLLOW_HOME_VIEW),
    FE_LOCATION("fe_location", FollowReferrerSource.FOLLOW_HOME_VIEW),
    FF_SOURCE("ff_source", FollowReferrerSource.FOLLOWED_ENTITIES_HOME),
    FF_TOPIC("ff_topic", FollowReferrerSource.FOLLOWED_ENTITIES_HOME),
    FF_LOCATION("ff_location", FollowReferrerSource.FOLLOWED_ENTITIES_HOME),
    FOLLOW_HOME("FOLLOW_HOME",FollowReferrerSource.FOLLOW_HOME_VIEW),
    FOLLOW_SEE_ALL("follow_see_all",FollowReferrerSource.FOLLOW_ENTITY_LIST_VIEW),
    FOLLOWING_ALL("following_all",FollowReferrerSource.FOLLOWED_ENTITIES_HOME);

    override fun getReferrerName(): String {
        return referrerName
    }

    override fun getReferrerSource(): NHReferrerSource {
        return referrerSource
    }
}

enum class FollowReferrerSource : NHReferrerSource {
    FOLLOW_HOME_VIEW,
    FOLLOWED_ENTITIES_HOME,
    FOLLOW_ENTITY_LIST_VIEW,
    FOLLOW_EXPLORE_VIEW
}