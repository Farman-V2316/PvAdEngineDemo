/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts

import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.model.entity.AuthType
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.AccountLinkType
import com.newshunt.sso.analytics.NhAnalyticsSSOEventParam

/**
 * Helper class to fire analytics events related to accounts linking
 *
 * @author srikanth on 06/16/2020
 */
const val TYPE_PROMPT_VIEW = "prompt_view"
const val TYPE_ACCOUNT_SELECT = "account_select"
const val TYPE_NUMBER_EDIT = "number_edit"

class AccountsAnalyticsHelper {
    companion object {
        @JvmStatic
        fun logAccountOptionsDisplayed(referrer: PageReferrer?) {
            val paramsMap = mutableMapOf<NhAnalyticsEventParam, Any?>(NhAnalyticsAppEventParam.TYPE
                    to TYPE_PROMPT_VIEW)
            logAccountLinkEvent(paramsMap, referrer)
        }

        @JvmStatic
        fun logAccountOptionSelectedEvent(referrer: PageReferrer?, selection: String?) {
            val paramsMap = mutableMapOf<NhAnalyticsEventParam, Any?>(NhAnalyticsAppEventParam.TYPE to TYPE_ACCOUNT_SELECT,
                    NhAnalyticsSSOEventParam.SELECTION to selection)
            logAccountLinkEvent(paramsMap, referrer)
        }

        @JvmStatic
        fun logPrimaryAccountSelected(referrer: PageReferrer?,
                                      oldUserId: String?,
                                      newUserId: String?,
                                      linkedAccountList: List<AccountLinkType>?) {
            //Filter the account types and convert them to AuthType string for analytics
            val linkedAccountsString = (linkedAccountList?.asSequence()?.filter {
                it.loginType in listOf(LoginType.MOBILE, LoginType.FACEBOOK, LoginType.GOOGLE)
            }?.map {
                it.loginType
            }?.map {
                AuthType.getAuthTypeFromLoginType(it)
            }?.map {
                it?.name
            }?.filterNotNull()?.toList() ?: emptyList()).toString()

            val paramsMap = mutableMapOf<NhAnalyticsEventParam, Any?>(NhAnalyticsAppEventParam.OLD_VALUE to oldUserId,
                    NhAnalyticsAppEventParam.NEW_VALUE to newUserId,
                    NhAnalyticsAppEventParam.LINKED_ACCOUNTS to linkedAccountsString)
            logAccountLinkEvent(paramsMap, referrer)
        }

        @JvmStatic
        fun logMobileNumberEditClicked(referrer: PageReferrer?) {
            val paramsMap = mutableMapOf<NhAnalyticsEventParam, Any?>(NhAnalyticsAppEventParam.TYPE to TYPE_NUMBER_EDIT)
            logAccountLinkEvent(paramsMap, referrer)
        }

        private fun logAccountLinkEvent(paramsMap: Map<NhAnalyticsEventParam, Any?>, referrer: PageReferrer?) {
            AnalyticsClient.log(NhAnalyticsAppEvent.ACCOUNT_LINK,
                    NhAnalyticsEventSection.APP,
                    paramsMap,
                    referrer)
        }
    }
}