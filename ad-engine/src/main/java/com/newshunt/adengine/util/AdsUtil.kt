/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.util

import `in`.dailyhunt.money.adContextEvaluatorEngineNative.AdContextLogger
import `in`.dailyhunt.money.contentContext.ContentContext
import `in`.dailyhunt.money.frequency.FCEngine
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.amazon.device.ads.AdRegistration
import com.dailyhunt.tv.exolibrary.entities.MediaItem
import com.dailyhunt.tv.ima.ContentPlayerController
import com.dailyhunt.tv.players.constants.PlayerContants
import com.facebook.ads.InterstitialAd
import com.facebook.ads.NativeAd
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AmazonAdFetcher
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.instream.IAdLogger
import com.newshunt.adengine.model.ExternalAdResponse
import com.newshunt.adengine.model.entity.AdErrorType
import com.newshunt.adengine.model.entity.AdReportInfo
import com.newshunt.adengine.model.entity.AdUrl
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.model.entity.NativeData
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.model.entity.version.AmazonBidPayload
import com.newshunt.adengine.model.entity.version.ExternalSdkAdType
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.ViewUtils
import com.newshunt.common.helper.font.TitleBoldHelper
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.NHRoundedFrameLayout
import com.newshunt.dataentity.ads.AdFCType
import com.newshunt.dataentity.common.asset.AdDisplayType
import com.newshunt.dataentity.common.asset.PostEntity
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsConfig
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsSdkTimeout
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.ApsInfo
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dataentity.social.entity.ZoneConfig
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.util.NewsConstants
import com.newshunt.sdk.network.connection.ConnectionManager
import com.newshunt.sdk.network.connection.ConnectionSpeed
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

/**
 * @author neeraj.kumar
 */
class AdsUtil {

    companion object {
        const val TAG = "AdsUtils"

        val zonesWhichDropEmptyAds = listOf(AdPosition.SPLASH_DEFAULT,
            AdPosition.VDO_PGI)

        val zonesWhichSkipEvergreenAds = setOf(AdPosition.SPLASH_DEFAULT,
            AdPosition.VDO_PGI)

        val instreamAdSourceIdMap = mapOf(0 to AdConstants.PRE_ROLL_POD, -1 to AdConstants.POST_ROLL_POD)


        /**
         * returns swipe count which is saved across sessions
         *
         * @return
         */
        @JvmStatic
        val savedSwipeCount: Int
            get() = PreferenceManager.getPreference(AdsPreference.SAVED_SWIPE_COUNT, 0)

        /**
         * retuns app launch count
         *
         * @return
         */
        @JvmStatic
        val appLaunchCount: Int
            get() = PreferenceManager.getPreference(AdsPreference.APP_LAUNCH_COUNT, 0)

        /**
         * returns minimum sessions count after which we should start using swipe counts saved across
         * sessions
         *
         * @return
         */
        @JvmStatic
        val minSessionsToPersistSwipeCount: Int
            get() = PreferenceManager.getPreference(AdsPreference.MIN_SESSIONS_TO_PERSIST_SWIPE_COUNT, -1)

        /**
         * default width for a wide image with standard ad padding on sides.
         *
         * @return
         */
        @JvmStatic
        val defaultWidthForWideAds: Int
            get() = CommonUtils.getDeviceScreenWidth() - 2 * CommonUtils.getDimension(R.dimen.ad_content_margin)

        /**
         * default height of media view
         *
         * @return
         */
        @JvmStatic
        //default media view aspect ratio is 1.91:1
        val mediaViewDefaultHeight: Int
            get() = (defaultWidthForWideAds / AdConstants.ASPECT_RATIO_WIDE_ADS_DEFAULT).toInt()

        @JvmStatic
        val minAspectRatioNative: Float
            get() {
                val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
                return if (adsUpgradeInfo != null && adsUpgradeInfo.minAspectRatioNativeHighTemplate > 0f) {
                    adsUpgradeInfo.minAspectRatioNativeHighTemplate
                } else AdConstants.ASPECT_RATIO_NATIVE_WIDE
            }

        @JvmStatic
        val minAspectRatioVideo: Float
            get() {
                val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
                return if (adsUpgradeInfo != null && adsUpgradeInfo.minAspectRatioVideo > 0f) {
                    adsUpgradeInfo.minAspectRatioVideo
                } else AdConstants.ASPECT_RATIO_VIDEO_MIN
            }

        @JvmStatic
        val isCardP0PullRefreshEnabled: Boolean
            get() = PreferenceManager.getPreference(AdsPreference.CARD_P0_REFRESH_ENABLED, false)

        // If connection info is not available within timeout, use last known value
        private val adRequestTimeout: Int
            get() {
                var timeoutConfig: AdsSdkTimeout? = null
                val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
                adsUpgradeInfo?.let {
                    timeoutConfig = adsUpgradeInfo.adsSdkTimeout
                }

                return when (ConnectionManager.getInstance().getCurrentConnectionSpeed(CommonUtils.getApplication())) {
                    ConnectionSpeed.GOOD, ConnectionSpeed.FAST -> {
                        if (timeoutConfig != null && timeoutConfig!!.adsTimeoutGood > 0)
                            timeoutConfig!!.adsTimeoutGood
                        else
                            AdConstants.ADS_SDK_TIMEOUT_GOOD
                    }
                    ConnectionSpeed.AVERAGE -> {
                        if (timeoutConfig != null && timeoutConfig!!.adsTimeoutAverage > 0)
                            timeoutConfig!!.adsTimeoutAverage
                        else
                            AdConstants.ADS_SDK_TIMEOUT_AVERAGE
                    }
                    else -> {
                        if (timeoutConfig != null && timeoutConfig!!.adsTimeoutSlow > 0)
                            timeoutConfig!!.adsTimeoutSlow
                        else
                            AdConstants.ADS_SDK_TIMEOUT_SLOW
                    }
                }
            }

        /**
         * This method creates folder in external cache directory and returns the path.
         *
         * @param folderName
         * @return
         */
        @JvmStatic
        fun getAdBaseDirectory(folderName: String): String? {
            val adBasePath: String
            val root = CommonUtils.getApplication().externalCacheDir ?: return null
            adBasePath = root.path + folderName
            val zipRootDirectory = File(adBasePath)
            if (!zipRootDirectory.exists()) {
                zipRootDirectory.mkdirs()
            }
            return adBasePath
        }

        /**
         * @param title    First part of title
         * @param subTitle Second part of title or body
         * @return Merged strings `title` and {@subTitle} in readable form.
         */
        @JvmStatic
        fun getMergedTitle(title: String?, subTitle: String?): String {
            return if (title.isNullOrBlank() && subTitle.isNullOrBlank()) {
                Constants.EMPTY_STRING
            } else if (title.isNullOrBlank()) {
                subTitle!!
            } else if (subTitle.isNullOrBlank()) {
                title
            } else {
                if (title.endsWith(".")) {
                    "$title $subTitle"
                } else {
                    "$title. $subTitle"
                }
            }
        }

        /**
         * saves the swipe count to be used across sessions.
         *
         * @param swipeCount
         */
        @JvmStatic
        fun saveSwipeCount(swipeCount: Int) {
            PreferenceManager.savePreference(AdsPreference.SAVED_SWIPE_COUNT, swipeCount)
        }

        /**
         * saves app launch count
         *
         * @param appLaunchCount
         */
        @JvmStatic
        private fun saveAppLaunchCount(appLaunchCount: Int) {
            PreferenceManager.savePreference(AdsPreference.APP_LAUNCH_COUNT, appLaunchCount)
        }

        /**
         * increment app launch count by 1 on every splash launch
         */
        @JvmStatic
        fun incrementAppLaunchCount() {
            var launchCount = appLaunchCount
            launchCount++
            saveAppLaunchCount(launchCount)
        }

        /**
         * saves minimum sessions count after which we should start using swipe count saved across
         * sessions.
         *
         * @param sessionCount
         */
        @JvmStatic
        fun saveMinSessionsToPersistSwipeCount(sessionCount: Int?) {
            sessionCount?.let {
                PreferenceManager.savePreference(AdsPreference.MIN_SESSIONS_TO_PERSIST_SWIPE_COUNT,
                    sessionCount)
            }
        }

        @JvmStatic
        fun getActionBarHeight(activity: Activity?): Int {
            if (activity == null) {
                return 0
            }
            val typedValue = TypedValue()
            activity.theme.resolveAttribute(R.attr.actionBarSize, typedValue, true)
            return TypedValue.complexToDimensionPixelSize(
                    typedValue.data, activity.resources.displayMetrics)
        }

        @JvmStatic
        fun isUrdu(baseDisplayAdEntity: BaseDisplayAdEntity?): Boolean {
            return baseDisplayAdEntity?.content?.language?.equals(NewsConstants
                    .URDU_LANGUAGE_CODE, ignoreCase = true) ?: false
        }

        @JvmStatic
        fun isEnglish(baseDisplayAdEntity: BaseDisplayAdEntity?): Boolean {
            return baseDisplayAdEntity?.content?.language?.equals(
                    NewsConstants.ENGLISH_LANGUAGE_CODE, ignoreCase = true) ?: false
        }

        /**
         * saves cache count for a particular adPosition to be used across sessions on good network.
         *
         * @param adPosition - zone type of ad
         * @param cacheCount - cache level
         */
        @JvmStatic
        fun saveCacheCountForGoodNwSpeed(adPosition: AdPosition?, cacheCount: Int?) {
            adPosition ?: return
            cacheCount ?: return
            // In case count comes <= 0, keep using the current value in preferences.
            if (cacheCount <= 0) {
                return
            }

            val adPref = getPreferenceForAdPositionForGoodNw(adPosition)
            if (adPref != null) {
                PreferenceManager.savePreference(adPref,
                        if (cacheCount > AdConstants.MAX_AD_CACHE_COUNT_GOOD)
                            AdConstants.MAX_AD_CACHE_COUNT_GOOD
                        else
                            cacheCount)
            }
        }

        /**
         * saves cache count for a particular adPosition to be used across sessions on good network.
         *
         * @param adPosition - zone type of ad
         * @param cacheCount - cache level
         */
        @JvmStatic
        fun saveCacheCountForAverageNwSpeed(adPosition: AdPosition?, cacheCount: Int?) {
            adPosition ?: return
            cacheCount ?: return
            // In case count comes <= 0, keep using the current value in preferences.
            if (cacheCount <= 0) {
                return
            }

            val adPref = getPreferenceForAdPositionForAverageNw(adPosition)
            if (adPref != null) {
                PreferenceManager.savePreference(adPref,
                        if (cacheCount > AdConstants.MAX_AD_CACHE_COUNT_AVERAGE)
                            AdConstants.MAX_AD_CACHE_COUNT_AVERAGE
                        else
                            cacheCount)
            }
        }

        /**
         * saves cache count for a particular adPosition to be used across sessions on good network.
         *
         * @param adPosition - zone type of ad
         * @param cacheCount - cache level
         */
        @JvmStatic
        fun saveCacheCountForSlowNwSpeed(adPosition: AdPosition?, cacheCount: Int?) {
            adPosition ?: return
            cacheCount ?: return
            // In case count comes <= 0, keep using the current value in preferences.
            if (cacheCount <= 0) {
                return
            }

            val adPref = getPreferenceForAdPositionForSlowNw(adPosition)
            if (adPref != null) {
                PreferenceManager.savePreference(adPref,
                        if (cacheCount > AdConstants.MAX_AD_CACHE_COUNT_SLOW)
                            AdConstants.MAX_AD_CACHE_COUNT_SLOW
                        else
                            cacheCount)
            }
        }

        /**
         * returns cache count which is saved across sessions
         *
         * @return
         */
        fun getSavedCacheCount(adPosition: AdPosition, adsUpgradeInfo: AdsUpgradeInfo?): Int {
            val adPref =  when (ConnectionManager.getInstance().getCurrentConnectionSpeed(CommonUtils.getApplication())) {
                ConnectionSpeed.SLOW -> getPreferenceForAdPositionForSlowNw(adPosition)

                ConnectionSpeed.AVERAGE -> getPreferenceForAdPositionForAverageNw(adPosition)

                ConnectionSpeed.GOOD, ConnectionSpeed.FAST -> getPreferenceForAdPositionForGoodNw(adPosition)

                else -> getPreferenceForAdPositionForSlowNw(adPosition)
            }

            var multiplier = 1
            if (adPosition == AdPosition.SUPPLEMENT) {
                multiplier = adsUpgradeInfo?.supplementAdConfig?.tagOrder?.size ?: 1
            }
            if (adPosition == AdPosition.PP1) {
                multiplier = adsUpgradeInfo?.cardPP1AdsConfig?.tagOrder?.size ?: 1
            }
            return if (adPref != null)
                multiplier * PreferenceManager.getPreference(adPref, AdConstants.DEFAULT_AD_CACHE_COUNT)
            else
                AdConstants.DEFAULT_AD_CACHE_COUNT
        }

        private fun getPreferenceForAdPositionForGoodNw(adPosition: AdPosition): AdsPreference? {
            return when (adPosition) {
                AdPosition.CARD_P1 -> AdsPreference.CARD_P1_AD_CACHE_LEVEL_GOOD
                AdPosition.STORY -> AdsPreference.STORY_AD_CACHE_LEVEL_GOOD
                AdPosition.SUPPLEMENT -> AdsPreference.SUPPLEMENT_AD_CACHE_LEVEL_GOOD
                AdPosition.MASTHEAD -> AdsPreference.MASTHEAD_AD_CACHE_LEVEL_GOOD
                AdPosition.DHTV_MASTHEAD -> AdsPreference.DHTV_MH_AD_CACHE_LEVEL_GOOD
                AdPosition.PP1 -> AdsPreference.CARD_PP1_AD_CACHE_LEVEL_GOOD
                else -> null
            }
        }

        private fun getPreferenceForAdPositionForAverageNw(adPosition: AdPosition): AdsPreference? {
            return when (adPosition) {
                AdPosition.CARD_P1 -> AdsPreference.CARD_P1_AD_CACHE_LEVEL_AVERAGE
                AdPosition.STORY -> AdsPreference.STORY_AD_CACHE_LEVEL_AVERAGE
                AdPosition.SUPPLEMENT -> AdsPreference.SUPPLEMENT_AD_CACHE_LEVEL_AVERAGE
                AdPosition.MASTHEAD -> AdsPreference.MASTHEAD_AD_CACHE_LEVEL_AVERAGE
                AdPosition.DHTV_MASTHEAD -> AdsPreference.DHTV_MH_AD_CACHE_LEVEL_AVERAGE
                AdPosition.PP1 -> AdsPreference.CARD_PP1_AD_CACHE_LEVEL_AVERAGE
                else -> null
            }
        }

        private fun getPreferenceForAdPositionForSlowNw(adPosition: AdPosition): AdsPreference? {
            return when (adPosition) {
                AdPosition.CARD_P1 -> AdsPreference.CARD_P1_AD_CACHE_LEVEL_SLOW
                AdPosition.STORY -> AdsPreference.STORY_AD_CACHE_LEVEL_SLOW
                AdPosition.SUPPLEMENT -> AdsPreference.SUPPLEMENT_AD_CACHE_LEVEL_SLOW
                AdPosition.MASTHEAD -> AdsPreference.MASTHEAD_AD_CACHE_LEVEL_SLOW
                AdPosition.DHTV_MASTHEAD -> AdsPreference.DHTV_MH_AD_CACHE_LEVEL_SLOW
                AdPosition.PP1 -> AdsPreference.CARD_PP1_AD_CACHE_LEVEL_SLOW
                else -> null
            }
        }

        /**
         * default height of image in native view for the given aspect ratio and subject to a
         * minAspectRatio.
         * If minAspectRatio = -1, then height can be as large as requested.
         *
         * @return aspect ratio adjusted height
         */
        @JvmStatic
        fun getHeightWithAspectRatio(width: Int, height: Int, aspectRatio: Float,
                                     viewWidth: Int, minAspectRatio: Float = -1f,
                                     isVideo: Boolean = false): Int {
            var aspectRatio = aspectRatio
            var viewWidth = viewWidth
            if (aspectRatio == 0f || minAspectRatio == 0f) {
                return 0
            }
            viewWidth = if (viewWidth == 0) CommonUtils.getDeviceScreenWidth() else viewWidth
            if (width != 0 && height != 0) {
                aspectRatio = 1.0f * width / height
            }

            // For portrait videos, Dont apply the min aspect ratio from handshake
            if ((isVideo && width > height && minAspectRatio != -1f) || aspectRatio <
                    minAspectRatio) {
                aspectRatio = minAspectRatio
            }
            val newHeight = (viewWidth / aspectRatio).toInt()
            val maxHeight = if (isVideo) getMaxHeight(CommonUtils.getApplication().applicationContext) else newHeight
            AdLogger.d(TAG,
                    "Original size " + width + 'x'.toString() + height + ". Adjusted size " + viewWidth + 'x'.toString() + newHeight
                            + ". Aspect ratio : " + aspectRatio + ". maxHeight : " + maxHeight)
            return  min(newHeight, maxHeight)
        }

        /**
         * Used to restrict video max ad height to the ratio specified in handshake
         */
        private fun getMaxHeight(context: Context): Int {
            val metrics = context.resources.displayMetrics
            val maxContentHeight: Int = metrics.heightPixels - CommonUtils.getPixelFromDP(
                    PlayerContants.TV_STATUS_BAR_HEIGHT * 2, context)
            val maxHeightRatio = PreferenceManager.getFloat(Constants.MAX_VIDEO_HEIGHT_RATIO, 1.0f)
            return min(maxContentHeight, (defaultWidthForWideAds * maxHeightRatio).toInt())
        }

        /**
         * Fetch ad unit id
         *
         * @param externalSdkAd
         * @param defaultValue
         * @return
         */
        @JvmStatic
        fun getAdUnitId(externalSdkAd: ExternalSdkAd?, defaultValue: String): String {
            val adUnitId = externalSdkAd?.external?.adUnitId
            return if (adUnitId.isNullOrBlank()) {
                defaultValue
            } else adUnitId

        }

        /**
         * Call the destroy method to release external sdk ads
         *
         * @param baseAdEntity  ad
         * @param uiComponentId componentId of calling view.
         */
        @JvmStatic
        fun destroyAd(baseAdEntity: BaseAdEntity?, uiComponentId: Int, viewDestroyed: Boolean = true) {
            baseAdEntity ?: return
            AndroidUtils.getMainThreadHandler().post {
                baseAdEntity.parentIds.remove(uiComponentId)
                if (baseAdEntity is ExternalSdkAd) {
                    destroyExternalSdkAd(baseAdEntity as ExternalSdkAd?, viewDestroyed)
                } else {
                    // Finish OM tracking session for the direct ads.
                    val ads = ArrayList<BaseDisplayAdEntity>()
                    if (baseAdEntity is MultipleAdEntity) {
                        ads.addAll(baseAdEntity.baseDisplayAdEntities)
                    } else {
                        ads.add(baseAdEntity as BaseDisplayAdEntity)
                    }

                    for (adEntity in ads) {
                        adEntity.omSessionState?.get(uiComponentId)?.let {
                            it.finish()
                            adEntity.omSessionState?.remove(uiComponentId)
                            AdLogger.d("OMTracker", "OM session finish for ${adEntity
                                    .uniqueAdIdentifier} in $uiComponentId")
                        }
                    }
                }
            }
        }

        /**
         * Call the destroy method to release external sdk ads
         *
         * @param externalSdkAd
         */
        private fun destroyExternalSdkAd(externalSdkAd: ExternalSdkAd?, viewDestroyed: Boolean) {
            externalSdkAd?.nativeAdObject ?: return

            if (!externalSdkAd.isShown) {
                if (externalSdkAd.parentIds.isEmpty()) {
                    when (val sdkAd = externalSdkAd.nativeAdObject) {
                        is AdManagerAdView -> detachAd(sdkAd as View)
                        is NativeAd -> sdkAd.unregisterView()
                        is ContentPlayerController -> {
                            if (viewDestroyed) {
                                sdkAd.destroy()
                            }
                            externalSdkAd.nativeAdObject = null
                        }
                    }
                }
                return
            }

            when (val sdkAd = externalSdkAd.nativeAdObject) {
                is AdManagerAdView -> {
                    sdkAd.destroy()
                    detachAd(sdkAd as View)
                }
                is com.google.android.gms.ads.nativead.NativeAd -> sdkAd.destroy()
                is NativeCustomFormatAd -> sdkAd.destroy()
                is NativeAd -> sdkAd.destroy()
                is InterstitialAd -> sdkAd.destroy()

                is ContentPlayerController -> sdkAd.destroy()
            }
            externalSdkAd.nativeAdObject = null
        }

        /**
         * Detach ad from layout to avoid activity leak.
         * @param adView adView
         */
        private fun detachAd(adView: View) {
            val parent = adView.parent as? ViewGroup
            parent?.removeView(adView)
        }

        @JvmStatic
        fun isExternalSdkNativePgiAd(baseAdEntity: BaseAdEntity?): Boolean {
            if (baseAdEntity !is ExternalSdkAd) {
                return false
            }
            return when (ExternalSdkAdType.fromAdType(baseAdEntity.external?.data)) {
                ExternalSdkAdType.DFP_NATIVE_INTERSTITIAL,
                ExternalSdkAdType.FB_NATIVE_INTERSTITIAL,
                ExternalSdkAdType.FB_NATIVE_INTERSTITIAL_BID -> true
                else -> false
            }
        }

        @JvmStatic
        fun isExternalPopUpAd(baseAdEntity: BaseAdEntity?): Boolean {
            if (baseAdEntity !is ExternalSdkAd) {
                return false
            }
            return when (ExternalSdkAdType.fromAdType(baseAdEntity.external?.data)) {
                ExternalSdkAdType.DFP_INTERSTITIAL,
                ExternalSdkAdType.FB_INTERSTITIAL_AD,
                ExternalSdkAdType.AMAZON_INTERSTITIAL -> true
                else -> false
            }
        }

        @JvmStatic
        fun isIMAVideoAd(baseAdEntity: BaseAdEntity?): Boolean {
            if (baseAdEntity !is ExternalSdkAd) {
                return false
            }
            val externalAdType = ExternalSdkAdType.fromAdType(baseAdEntity.external?.data)
            return externalAdType == ExternalSdkAdType.IMA_SDK
        }

        private fun isNativeAd(adEntity: BaseAdEntity?): Boolean {
            return when (adEntity?.type) {
                AdContentType.NATIVE_BANNER -> true
                AdContentType.EXTERNAL_SDK -> {
                    val externalSdkAd = adEntity as ExternalSdkAd
                    ExternalSdkAdType.fromAdType(externalSdkAd.external?.data) == ExternalSdkAdType.DFP_CUSTOM_NATIVE
                }
                else -> false
            }
        }

        private fun isFbNativeAd(baseAdEntity: BaseAdEntity?): Boolean {
            if (baseAdEntity !is ExternalSdkAd) {
                return false
            }
            return when (ExternalSdkAdType.fromAdType(baseAdEntity.external?.data)) {
                ExternalSdkAdType.FB_NATIVE_AD,
                ExternalSdkAdType.FB_NATIVE_BID,
                ExternalSdkAdType.FB_NATIVE_INTERSTITIAL,
                ExternalSdkAdType.FB_NATIVE_INTERSTITIAL_BID -> true
                else -> false
            }
        }

        private fun isDfpNativeAd(baseDisplayAdEntity: BaseAdEntity?): Boolean {
            return if (baseDisplayAdEntity is ExternalSdkAd) {
                baseDisplayAdEntity.nativeAdObject is com.google.android.gms.ads.nativead.NativeAd
            } else false
        }

        @JvmStatic
        fun getRequestSwipeCountFromHandshake(defaultValue: Int): Int {
            val pgiAdsConfig = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.pgiAdConfig
                    ?: return defaultValue

            val requestSwipeCount =
                    when (ConnectionManager.getInstance().getCurrentConnectionSpeed(CommonUtils.getApplication())) {
                        ConnectionSpeed.GOOD, ConnectionSpeed.FAST -> pgiAdsConfig.requestSwipeCountGood
                        ConnectionSpeed.AVERAGE -> pgiAdsConfig.requestSwipeCountAverage
                        else -> pgiAdsConfig.requestSwipeCountSlow
                    }
            return if (requestSwipeCount >= 0) requestSwipeCount else defaultValue
        }

        @JvmStatic
        fun getRequestSwipeCountFromAd(adEntity: BaseDisplayAdEntity?, defaultValue: Int): Int {
            adEntity ?: return defaultValue

            return when (ConnectionManager.getInstance().getCurrentConnectionSpeed(CommonUtils.getApplication())) {
                ConnectionSpeed.GOOD, ConnectionSpeed.FAST -> adEntity.requestSwipeCountGood
                ConnectionSpeed.AVERAGE -> adEntity.requestSwipeCountAverage
                else -> adEntity.requestSwipeCountSlow
            } ?: defaultValue
        }

        @JvmStatic
        fun createTimeoutHelper(externalAdResponse: ExternalAdResponse,
                                tag: String): AdsTimeoutHelper {
            return AdsTimeoutHelper(AdsTimeoutHelper.TimeoutListener {
                AdLogger.d(TAG, "$tag ad timeout")
                externalAdResponse.onResponse(null)
            }, adRequestTimeout)
        }

        @JvmStatic
        fun isBuzzZone(adPosition: AdPosition?): Boolean {
            return adPosition == AdPosition.INLINE_VIDEO ||
                    adPosition == AdPosition.INSTREAM_VIDEO || adPosition == AdPosition.VDO_PGI
        }

        /**
         * News section adPositions for which validation rules are applicable.
         *
         * @param adPosition
         * @return
         */
        @JvmStatic
        fun isNewsZone(adPosition: AdPosition?): Boolean {
            return when (adPosition ?: return false) {
                AdPosition.P0, AdPosition.PP1, AdPosition.CARD_P1, AdPosition.PGI,
                AdPosition.STORY, AdPosition.SUPPLEMENT, AdPosition.MASTHEAD -> true
                else -> false
            }
        }

        /**
         * A valid buzz ad is of type following zones and ad types.
         *
         * @param externalSdkAd
         * @return
         */
        @JvmStatic
        fun isValidBuzzAd(externalSdkAd: ExternalSdkAd?): Boolean {
            if (externalSdkAd == null) {
                return false
            }

            val externalSdkAdType = ExternalSdkAdType.fromAdType(externalSdkAd.external?.data)
                    ?: return false

            when (externalSdkAd.adPosition ?: return false) {
                AdPosition.INLINE_VIDEO -> return when (externalSdkAdType) {
                    ExternalSdkAdType.INLINE_VIDEO_AD -> true
                    else -> false
                }
                AdPosition.VDO_PGI -> return externalSdkAdType == ExternalSdkAdType.IMA_SDK

                AdPosition.INSTREAM_VIDEO -> return when (externalSdkAdType) {
                    ExternalSdkAdType.IMA_SDK -> true
                    else -> false
                }
                else -> return false
            }
        }

        /**
         * Check validity of an adEntity for News section
         *
         * @param adEntity
         * @return
         */
        @JvmStatic
        fun isValidNewsAd(adEntity: BaseAdEntity?): Boolean {
            when (adEntity?.adPosition ?: return false) {
                AdPosition.P0, AdPosition.PP1, AdPosition.STORY, AdPosition.CARD_P1,
                AdPosition.PGI, AdPosition.SUPPLEMENT, AdPosition.MASTHEAD -> return true
                else -> return false
            }
        }

        /**
         * Check validity of an adEntity for splash-exit zone
         *
         * @param adEntity
         * @return
         */
        @JvmStatic
        fun isValidExitSplashAd(adEntity: BaseAdEntity?): Boolean {
            return when (adEntity?.type) {
                AdContentType.EMPTY_AD,
                AdContentType.IMAGE_LINK,
                AdContentType.MRAID_EXTERNAL,
                AdContentType.MRAID_ZIP -> true
                AdContentType.EXTERNAL_SDK -> if (adEntity is ExternalSdkAd) {
                    when (adEntity.external?.data) {
                        ExternalSdkAdType.DFP_INTERSTITIAL.adType,
                        ExternalSdkAdType.FB_INTERSTITIAL_AD.adType,
                        ExternalSdkAdType.AMAZON_INTERSTITIAL.adType,
                        ExternalSdkAdType.FB_NATIVE_INTERSTITIAL.adType,
                        ExternalSdkAdType.FB_NATIVE_INTERSTITIAL_BID.adType,
                        ExternalSdkAdType.DFP_NATIVE_INTERSTITIAL.adType -> true
                        else -> false
                    }
                } else false
                else -> false
            }
        }

        @JvmStatic
        fun removeClickListenerFromAllChilds(viewGroup: ViewGroup) {
            for (i in 0 until viewGroup.childCount) {
                viewGroup.getChildAt(i).let {
                    if (it.tag != AdConstants.AD_GENERIC_VIEW) {
                        it.setOnClickListener(null)
                    }
                }
            }
            viewGroup.setOnClickListener(null)
        }

        @JvmStatic
        fun getAdReportInfo(data: NativeData?): AdReportInfo? {
            data ?: return null

            val adReportInfo = AdReportInfo()
            adReportInfo.adTitle = data.title
            adReportInfo.adDescription = data.body
            adReportInfo.advertiser = data.advertiser
            adReportInfo.category = data.category
            return adReportInfo
        }

        @JvmStatic
        fun getIntValue(value: Int?, defaultValue: Int = AdConstants.AD_NEGATIVE_DEFAULT): Int {
            value?.let {
                return if (value <= AdConstants.AD_NEGATIVE_DEFAULT) {
                    defaultValue
                } else {
                    value
                }
            }
            return defaultValue
        }

        /**
         * Filter out zones to be blocked.
         * @param adSpec : adSpec of the entity/post.
         * @param supportedZones : all zones supported by entity/post
         * @param entityId : EntityId/postId, for logging
         * @param logTag: tag, for logging
         *
         */
        fun filterBlockedZones(adSpec: AdSpec?, supportedZones: MutableSet<String>,
                               entityId: String?, logTag: String, subSlots: MutableList<String>? = null) {
            adSpec?.adZones?.toHide?.forEach { item ->
                if (item.showIf == null || item.showIf?.operand == null
                        || item.showIf?.rules?.isEmpty() == true) {
                    when (item.zone) {
                        AdPosition.P0.value,
                        AdPosition.CARD_P1.value,
                        AdPosition.PGI.value,
                        AdPosition.MASTHEAD.value,
                        AdPosition.STORY.value -> {
                            Logger.d(logTag, "Blocked ${item.zone} zone $entityId ")
                            supportedZones.remove(item.zone)
                        }
                        AdPosition.PP1.value,
                        AdPosition.SUPPLEMENT.value -> {
                            item.subSlots?.let {
                                Logger.d(logTag, "Blocked supplement tags : $it  in $entityId ")
                                subSlots?.removeAll(it)
                            }
                            if (subSlots == null || subSlots.isEmpty()) {
                                supportedZones.remove(item.zone)
                            }
                        }
                    }
                }
            }
        }

        /**
         * Update requiredTagcount for supplement zone request.
         */
        fun updateRequiredAdTagsFromCache(adRequest: AdRequest?, cacheSize: Int) {
            if (adRequest == null || (adRequest.zoneType != AdPosition.SUPPLEMENT && adRequest.zoneType != AdPosition.PP1) ||
                    CommonUtils.isEmpty(adRequest.localRequestedAdTags)) {
                return
            }
            var tagOrder: List<String>? = null
            when (adRequest.zoneType) {
                AdPosition.SUPPLEMENT -> {
                    tagOrder = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.supplementAdConfig?.tagOrder

                }
                AdPosition.PP1 -> {
                    tagOrder = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.cardPP1AdsConfig?.tagOrder
                }
                else -> {
                }
            }
            if (tagOrder.isNullOrEmpty()) {
                return
            }
            val cachePerTag = cacheSize / tagOrder.size
            val requiredAdTags = ConcurrentHashMap<String, Int>()
            for (tag in tagOrder) {
                when (adRequest.zoneType) {
                    AdPosition.SUPPLEMENT -> {
                        requiredAdTags[tag] = cachePerTag
                    }
                    AdPosition.PP1 -> {
                        requiredAdTags[tag] = 1
                    }
                    else -> {
                        return
                    }
                }
            }
            adRequest.requiredAdtags = requiredAdTags
        }

        /**
         * @param adEntity base display ad entity
         * @param borderContainerView border container made of card view
         * @param bottomBanner        bottom banner of view holder
         */
        @JvmStatic
        fun setupAdBorder(adEntity: BaseDisplayAdEntity?,
                          borderContainerView: View?, bottomBanner: View? = null) {
            if (borderContainerView !is NHRoundedFrameLayout) {
                return
            }
            var borderColor = getAdBorderColor(adEntity)
            if (adEntity?.showBorder == true) {
                val borderSize = CommonUtils.getDimension(R.dimen.ad_container_border_width)
                borderContainerView.setStroke(borderSize, borderColor)
            } else {
                borderContainerView.setStroke(0, 0)
            }
            bottomBanner?.let {
                setupAdBottomBanner(bottomBanner, borderColor)

            }
        }

        @JvmStatic
        fun setUpAdContainerBackground(
                baseDisplayAdEntity: BaseDisplayAdEntity?, container: View?) {
            val containerBg = baseDisplayAdEntity?.containerBackgroundColor
            val containerBorder = baseDisplayAdEntity?.containerBorderColor
            if (container is NHRoundedFrameLayout) {
                if (containerBg != null && containerBorder != null) {
                    container.fillColor(
                            ViewUtils.getColor(containerBg))
                } else {
                    container.fillColor(
                            ThemeUtils.getThemeColorByAttribute(container.context,
                                    R.attr.ads_container_background_color))
                }
            }

        }

        @JvmStatic
        fun setUpAdContainerBorder(
                baseDisplayAdEntity: BaseDisplayAdEntity?, container: View?) {
            val borderSize = CommonUtils.getDimension(R.dimen.ad_border_width)
            val containerBg = baseDisplayAdEntity?.containerBackgroundColor
            val containerBorder = baseDisplayAdEntity?.containerBorderColor
            if (container is NHRoundedFrameLayout) {
                if (containerBg != null && containerBorder != null) {
                    container.setStroke(borderSize, ViewUtils.getColor(containerBorder))
                } else {
                    container.setStroke(borderSize, ThemeUtils.getThemeColorByAttribute(container.context,
                            R.attr.ads_container_border_color))
                }
            }

        }

        @JvmStatic
        fun setUpAdContainerBackgroundWithBorder(
                baseDisplayAdEntity: BaseDisplayAdEntity?, container: View): GradientDrawable {
            val containerBackgroundColor = baseDisplayAdEntity?.containerBackgroundColor
            val containerBorderColor = baseDisplayAdEntity?.containerBorderColor
            val drawableBackground = GradientDrawable()
            drawableBackground.shape = GradientDrawable.RECTANGLE
            if (containerBackgroundColor != null && containerBorderColor != null) {
                drawableBackground.setColor(ViewUtils.getColor(containerBackgroundColor)!!.toInt())
                drawableBackground.setStroke(CommonUtils.getDimension(R.dimen.readmore_btn_stroke_width),
                        ViewUtils.getColor(containerBorderColor)!!)
            } else {
                drawableBackground.setColor(ThemeUtils.getThemeColorByAttribute(container.context,
                        R.attr.ads_container_background_color))
                drawableBackground.setStroke(CommonUtils.getDimension(R.dimen.readmore_btn_stroke_width),
                        ThemeUtils.getThemeColorByAttribute(container.context,
                                R.attr.ads_container_border_color))
            }
            return drawableBackground
        }

        /**
         * @param baseDisplayAdEntity Ad response data
         * @return Ad border color fallback color.
         */
        private fun getAdBorderColor(baseDisplayAdEntity: BaseDisplayAdEntity?): Int {
            var responseColor: String? = null
            baseDisplayAdEntity?.let {
                responseColor = it.adBorderColor
            }
            val responseParsedColor = ViewUtils.getColor(responseColor)
            responseParsedColor?.let {
                return responseParsedColor.toInt()
            }
            val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
            val handshakeColor = adsUpgradeInfo?.adBorderColor

            return ViewUtils.getColor(handshakeColor, CommonUtils.getColor(R.color.ad_border_color))
        }

        /**
         * @param bottomBanner setup UI for bottom Banner in Ads
         * @param borderColor  set explicit border color
         */
        private fun setupAdBottomBanner(bottomBanner: View?, borderColor: Int?) {
            bottomBanner ?: return

            val chooseStrokeColor: Int = borderColor ?: getAdBorderColor(null)
            val cornerRadius = CommonUtils.getDimension(R.dimen.ad_image_corner_radius)
            val fillColor = ThemeUtils.getThemeColorByAttribute(bottomBanner.context, R.attr
                    .ad_bottom_banner_fill_color)
            val strokeWidth = CommonUtils.getDimension(R.dimen.ad_border_width)
            //top-left, top-right, bottom-right, bottom-left
            val radii = floatArrayOf(0f, 0f, 0f, 0f, cornerRadius.toFloat(),
                    cornerRadius.toFloat(), cornerRadius.toFloat(), cornerRadius.toFloat())
            val bannerBackground = AndroidUtils.makeRoundedRectDrawable(radii, fillColor, strokeWidth,
                    chooseStrokeColor)
            bottomBanner.background = bannerBackground
        }

        fun getCardTypeForAds(adEntity: BaseAdEntity): Int {
            if (isExternalSdkNativePgiAd(adEntity)) {
                return when {
                    isDfpNativeAd(adEntity) -> AdDisplayType.NATIVE_DFP_AD.index
                    isFbNativeAd(adEntity) -> AdDisplayType.AD_FB_NATIVE.index
                    else -> AdDisplayType.EXTERNAL_NATIVE_PGI.index
                }
            }
            if (isIMAVideoAd(adEntity)) {
                return AdDisplayType.IMA_VIDEO_AD.index
            }
            val template = adEntity.adTemplate

            if (isNativeAd(adEntity)) {
                return when (template) {
                    AdTemplate.HIGH -> AdDisplayType.NATIVE_HIGH_AD
                    AdTemplate.ENHANCED_HIGH -> AdDisplayType.NATIVE_ENHANCED_HIGH_AD
                    else -> AdDisplayType.NATIVE_AD
                }.index
            }
            if (isDfpNativeAd(adEntity)) {
                return if (template == AdTemplate.HIGH) {
                    AdDisplayType.NATIVE_DFP_HIGH_AD.index
                } else AdDisplayType.NATIVE_DFP_AD.index
            }
            if (isFbNativeAd(adEntity)) {
                return if (template == AdTemplate.HIGH)
                    AdDisplayType.AD_FB_NATIVE_HIGH.index
                else
                    AdDisplayType.AD_FB_NATIVE.index
            }
            if (isHtmlAd(adEntity)) {
                return if (isFullScreenAd(adEntity))
                    AdDisplayType.HTML_AD_FULL.index
                else AdDisplayType.HTML_AD.index
            }
            if (adEntity.type == AdContentType.EMPTY_AD) {
                return AdDisplayType.EMPTY_AD.index
            }
            if (isImageAd(adEntity)) {
                return if (isFullScreenAd(adEntity))
                    AdDisplayType.IMAGE_LINK_FULL.index
                else
                    AdDisplayType.IMAGE_LINK.index
            }
            try {
                adEntity.type?.let {
                    return AdDisplayType.valueOf(it.name).index
                }
            } catch (ex: java.lang.Exception) {
                Logger.caughtException(ex)
            }
            return -1
        }

        private fun isImageAd(adEntity: BaseAdEntity?): Boolean {
            return when (adEntity?.type) {
                AdContentType.IMAGE_LINK -> true
                else -> false
            }
        }

        private fun isHtmlAd(adEntity: BaseAdEntity?): Boolean {
            return when (adEntity?.type) {
                AdContentType.MRAID_ZIP,
                AdContentType.MRAID_EXTERNAL,
                AdContentType.PGI_ZIP,
                AdContentType.PGI_EXTERNAL -> true
                else -> false
            }
        }

        fun isFullScreenAd(adEntity: BaseAdEntity?): Boolean {
            return when (adEntity?.adPosition) {
                AdPosition.SPLASH,
                AdPosition.PGI,
                AdPosition.EXIT_SPLASH -> true
                else -> false
            }
        }

        /**
         * @param adPosition     Ad position
         * @return Cache Time to live for ads which have given adPosition.
         */
        fun getCacheTTL(adPosition: AdPosition): Long {
            val ttl = getAdsConfig(adPosition)?.cacheTTL ?: 0L
            return if (ttl <= 0) {
                if (adPosition == AdPosition.EVERGREEN) -1L else AdConstants.ADS_DEFAULT_CACHE_TTL
            } else ttl
        }

        /**
         * @param adPosition     Ad position
         * @param adsUpgradeInfo Ads upgrade info
         * @return Boolean which defines whether to prefetch ads for this zone or not
         */
        @JvmStatic
        fun isPrefetchEnabled(adPosition: AdPosition,
                              adsUpgradeInfo: AdsUpgradeInfo?): Boolean {
            if (adsUpgradeInfo == null) {
                return false
            }
            if (adPosition != AdPosition.P0 && adPosition != AdPosition.PP1) {
                return false
            }
            return when (adPosition) {
                AdPosition.P0 -> {
                    val adsConfig = adsUpgradeInfo.cardP0AdConfig
                    adsConfig?.isEnablePrefetch ?: false
                }
                AdPosition.PP1 -> {
                    val adsConfig = adsUpgradeInfo.cardPP1AdsConfig
                    adsConfig?.isEnablePrefetch ?: false
                }
                else -> {
                    false
                }
            }

        }

        @JvmStatic
        fun buildAdServerURL(adRequest: AdRequest): String {
            return AdUrl(adRequest).toString()
        }

        /**
         * @param externalSdkAd External Sdk Ad item
         * @return ItemTag for external Sdk Ad
         */
        @JvmStatic
        fun getExternalSdkAdItemTag(externalSdkAd: ExternalSdkAd?): String {
            val externalTag = externalSdkAd?.external
            if (!externalTag?.itemTag.isNullOrBlank()) {
                return externalTag?.itemTag!!
            }
            val contentTag = externalSdkAd?.content
            return if (!contentTag?.itemTag?.data.isNullOrBlank()) {
                contentTag?.itemTag?.data!!
            } else Constants.EMPTY_STRING

        }

        @JvmStatic
        fun makeTitleUnBold(title: String): Boolean {
            val titleBoldHelper = TitleBoldHelper()
            try {
                return !titleBoldHelper.shouldShowBoldTitle(title)
            } catch (e: Exception) {
                Logger.caughtException(e)
            }
            return false
        }

        @JvmStatic
        fun getContentContextFor(adSpec: AdSpec?, adPosition: String): ContentContext? {
            adSpec?.contentContexts?.entries?.forEach { entry ->
                if (adPosition.equals(entry.key, true)) {
                    return entry.value
                }
            }
            return null
        }

        @JvmStatic
        fun toPostEntity(ad: BaseAdEntity): PostEntity {
            return PostEntity(id = ad.uniqueAdIdentifier, format = ad.i_format(), subFormat = ad.i_subFormat(),type = ad.i_type())
        }

        @JvmStatic
        fun getMinimumAutoplayVisibilityForSdk(sdk: String?): Int {
            val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
            if (sdk.isNullOrBlank() || adsUpgradeInfo == null) {
                return AdConstants.MIN_VISIBILITY_AUTOPLAY_VIDEO
            }
            var autoplayVisibility = 0

            val sdkAutoplayPercent = adsUpgradeInfo.sdkMinimumAutoplayPercent
            if (!sdkAutoplayPercent.isNullOrEmpty() && sdkAutoplayPercent.containsKey(sdk)) {
                autoplayVisibility = sdkAutoplayPercent[sdk] ?: 0
            }
            return if (autoplayVisibility > 0) autoplayVisibility else AdConstants.MIN_VISIBILITY_AUTOPLAY_VIDEO
        }

        private fun getAdsConfig(adPosition: AdPosition): AdsConfig? {
            val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
                    ?: return null
            return when (adPosition) {
                AdPosition.P0 -> adsUpgradeInfo.cardP0AdConfig
                AdPosition.PP1 -> adsUpgradeInfo.cardPP1AdsConfig
                AdPosition.CARD_P1 -> adsUpgradeInfo.cardP1AdConfig
                AdPosition.STORY -> adsUpgradeInfo.storyPageAdConfig
                AdPosition.MASTHEAD -> adsUpgradeInfo.mastheadAdConfig
                AdPosition.SUPPLEMENT -> adsUpgradeInfo.supplementAdConfig
                AdPosition.PGI -> adsUpgradeInfo.pgiAdConfig
                else -> null
            }
        }

        fun getAdSlotName(tag: String?, adPosition: AdPosition): String {
            return adPosition.value.plus(AdConstants.AD_TOKEN).plus(tag)
        }

        fun checkShowIf(elementList: ArrayList<String>, zoneConfig: ZoneConfig?): Boolean {
            val showIf = zoneConfig?.showIf ?: return true
            var isValid = false
            showIf.rules?.forEach { adRule ->
                adRule.on?.let {
                    when (showIf.operand) {
                        OperandType.AND.name -> {
                            isValid = true
                            if (!elementList.contains(adRule.element) || !enumContains<ShowOn>(it)) {
                                return false
                            }
                        }
                        OperandType.OR.name -> {
                            if (elementList.contains(adRule.element) || enumContains<ShowOn>(it)) {
                                return true
                            }
                        }
                    }
                }
            }
            return isValid
        }

        fun hitAdRespondedUrl(baseAdEntity: BaseAdEntity) {
            val ad = when (baseAdEntity) {
                is BaseDisplayAdEntity -> {
                    baseAdEntity
                }
                is MultipleAdEntity -> {
                    baseAdEntity.baseDisplayAdEntities.firstOrNull()
                }
                else -> null
            }
            ad?.adRespondedBeaconUrl?.let {
                AsyncAdImpressionReporter(ad).hitTrackerUrl(it)
            }
        }

        /**
         * Check if impression frequency cap has been reached for an ad.
         */
        fun isFCLimitReachedForAd(adEntity: BaseAdEntity?, uniqueRequestId: Int = -1,
                                  useSoftCounter: Boolean = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.enableSoftCounter
                                      ?: false): Boolean {
            val campaignFC = getCapId(adEntity, AdFCType.CAMPAIGN)?.let { capId ->
                isFCLimitReached(AdFCType.CAMPAIGN, capId, uniqueRequestId, useSoftCounter)
            } ?: false
            val bannerFC = getCapId(adEntity, AdFCType.BANNER)?.let { capId ->
                isFCLimitReached(AdFCType.BANNER, capId, uniqueRequestId, useSoftCounter)
            } ?: false
            return campaignFC || bannerFC
        }

        fun isFCLimitReached(type: AdFCType, capId: String, uniqueRequestId: Int = -1,
                             useSoftCounter: Boolean = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.enableSoftCounter
                                 ?: false): Boolean {
            var logger: AdContextLogger? = null
            return AdFrequencyStats.getFcData(type, capId)?.let { fcData ->
                try {
                    if (Logger.loggerEnabled()) {
                        logger = AdContextLogger()
                    }
                    val exhausted = FCEngine.isLimitReached(fcData.campaignId, fcData,
                        AdFrequencyStats.getImpressionCount(fcData, uniqueRequestId, useSoftCounter), logger)
                    if (exhausted) {
                        //Check if FC cap can be reset now.
                        return !AdFrequencyStats.canFCDataResetFor(type, capId)
                    }
                    exhausted
                } catch (ex: Exception) {
                    Logger.caughtException(ex)
                    ex.printStackTrace()
                    false
                }
            } ?: false
        }

        fun getCapId(ad: BaseAdEntity?, type: AdFCType): String? {
            ad ?: return null
            return when (type) {
                AdFCType.CAMPAIGN -> ad.campaignId
                AdFCType.BANNER -> if (ad is BaseDisplayAdEntity) {
                    ad.bannerId
                } else if (ad is MultipleAdEntity) {
                    ad.baseDisplayAdEntities.firstOrNull()?.bannerId
                } else null
            }
        }

        fun getErrorCodeFor(adErrorType: AdErrorType): Int? {
            val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
            adsUpgradeInfo?.errorCodes?.let {
                if (it.containsKey(adErrorType.name)) {
                    return it[adErrorType.name]
                }
            }
            return adErrorType.value
        }

        fun getPersistedSplashFile(): File? {
            val splashDirPath = CommonUtils.getApplication().filesDir.toString() + File.separator + Constants.SPLASH_DIR + File.separator
            val splashFilePath = splashDirPath + Constants.SPLASH_FILE

            val splashDir = File(splashDirPath)
            if (!splashDir.exists()) {
                splashDir.mkdir()
            }
            return if (splashDir.exists()) {
                return File(splashFilePath)
            } else {
                null
            }
        }

        /**
         * To calculate the ad Inserted index for list ads
         */
        fun computeAdInsertedIndex(adEntity: BaseDisplayAdEntity, position: Int): Int {
            return when (adEntity.adPosition) {
                AdPosition.P0, AdPosition.CARD_P1, AdPosition.PP1 -> position
                else -> -1
            }
        }

        /**
         * To be called only on upgrade.
         */
        @JvmStatic
        fun deletePersistedSplashFile(): Observable<Boolean> {
            val successObs = Observable.just(true)
            val splashFile = getPersistedSplashFile()?: return successObs
            return if (splashFile.exists()) {
                Observable.fromCallable {
                    val deleted = splashFile.delete()
                    Logger.d(TAG, "deletePersistedSplashFile: deleted=$deleted,file=${splashFile.absolutePath}")
                    if (deleted) {
                        /*below 2 prefs are set by SplashBitmapTarget */
                        PreferenceManager.remove(GenericAppStatePreference.SPLASH_IMAGE_URL)
                        PreferenceManager.remove(GenericAppStatePreference.DEFAULT_SPLASH_TIMEOUT)
                    }
                    deleted
                }.subscribeOn(Schedulers.io())
            } else successObs
        }

        fun saveMediaToStorage(bitmap: Bitmap) : File {
            val filename = "${System.currentTimeMillis()}.jpg"

            val cachePath = File(CommonUtils.getApplication().externalCacheDir?.path, AdConstants.SCREENSHOT_DIR)
            if(!cachePath.exists()) {
                cachePath.mkdirs()
            }

            val screenshotFile = File(cachePath, filename)
            if(screenshotFile.exists()) {
                screenshotFile.delete()
            }
            screenshotFile.also { file ->
                FileOutputStream(file).use { fileOutputStream -> bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fileOutputStream) }
            }.apply {
                deleteOnExit()
            }

            return screenshotFile;
        }

        fun getPodAdId(ad: Ad?) : String {
            var answer = Constants.EMPTY_STRING
            var podIndex = ad?.adPodInfo?.podIndex
            var adPosition = ad?.adPodInfo?.adPosition

            answer = if(podIndex != 0 && podIndex != -1) {
                answer.plus(AdConstants.MID_ROLL_POD).plus(podIndex).plus(Constants.UNDERSCORE_CHARACTER).plus(adPosition)
            } else {
                answer.plus(instreamAdSourceIdMap[podIndex]).plus(Constants.UNDERSCORE_CHARACTER).plus(adPosition)
            }
            IAdLogger.d(TAG, "key to fetch instream pod ad data : $answer")
            return answer
        }

        @JvmStatic
        fun getMediaItemFromAds(baseAdEntity: BaseAdEntity): MediaItem? {
            val prefetchDur = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.prefetchDurationAds
            prefetchDur ?: return null

            if(baseAdEntity is ExternalSdkAd) {
                baseAdEntity.external?.mediaFileURL ?: return null
                return MediaItem(
                    Uri.parse(baseAdEntity.external?.mediaFileURL), baseAdEntity.uniqueAdIdentifier.toString(), false
                ).also {
                    it.prefetchDuration = prefetchDur
                }
            }
            return null
        }

        fun makeAmazonAdRequest(adPosition: AdPosition) {
            val apsInfo: ApsInfo? = AdsUpgradeInfoProvider.getInstance()?.adsUpgradeInfo?.amazonSDK
            if(AdRegistration.isInitialized()) {
                apsInfo?.let {
                    val bannerInfo = apsInfo.banner?.sizes
                    // traverse through each size list item
                    bannerInfo?.forEach { item ->
                        val width = item.width
                        val height = item.height
                        val zoneInfo = item.data
                        // traverse through each adPosition inside size list item
                        zoneInfo?.forEach { zoneItem ->
                            val slotInfo = zoneItem.slotInfo
                            val position = zoneItem.adPosition
                            if (position == adPosition.value) {
                                // traverse through each slots present for given adPosition
                                slotInfo?.forEach { slotItem ->
                                    val slotUUID = slotItem.slotAdUnitId
                                    val tag = slotItem.tag
                                    slotUUID.let { slotId ->
                                        AmazonAdFetcher().fetchBannerAd(width, height, slotId, position, tag)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                AdLogger.d(TAG, "Amazon sdk is not initialized")
            }
        }

        fun getAmazonRequestBody(adPosition: AdPosition) : MutableList<HashMap<String, List<AmazonBidPayload>>>{
            val requestBodyList: MutableList<HashMap<String, List<AmazonBidPayload>>> = mutableListOf()
            val tagToBidMapping = HashMap<String, List<AmazonBidPayload>>()
            var slotIds : List<String>?
            var key = Constants.DEFAULT

            if (adPosition == AdPosition.SUPPLEMENT) {
                val supplementSubSlots = mutableListOf<String>()
                AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.supplementAdConfig?.tagOrder?.let {
                    if (it.isNotEmpty()) {
                        supplementSubSlots.addAll(it)
                    }
                }
                supplementSubSlots.forEach { tag ->
                    key = getAdSlotName(tag, adPosition)
                    slotIds = AmazonBidUtilities.fetchSlotUUIDForZone(key)
                    val amazonBidList = mutableListOf<AmazonBidPayload>()
                    slotIds?.forEach { slotId ->
                        if (!AmazonBidUtilities.isExpiredBid(slotId)) {
                            val bid = AmazonBidUtilities.fetchBid(slotId)
                            bid?.let {
                                amazonBidList.add(
                                    AmazonBidPayload(
                                        slotUUID = slotId,
                                        amazonHost = it[AdConstants.AMAZON_HOST],
                                        amazonSlot = it[AdConstants.AMAZON_PRICEPOINTS],
                                        amazonBid = it[AdConstants.AMAZON_BID_ID],
                                        amazonp = it[AdConstants.AMAZON_HASHED_BIDDER_ID],
                                        dc = it[AdConstants.AMAZON_DATA_CENTER]
                                    )
                                )
                            }
                        } else {
                            AmazonBidUtilities.clearBidInfo(slotId)
                        }
                    }
                    if(amazonBidList.size > 0)
                        tagToBidMapping[tag] = amazonBidList
                }
            } else {
                slotIds = AmazonBidUtilities.fetchSlotUUIDForZone(adPosition.value)
                val amazonBidList = mutableListOf<AmazonBidPayload>()
                slotIds?.forEach { slotId ->
                    if (!AmazonBidUtilities.isExpiredBid(slotId)) {
                        val bid = AmazonBidUtilities.fetchBid(slotId)
                        bid?.let {
                            amazonBidList.add(
                                AmazonBidPayload(
                                    slotUUID = slotId,
                                    amazonHost = it[AdConstants.AMAZON_HOST],
                                    amazonSlot = it[AdConstants.AMAZON_PRICEPOINTS],
                                    amazonBid = it[AdConstants.AMAZON_BID_ID],
                                    amazonp = it[AdConstants.AMAZON_HASHED_BIDDER_ID],
                                    dc = it[AdConstants.AMAZON_DATA_CENTER]
                                )
                            )
                        }
                    } else {
                        AmazonBidUtilities.clearBidInfo(slotId)
                    }
                }
                if(amazonBidList.size > 0) {
                    tagToBidMapping[key] = amazonBidList
                }
            }
            if(tagToBidMapping.size > 0)
                requestBodyList.add(tagToBidMapping)

            return requestBodyList
        }
    }
}

enum class ShowOn {
    LOADED
}

enum class OperandType {
    AND,
    OR
}

inline fun <reified T : Enum<T>> enumContains(name: String?): Boolean {
    return enumValues<T>().any { it.name == name }
}
