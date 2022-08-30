/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.facebook

import com.facebook.applinks.AppLinkData
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.launch.CampaignAcquisitionHelper
import com.newshunt.dhutil.helper.preference.AppStatePreference
import java.net.URLDecoder

/**
 * Helper singleton class to fetch the deferred deeplink from Facebook SDK
 * <p>
 * Created by srikanth.ramaswamy on 07/10/2018.
 */

object FBDeepLinkHelper {
    private const val PARAM_CAMPAIGN = "campaign"
    private const val LOG_TAG = "FBDeepLinkHelper"
    private const val DAILYHUNT_SCHEME = "dailyhunt://"

    fun fetchDeferredDeeplink() {
        AppLinkData.fetchDeferredAppLinkData(CommonUtils.getApplication()) {
            val deferredDeeplink = it?.argumentBundle?.getString(AppLinkData.ARGUMENTS_NATIVE_URL)
            if (isValidDeferredDeeplink(deferredDeeplink)) {
                Logger.d(LOG_TAG, "deferred deeplink from FB: $deferredDeeplink")
                PreferenceManager.savePreference(AppStatePreference.FB_DEFERRED_DEEPLINK, deferredDeeplink)
                val urlQueryParams = UrlUtil.urlRequestParamToMap(UrlUtil.getQueryUrl(deferredDeeplink))
                if (!CommonUtils.isEmpty(urlQueryParams)) {
                    val campaignParam = urlQueryParams[PARAM_CAMPAIGN]
                    if (!CommonUtils.isEmpty(campaignParam)) {
                        CampaignAcquisitionHelper.parseCampaignParameter(URLDecoder.decode(campaignParam!!, Constants.TEXT_ENCODING_UTF_8))
                    }
                }
            }
        }
    }

    fun getDeferredDeeplinkResponse(): String {
        return PreferenceManager.getPreference(AppStatePreference.FB_DEFERRED_DEEPLINK, Constants
                .EMPTY_STRING)
    }

    private fun isValidDeferredDeeplink(deferredDeeplink: String?): Boolean {
        if (CommonUtils.isEmpty(deferredDeeplink) || !deferredDeeplink!!.startsWith(DAILYHUNT_SCHEME)) {
            Logger.d(LOG_TAG, "Ignoring deeplink received: $deferredDeeplink")
            return false
        }

        return true
    }
}