/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.adupgrade


import com.google.gson.annotations.SerializedName
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.BrowserType
import com.newshunt.dataentity.dhutil.model.entity.upgrade.PublicEncryptionKey

import java.io.Serializable

/**
 * Represents the ads information currently available on the server. Clients
 * can use this information to check if upgrade is needed or not.
 *
 * @author raunak.yadav
 */
class AdsUpgradeInfo : Serializable {

    /**
     * version to be stored by the client
     */
    var version: String? = null

    /**
     * Configuration for card-p0 ads
     */
    @SerializedName("card-p0")
    val cardP0AdConfig: AdsConfig? = null

    /**
     * Configuration for card-p1 ads
     */
    @SerializedName("card-p1")
    val cardP1AdConfig: AdsConfig? = null

    /**
     * Configuration for storypage ads
     */
    @SerializedName("storypage")
    val storyPageAdConfig: StorypageAdConfig? = null

    /**
     * Configuration for supplement ads
     */
    @SerializedName("supplement")
    val supplementAdConfig: SupplementAdsConfig? = null

    @SerializedName("card-pp1")
    val cardPP1AdsConfig: PP1AdsConfig? = null
    /**
     * Configuration for pgi ads
     */
    @SerializedName("pgi")
    val pgiAdConfig: PgiAdsConfig? = null
    /**
     * Configuration for splash-exit ads
     */
    @SerializedName("splash-exit")
    val splashExitConfig: AdsConfig? = null

    /**
     * Timeouts based on n/w speed for Ads sdks.
     */
    @SerializedName("sdkTimeout")
    val adsSdkTimeout: AdsSdkTimeout? = null

    @SerializedName("masthead")
    val mastheadAdConfig: AdsConfig? = null

    /**
     * Flag which tells whether to refresh card-p0 ad on pull to refresh or no
     */
    @SerializedName("cardP0Refresh")
    val isCardP0Refresh: Boolean = false

    /**
     * Tells after how many card scrolls we should make next ad request once we get no-fill or
     * empty ad
     */
    val cardP1NoFillRetryDistance: Int = 0

    /**
     * Count after how many card dwipes should we request next ad once we get no-fill or
     * empty ad
     */
    val pgiNoFillRetrySwipeCount: Int = 0

    /**
     * Params to enable skippable Ads in Buzz
     */
    var buzzAd: TvAdData? = null


    /**
     * Params to Instream Ads in DHTV
     */
    @SerializedName("dhtv")
    val dhTvAdConfig: DHTVAdConfig? = null

    /**
     * Params for Instream Ads across app.
     */
    @SerializedName("instream-vdo")
    val instreamAdsConfig: InstreamAdsConfig? = null

    /**
     * Delay in minutes between 2 posts of device data to Ads Backend
     */
    val deviceDataPostDelayMin = DEFAULT_DEVICE_DATA_POST_DELAY

    /**
     * Number of samples sent to Ads backend for each kind of data collection like Battery, data
     * usage etc.
     */
    val deviceDataSampleSize = DEFAULT_DEVICE_DATA_SAMPLE_SIZE

    var facebookPermissions: List<String>? = null

    /**
     * Switch to turn off data collection from backend
     */
    @SerializedName("enableDataCollection")
    val isDataCollectionEnabled = true

    /**
     * Switch to turn off soft counter logic for Frquency Cap calculations.
     */
    val enableSoftCounter = true
    /**
     * Sync ad campaigns info Interval
     */
    val campaignSyncFgIntervalInMinutes: Long? = -1L
    /**
     * Sync ad campaigns info Interval for background
     */
    val campaignSyncBgIntervalInMinutes: Long? = -1L
    /**
     * Url for fetching ad campaigns config.
     */
    val fetchCampaignDataUrl: String? = null
    /**
     * Default list of preferred browsers sent by B.E to open the Ads landing url
     */
    val externalBrowsers: List<String>? = null

    /**
     * Image ad data to be displayed if video ads fail to load.
     */
    val videoAdFallback: VideoAdFallback? = null

    var isPageLoadedBeaconNeeded: Boolean = false

    val omSdkConfig: OmSdkConfig? = null

    val adBorderColor: String? = null

    @SerializedName("selfService")
    val selfService: SelfServiceConfig? = null

    val minVisibilityForAutoplay = 20
    val defaultBrowser = BrowserType.EXTERNAL_BROWSER.getName()

    @SerializedName("enableOmidExperimentally")
    val isEnableOmidExperimentally: Boolean = false

    val minAspectRatioNativeHighTemplate: Float = 0f

    val minAspectRatioVideo: Float = 0f

    /**
     * Minimum % for autoplaying per ad sdk
     */
    val sdkMinimumAutoplayPercent: Map<String, Int>? = null

    @SerializedName("enableFBBidding")
    var isEnableFBBidding = true

    /**
     * Maximum adRequests allowed to be triggered in parallel, per zone.
     */
    var maxParallelAdRequestCount = 2

    /*
    * TimeSpent On landing page threshold time
    * */
    val adLpTimespentTimeoutMS : Long? = null

    /**
      * Public encryption key
      */
     val encryption : PublicEncryptionKey? = null

    /**
     *  Report ads
     */
    @SerializedName("reportAdsMenu")
    val reportAdsMenuEntity : ReportAdsMenuEntity? = null

    /*
    *  Immersive view params
    * */
    @SerializedName("immersiveView")
    val immersiveView: ImmersiveAdsConfig? = null

    var errorCodes: Map<String, Int>? = null
    var enableErrorScreenShot: Boolean = false

    /*
     * count of zipped html ads file to be kept in cache
     * */
    val zippedHtmlAdCacheCount = 30

    /**
     * Flag to enable/disable apps on device data collection. Intentionally keeping the name
     * shortened to not reveal its purpose
     */
    val aodCollection: String? = AppsOnDeviceStatus.DISABLED.name

    /*
     * JSON to get all the needed information to make amazon server call
     */
    val amazonSDK: ApsInfo? = null

    val evergreenAds: EvergreenAdsConfig? = null

    val prefetchDurationAds: Float = 0F

    companion object {
        //Default sample size for device data collection. This is common for all kinds of data
        const val DEFAULT_DEVICE_DATA_SAMPLE_SIZE = 1
        //Default delay between 2 device data pings
        const val DEFAULT_DEVICE_DATA_POST_DELAY = 30
    }
}

enum class AppsOnDeviceStatus {
    ENABLED,
    ENABLED_AFTER_CONSENT,
    DISABLED;

    companion object {
        @JvmStatic
        fun fromName(name: String?): AppsOnDeviceStatus {
            values().forEach {
                if (CommonUtils.equalsIgnoreCase(it.name, name)) {
                    return it
                }
            }
            return DISABLED
        }
    }
}
class ApsInfo : Serializable {
    val banner: AmazonAdFormatData? = null
    val interstitial: AmazonAdFormatData? = null
    val cacheTTL: Int = 0
}

data class AmazonAdFormatData(val sizes: List<CreativeSize>)

data class CreativeSize(
    val width: Int,
    val height: Int,
    val data: List<ZoneInfo>?
)

data class ZoneInfo(
    val adPosition: String,
    val slotInfo: List<SlotInfo>?
)

data class SlotInfo(
    val tag: String? = null,
    val slotAdUnitId: String
)

