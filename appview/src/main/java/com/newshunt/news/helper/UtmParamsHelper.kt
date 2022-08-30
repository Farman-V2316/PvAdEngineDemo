/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.analytics.NhAnalyticsCampaignEventParam
import java.net.URLEncoder

/**
 * Created by anshul on 02/04/18.
 */
class UtmParamsHelper {

    companion object {

        private val UTM_PARAMS_SEPERATOR = "?"
        private val DEEPLINK_PREFLIX = "deeplink"
        private val DEEPLINK_SEPERATOR = "_"

        @JvmStatic
        fun getReferrerRaw(queryParamsMap: Map<String, String>): String {
            if (CommonUtils.isEmpty(queryParamsMap)) {
                return Constants.EMPTY_STRING
            }

            val filteredMap = queryParamsMap.filterKeys {
                it.equals(NhAnalyticsCampaignEventParam.UTM_SOURCE.name, true) ||
                        it.equals(NhAnalyticsCampaignEventParam.UTM_CAMPAIGN.name, true) ||
                        it.equals(NhAnalyticsCampaignEventParam.UTM_MEDIUM.name, true) ||
                        it.equals(NhAnalyticsCampaignEventParam.UTM_CONTENT.name, true) ||
                        it.equals(NhAnalyticsCampaignEventParam.UTM_TERM.name, true) ||
                        it.equals(NhAnalyticsCampaignEventParam.URLREFERRER.name, true)
            }.mapKeys { it.key.toLowerCase() }

            val valueEncodedMap =
                filteredMap.mapValues { URLEncoder.encode(it.value, Constants.TEXT_ENCODING_UTF_8) }

            var queryParamsStr = UrlUtil.getUrlWithQueryParamns(null, valueEncodedMap)
            if (queryParamsStr.startsWith(UTM_PARAMS_SEPERATOR)) {
                queryParamsStr =
                        queryParamsStr.replaceFirst(UTM_PARAMS_SEPERATOR, Constants.EMPTY_STRING)
            }
            return DEEPLINK_PREFLIX + DEEPLINK_SEPERATOR + queryParamsStr;
        }
    }
}
