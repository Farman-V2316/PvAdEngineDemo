/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.sso.analytics

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.model.entity.AuthType
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.news.analytics.NHProfileAnalyticsEventParam
import com.newshunt.sso.model.entity.SSOResult

import java.util.HashMap

/**
 * @author anshul.jain
 * Utility class for signin and signout analytics.
 */

class SSOAnalyticsUtility {

    companion object {

        @JvmStatic
        fun logSigninPageView(pageReferrer: PageReferrer?, isFPV: Boolean) {
            val map = mutableMapOf<NhAnalyticsEventParam, Any?>(
                    NHProfileAnalyticsEventParam.PROFILE_VIEW_TYPE to if (isFPV) Constants.FPV else Constants.TPV
            )
            AnalyticsClient.log(NhAnalyticsSSOEvent.SIGN_IN_PAGE_VIEW, NhAnalyticsEventSection
                    .APP, map, pageReferrer)
        }

        @JvmStatic
        fun logSignInClick(loginType: LoginType, pageReferrer: PageReferrer?, isFPV: Boolean) {
            val map = mutableMapOf<NhAnalyticsEventParam, Any?>(
                    NhAnalyticsSSOEventParam.SELECTION to AuthType.getAuthTypeFromLoginType
                    (loginType)?.toString()?.toLowerCase(),
                    NHProfileAnalyticsEventParam.PROFILE_VIEW_TYPE to if (isFPV) Constants.FPV else Constants.TPV

            )
            AnalyticsClient.log(NhAnalyticsSSOEvent.SIGN_IN_CLICK, NhAnalyticsEventSection.APP,
                    map, pageReferrer)
        }

        @JvmStatic
        fun logSignInSuccessful(loginType: LoginType, pageReferrer: PageReferrer?) {
            val map = HashMap<NhAnalyticsEventParam, Any?>()
            map[NhAnalyticsSSOEventParam.SELECTION] = AuthType.getAuthTypeFromLoginType(loginType)
                    ?.toString()?.toLowerCase()
            map[NhAnalyticsSSOEventParam.SUCCESS] = true
            AnalyticsClient.log(NhAnalyticsSSOEvent.SIGN_IN_STATUS, NhAnalyticsEventSection.APP,
                    map, pageReferrer)
        }

        @JvmStatic
        fun logSignInFailure(loginType: LoginType, result: SSOResult?, pageReferrer: PageReferrer?) {
            val map = HashMap<NhAnalyticsEventParam, Any?>()
            map[NhAnalyticsSSOEventParam.SELECTION] = AuthType.getAuthTypeFromLoginType(loginType)
                    ?.toString()?.toLowerCase()
            map[NhAnalyticsSSOEventParam.SUCCESS] = false
            map[NhAnalyticsSSOEventParam.FAILURE_REASON] = result?.name
            AnalyticsClient.log(NhAnalyticsSSOEvent.SIGN_IN_STATUS, NhAnalyticsEventSection.APP,
                    map, pageReferrer)
        }

        @JvmStatic
        fun logMenuSignin() {
            AnalyticsClient.log(NhAnalyticsSSOEvent.MENU_SIGN_IN, NhAnalyticsEventSection.APP, null)
        }

        @JvmStatic
        fun logMenuSignOut(signinMethod: String?, pageReferrer: PageReferrer?, signOutFlow: String) {
            val paramsMap = HashMap<NhAnalyticsEventParam, Any?>()
            paramsMap[NhAnalyticsSSOEventParam.SIGN_IN_METHOD] = signinMethod
                    ?: Constants.EMPTY_STRING
            paramsMap.put(NhAnalyticsSSOEventParam.SIGNOUT_FLOW, signOutFlow)
            AnalyticsClient.log(NhAnalyticsSSOEvent.MENU_SIGN_OUT, NhAnalyticsEventSection
                    .APP, paramsMap, pageReferrer)
        }
    }
}