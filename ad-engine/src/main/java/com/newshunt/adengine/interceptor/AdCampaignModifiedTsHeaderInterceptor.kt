/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.interceptor

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.preference.AdsPreference
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Save Last-Modified time for ad campaign sync api.
 *
 * @author raunak.yadav
 */
class AdCampaignModifiedTsHeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val lastModifiedPref = PreferenceManager.getPreference(AdsPreference.AD_CAMPAIGN_LAST_MODIFIED_TS, Constants.EMPTY_STRING)
        builder.addHeader(IF_MODIFIED_SINCE_HEADER, lastModifiedPref)

        val request = builder.build()
        val response = chain.proceed(request)

        val lastModified = response.header(LAST_MODIFIED_HEADER)
        PreferenceManager.savePreference(AdsPreference.AD_CAMPAIGN_LAST_MODIFIED_TS, lastModified)
        return response
    }

    companion object {
        private const val IF_MODIFIED_SINCE_HEADER = "If-Modified-Since"
        private const val LAST_MODIFIED_HEADER = "Last-Modified"
    }
}