/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import com.newshunt.dhutil.analytics.NhAnalyticsCampaignEventParam
import junit.framework.Assert
import org.junit.Test

/**
 * Created by anshul on 02/04/18.
 * A class with test functions for utmParamsHelper
 */
class UtmParamsHelperTest {

    @Test
    fun hello() {
        Assert.assertEquals(true, true)
    }

    @Test
    fun `getReferrerRaw - pass valid keys and get a valid string back `() {
        val map = mapOf<String, String>(
            NhAnalyticsCampaignEventParam.UTM_SOURCE.name to "source",
            NhAnalyticsCampaignEventParam.UTM_MEDIUM.name to "medium",
            NhAnalyticsCampaignEventParam.UTM_CAMPAIGN.name to "campaign",
            NhAnalyticsCampaignEventParam.UTM_TERM.name to "term",
            NhAnalyticsCampaignEventParam.UTM_CONTENT.name to "content"
        )

        val returnedString = UtmParamsHelper.getReferrerRaw(map)
        val expectedString = "deeplink_utm_source=source&utm_medium=medium&utm_campaign=campaign" +
                "&utm_term=term&utm_content=content"
        Assert.assertEquals(expectedString, returnedString)
    }

    @Test
    fun `getReferrerRaw - pass some invalid keys and get a valid string back `() {
        val map = mapOf<String, String>(
            NhAnalyticsCampaignEventParam.UTM_SOURCE.name to "source",
            NhAnalyticsCampaignEventParam.UTM_MEDIUM.name to "medium",
            NhAnalyticsCampaignEventParam.UTM_CAMPAIGN.name to "campaign",
            NhAnalyticsCampaignEventParam.UTM_TERM.name to "term",
            NhAnalyticsCampaignEventParam.UTM_CONTENT.name to "content",
            "invalidKey" to "invalidValue",
            NhAnalyticsCampaignEventParam.URLREFERRER.name to "urlReferrer"
        )

        val returnedString = UtmParamsHelper.getReferrerRaw(map)
        val expectedString = "deeplink_utm_source=source&utm_medium=medium&utm_campaign=campaign" +
                "&utm_term=term&utm_content=content&urlreferrer=urlReferrer"
        Assert.assertEquals(expectedString, returnedString)
    }
}