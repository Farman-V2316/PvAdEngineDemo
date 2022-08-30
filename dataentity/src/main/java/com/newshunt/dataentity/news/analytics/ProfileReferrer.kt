package com.newshunt.dataentity.news.analytics

import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer

/**
 * @author santhosh.kc
 */
enum class ProfileReferrer(private val referrerName: String,
                           private val referrerSource: NHReferrerSource) : NhAnalyticsReferrer {
    ACTIVITY("activity", ProfileReferrerSource.PROFILE_HOME_VIEW),
    SAVED("saved", ProfileReferrerSource.PROFILE_HOME_VIEW),
    HISTORY("history", ProfileReferrerSource.PROFILE_HOME_VIEW),
    TPV_RESPONSES("tpv_responses", ProfileReferrerSource.PROFILE_HOME_VIEW),
    MY_POSTS("my_posts", ProfileReferrerSource.PROFILE_HOME_VIEW),
    TPV_POSTS("tpv_posts", ProfileReferrerSource.PROFILE_HOME_VIEW),
    PROFILE("profile", ProfileReferrerSource.PROFILE_HOME_VIEW),
    POSTS("posts", ProfileReferrerSource.PROFILE_HOME_VIEW),
    RESPONSES("responses", ProfileReferrerSource.PROFILE_HOME_VIEW);


    override fun getReferrerName(): String {
        return referrerName
    }

    override fun getReferrerSource(): NHReferrerSource {
        return referrerSource
    }
}

enum class ProfileReferrerSource : NHReferrerSource {
    PROFILE_HOME_VIEW ,
}