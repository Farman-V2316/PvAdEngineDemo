/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.client

import android.util.Base64
import com.newshunt.adengine.model.AdErrorAPI
import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.entity.AdErrorRequestBody
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdFrequencyStats.onAdImpressionFailed
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdStatisticsHelper.onAdViewed
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEvents
import com.newshunt.dhutil.helper.appsflyer.AppsFlyerHelper.trackEvent
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.retrofit.RestAdapterProvider
import com.newshunt.sdk.network.Priority
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.concurrent.Callable
import java.util.concurrent.FutureTask

/**
 * Reports to ad-server about ad-display and ad-clicks.
 * Each ad should use this class to report impression and click through.
 * It assumes all URLs are configured in following properties of
 * [BaseDisplayAdEntity]
 *
 *  * beaconUrl
 *  * additionalBeaconUrl
 *  * landingUrl
 *  * additionalLandingUrl
 *
 * @author heena.arora
 */
class AsyncAdImpressionReporter(private val baseDisplayAdEntity: BaseDisplayAdEntity) {
    private val beaconUrl: String? = baseDisplayAdEntity.beaconUrl
    private val additionalBeaconUrls: MutableList<String?> = ArrayList()
    private var errorAPI: AdErrorAPI? = null
    private val impressionApiExecutor = AndroidUtils.newSingleThreadExecutor(IMPRESSION_API_EXECUTOR_THREAD)

    @JvmOverloads
    fun onCardView(startedMute: Boolean? = null, adPositionIndex: Int = -1) {
        baseDisplayAdEntity.adPosition?.let {
            onAdViewed(it)
        }
        if (!beaconUrl.isNullOrBlank()) {
            val beacon = StringBuilder(beaconUrl)
            if (startedMute != null) {
                beacon.append("&muteState=").append(startedMute)
            }
            if (AutoPlayHelper.isAutoPlayAllowed()) {
                if (baseDisplayAdEntity.isAboveContentAutoplayable) {
                    beacon.append("&aboveContentAutoplayable=true")
                }
                if (baseDisplayAdEntity.isBelowContentAutoplayable) {
                    beacon.append("&belowContentAutoplayable=true")
                }
            }
            val bodyParams = mutableMapOf<String?, String?>()
            if (adPositionIndex != -1) {
                AdLogger.d(LOG_TAG, "Ad inserted at index : $adPositionIndex")
                bodyParams[AD_INSERTED_INDEX] = adPositionIndex.toString()
            }
            baseDisplayAdEntity.fallbackReason?.let {
                bodyParams[AD_EG_FALLBACK_REGULAR_USER] = it.isRegularUser.toString()
                bodyParams[AD_EG_FALLBACK_TIMEOUT] = it.regAdTimeout.toString()
                bodyParams[AD_EG_FALLBACK_FIRST_AD] = it.isFirstAd.toString()
            }
            baseDisplayAdEntity.displayPosition?.let {
                bodyParams[AD_DISPLAY_POSITION] = JsonUtils.toJson(it)
            }
            execute(isMainImpression = true, addBodyParams = true,
                bodyParams, urls = listOf(beacon.toString()))
        }
        val event = if (baseDisplayAdEntity.isVideoAd) AppsFlyerEvents.EVENT_FIRST_VIDEO_AD_IMPRESSION
        else AppsFlyerEvents.EVENT_FIRST_AD_IMPRESSION

        trackEvent(event, null)
        execute(isMainImpression = false, addBodyParams = false, urls = additionalBeaconUrls)
    }

    fun onClickEvent() {
        //landing urls gets updated with npkeys as we swipe/scroll across the articles, we will get
        // updated landing url value here when user clicks on an ad.
        val landingUrl = baseDisplayAdEntity.landingUrl
        val additionalLandingUrls: MutableList<String> = ArrayList()
        additionalLandingUrls.addAll(baseDisplayAdEntity.landingUrlAdditional)
        landingUrl?.let {
            execute(isMainImpression = false, addBodyParams = true, urls = listOf(it))
        }
        execute(isMainImpression = false, addBodyParams = false, urls = additionalLandingUrls)
    }

    fun onAdInflated() {
        //Evergreen ads will hit requestUrl before AdInflated.
        if (baseDisplayAdEntity.isEvergreenAd && !baseDisplayAdEntity.isRequestUrlHit) {
            baseDisplayAdEntity.isRequestUrlHit = true
            execute(isMainImpression = false, addBodyParams = false,
                urls = listOf(baseDisplayAdEntity.requestUrl))
        }

        //AdInflated url will have fallback reason, if any.
        val bodyParams = mutableMapOf<String?, String?>()
        baseDisplayAdEntity.fallbackReason?.let {
            bodyParams[AD_EG_FALLBACK_REGULAR_USER] = it.isRegularUser.toString()
            bodyParams[AD_EG_FALLBACK_TIMEOUT] = it.regAdTimeout.toString()
            bodyParams[AD_EG_FALLBACK_FIRST_AD] = it.isFirstAd.toString()
        }
        execute(isMainImpression = false, false, bodyParams = bodyParams,
            urls = listOf(baseDisplayAdEntity.adInflatedBeaconUrl))
    }

    fun onAdViewToggled(adInteraction: AdInteraction) {
        val url = baseDisplayAdEntity.adClosedBeaconUrl
        if (url.isNullOrBlank()) {
            return
        }
        execute(
            isMainImpression = false,
            addBodyParams = true,
            bodyParams = mutableMapOf(AD_INTERACTION to adInteraction.name),
            listOf(url)
        )
    }

    fun hitErrorBeacon(adErrorBody: AdErrorRequestBody, screenshotFilePath: String? = null) {
        if (baseDisplayAdEntity.errorUrl.isNullOrBlank())
            return

        if (errorAPI == null) {
            errorAPI = RestAdapterProvider.getRestAdapter(Priority.PRIORITY_NORMAL, null, false)
                    .create(AdErrorAPI::class.java)
        }

        val map: HashMap<String, RequestBody?> = HashMap()
        adErrorBody.errorCode?.let {
            map[AdConstants.ERROR_REQ_BODY_ERROR_CODE] = toRequestBody(it.toString())
        }
        adErrorBody.errorMessage?.let {
            map[AdConstants.ERROR_REQ_BODY_ERROR_MESSAGE] = toRequestBody(it)
        }
        adErrorBody.url?.let {
            map[AdConstants.ERROR_REQ_BODY_URL] = toRequestBody(it)
        }
        adErrorBody.playerErrorCode?.let {
            map[AdConstants.ERROR_REQ_BODY_PLAYER_ERROR_CODE] = toRequestBody(it.toString())
        }
        adErrorBody.playerErrorMessage?.let {
            map[AdConstants.ERROR_REQ_BODY_PLAYER_ERROR_MESSAGE] = toRequestBody(it)
        }
        adErrorBody.playerErrorType?.let {
            map[AdConstants.ERROR_REQ_BODY_PLAYER_ERROR_TYPE] = toRequestBody(it)
        }

        var errorImage: MultipartBody.Part? = null
        if (!screenshotFilePath.isNullOrBlank()) {
            val file = File(screenshotFilePath)
            errorImage = MultipartBody.Part.createFormData(
                "screenshot",
                file.name,
                RequestBody.create(MediaType.parse("image/jpg"), file)
            )
        }

        val call = errorAPI?.hitErrorBeacon(baseDisplayAdEntity.errorUrl, map, errorImage)
        call?.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: retrofit2.Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (null != response && response.isSuccessful) {
                    AdLogger.d(LOG_TAG, "SUCCESS [${baseDisplayAdEntity.adPosition} : " +
                            "${baseDisplayAdEntity.uniqueAdIdentifier}] ${call.request().url()}")
                }
                val cacheDir = File(CommonUtils.getApplication().externalCacheDir?.path, AdConstants.SCREENSHOT_DIR)
                CommonUtils.deleteTempFiles(cacheDir)
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                AdLogger.d(LOG_TAG, "ErrorUrl hit failed ${t.message}")
                val cacheDir = File(CommonUtils.getApplication().externalCacheDir?.path, AdConstants.SCREENSHOT_DIR)
                CommonUtils.deleteTempFiles(cacheDir)
            }
        })
    }

    fun toRequestBody(value: String): RequestBody? {
        return RequestBody.create(MediaType.parse("text/plain"), value)
    }

    fun hitTrackerUrl(bodyParams: Boolean, url: String?) {
        url?.let { execute(isMainImpression = false, addBodyParams = bodyParams, urls = listOf(it)) }
    }

    fun hitTrackerUrl(url: String?) {
        url?.let { hitTrackerUrl(false, it) }
    }

    private fun execute(isMainImpression: Boolean, addBodyParams: Boolean,
                        bodyParams: MutableMap<String?, String?> = HashMap(), urls: List<String?>?) {
        if (urls.isNullOrEmpty()) {
            return
        }
        if (addBodyParams && baseDisplayAdEntity.adReportInfo != null) {
            val adInfo = baseDisplayAdEntity.adReportInfo!!
            updateParamWithKey(bodyParams, AD_TITLE, adInfo.adTitle)
            updateParamWithKey(bodyParams, AD_DESCRIPTION, adInfo.adDescription)
            updateParamWithKey(bodyParams, AD_ADVERTISER, adInfo.advertiser)
            updateParamWithKey(bodyParams, AD_CATEGORY, adInfo.category)
        }
        val futureTasks: MutableList<FutureTask<Call>> = ArrayList()
        for (url in urls) {
            if (url.isNullOrBlank()) {
                continue
            }
            futureTasks.add(FutureTask(AdImpressionReportedCallable(bodyParams, url)))
        }
        for (futureTask in futureTasks) {
            try {
                impressionApiExecutor.execute(futureTask)
                futureTask.get().enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        AdLogger.d(LOG_TAG, "FAILED ${call.request().url()}")
                        if (isMainImpression) {
                            onAdImpressionFailed(baseDisplayAdEntity)
                        }
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        AdLogger.d(LOG_TAG, "SUCCESS [${baseDisplayAdEntity.adPosition} : " +
                                "${baseDisplayAdEntity.uniqueAdIdentifier}] ${call.request().url()}")
                        response.close()
                    }
                })
            } catch (e: Exception) {
                Logger.e(LOG_TAG, "exceuting callable error ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun updateParamWithKey(bodyParams: MutableMap<String?, String?>, key: String, value: String?) {
        if (value.isNullOrBlank()) {
            return
        }
        try {
            val data = value.toByteArray(charset(Constants.TEXT_ENCODING_UTF_8))
            val base64 = Base64.encodeToString(data, Base64.NO_WRAP)
            val urlEncodedString = URLEncoder.encode(base64, Constants.TEXT_ENCODING_UTF_8)
            bodyParams[key] = urlEncodedString
        } catch (e: UnsupportedEncodingException) {
            Logger.caughtException(e)
        }
    }

    //callable for running api calls
    private inner class AdImpressionReportedCallable(private val bodyParams: Map<String?, String?>?,
                                                     private val trackingUrl: String) : Callable<Call> {
        @Throws(Exception::class)
        override fun call(): Call {
            return if (bodyParams != null) {
                HttpClientManager.newAdRequestCall(trackingUrl, bodyParams, Priority.PRIORITY_NORMAL)
            } else {
                HttpClientManager.newRequestCall(trackingUrl, Priority.PRIORITY_NORMAL)
            }
        }

    }

    companion object {
        private const val LOG_TAG = "PingURL"
        private const val AD_TITLE = "title"
        private const val AD_DESCRIPTION = "description"
        private const val AD_ADVERTISER = "advertiser"
        private const val AD_CATEGORY = "category"
        private const val AD_INSERTED_INDEX = "adInsertedIndex"
        private const val AD_EG_FALLBACK_REGULAR_USER = "isRegularUser"
        private const val AD_EG_FALLBACK_TIMEOUT = "regAdTimeout"
        private const val AD_EG_FALLBACK_FIRST_AD = "isFirstAd"
        private const val AD_DISPLAY_POSITION = "displayPosition"
        private const val AD_INTERACTION = "interaction"
        private const val IMPRESSION_API_EXECUTOR_THREAD = "IMPRESSION_API_EXECUTOR_THREAD"
    }

    init {
        additionalBeaconUrls.addAll(baseDisplayAdEntity.beaconUrlAdditional)
    }
}