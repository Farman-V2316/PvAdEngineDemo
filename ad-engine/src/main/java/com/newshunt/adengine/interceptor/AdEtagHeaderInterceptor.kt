/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.interceptor

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.preference.AdsPreference
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Save E-Tag for evergreen ads sync api.
 *
 * @author raunak.yadav
 */
class AdEtagHeaderInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val eTagHeader = PreferenceManager.getPreference(AdsPreference.AD_EVERGREEN_API_E_TAG,
            Constants.EMPTY_STRING)
        if (!eTagHeader.isNullOrBlank()){
            builder.addHeader(IF_NONE_MATCH_HEADER, eTagHeader)
        }

        val request = builder.build()
        val response = chain.proceed(request)

        val eTag = response.header(ETAG_HEADER)
        PreferenceManager.savePreference(AdsPreference.AD_EVERGREEN_API_E_TAG, eTag)
        return response
    }

    companion object {
        private const val IF_NONE_MATCH_HEADER = "If-None-Match"
        private const val ETAG_HEADER = "ETag"
    }
}