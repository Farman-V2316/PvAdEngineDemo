/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.interceptor

import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.analytics.EvtParam
import com.newshunt.dataentity.dhutil.analytics.EvtType
import com.newshunt.dataentity.dhutil.analytics.VerApiDevEvent
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.postToRestBus
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.nio.charset.Charset

/**
 *
 * This interceptor is used by the versioned apis to get the raw response from the server and
 * store in the versioned db.
 *
 * @author shrikant.agarwal
 */
class VersionedApiInterceptor
@JvmOverloads
constructor(private val validator: (String) -> String,
            private val saveToDb: ((String) -> Unit)? = null) : Interceptor {
    private val TAG = "VersionedApiInterceptor"
    private val emptyPair = ("" to "")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        //1st request
        val isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED,
                false)
        var request: Request
        if (!isRegistered) {
            val original = chain.request()
            val originalHttpUrl: HttpUrl = original.url()
            val url = originalHttpUrl.newBuilder().addQueryParameter("noReg", "true").build()
            val requestBuilder: Request.Builder = original.newBuilder()
                    .url(url)
            request = requestBuilder.build()
        } else {
            request = chain.request()
        }
        val response = chain.proceed(request)
        val retriedResponse: Response

        if (response.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            devLog(response, "")
            return response
        } else if (!response.isSuccessful) { // failed
            Logger.e(TAG, "intercept: first req failed " + response.code() + " retrying")
            devLog(response)
            retriedResponse = chain.proceed(request)
        } else {
            val verAndData = try {
                extractVersionFromResponse(response)
            } catch (e: Exception) {
                emptyPair
            }
            if (CommonUtils.isEmpty(verAndData.first)) { // unable to validate
                Logger.e(TAG, "intercept: first req validation failed " + response.code() + " retrying")
                devLog(response)
                retriedResponse = chain.proceed(request)
            } else { // successful & we are able to validate
                devLog(response, verAndData.first)
                return response
            }
        }

        if (retriedResponse.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            devLog(retriedResponse, "", isRetry = true)
        } else if (!retriedResponse.isSuccessful) { // failed
            devLog(retriedResponse, isRetry = true)
            Logger.e(TAG, "intercept: retry failed with" + retriedResponse.code() + ". do nothing")
        } else {
            val verAndData = try {
                extractVersionFromResponse(retriedResponse)
            } catch (e: Exception) {
                emptyPair
            }
            if (CommonUtils.isEmpty(verAndData.first)) { // unable to validate
                Logger.e(TAG,
                        "intercept: retry validation failed resetting version")
                devLog(retriedResponse, isRetry = true)
            } else { // successful & we are able to validate
                devLog(retriedResponse, verAndData.first, true)
            }
        }
        return retriedResponse
    }

    @Throws(IOException::class)
    private fun extractVersionFromResponse(response: Response): Pair<String, String> {
        val responseBody = response.body() ?: return emptyPair

        val contentLength = responseBody.contentLength()
        val source = responseBody.source()
        source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source.buffer()

        if (contentLength != 0L) {
            val rawResponse = buffer.clone().readString(Charset.forName(Constants.TEXT_ENCODING_UTF_8))
            try {
                return validator(rawResponse) to rawResponse
            } catch (e: Exception) {
                Logger.e(TAG, "Exception validating response", e)
            }
        }
        return emptyPair
    }

    private fun devLog(response: Response,
                       parsedVersion: String? = null,
                       isRetry: Boolean = false) {
        if (!AndroidUtils.devEventsEnabled()) return
        val evtType = if (isRetry) EvtType.DEV_VER_RETRY_RESP else EvtType.DEV_VER_RESP
        postToRestBus {
            VerApiDevEvent(evtType.name.toLowerCase(), mapOf(
                    EvtParam.UNIQUE_ID to hashCode(),
                    EvtParam.URL to response.request().url().toString(),
                    EvtParam.RESP_CODE to response.code(),
                    EvtParam.RESULT to response.isSuccessful,
                    EvtParam.SIZE to (response.body()?.contentLength() ?: -1),
                    EvtParam.SERV_VERSION to (parsedVersion ?: "PARSE_ERROR")))
        }
    }

}