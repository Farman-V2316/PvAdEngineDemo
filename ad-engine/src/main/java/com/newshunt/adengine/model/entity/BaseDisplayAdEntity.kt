/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity

import `in`.dailyhunt.money.frequency.FCData
import com.google.gson.annotations.SerializedName
import com.newshunt.adengine.model.entity.omsdk.OMSessionState
import com.newshunt.adengine.model.entity.omsdk.OMTrackType
import com.newshunt.adengine.model.entity.omsdk.OMVendorInfo
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.model.entity.version.BannerFill
import com.newshunt.adengine.util.AdConstants
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.AdContextRules
import com.newshunt.dataentity.dhutil.model.entity.BrowserType
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.PgiAdsConfig
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.ReportAdsMenuFeedBackEntity
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

/**
 * Represents base ad entity for all ads in the system.
 * Assumes all ads will have attributes in its ad tag.
 *
 * If new fields are added to AdEntity that support macros, they must be processed
 * in {@link com.newshunt.adengine.util.AdMacroUtils}
 *
 * @author shreyas.desai
 */
open class BaseDisplayAdEntity : BaseAdEntity(), Serializable {
    override fun addLandingUrl(url: String) {
        landingUrlAdditional.add(url)
    }

    override val campaignId: String? = Constants.EMPTY_STRING
    val bannerId: String? = Constants.EMPTY_STRING
    var beaconUrlAdditional: MutableList<String?> = ArrayList()
    override var beaconUrl: String? = null
    var landingUrl: String? = null

    /**
     * Triggered when a valid ad Creative is received by the app.
     */
    var adRespondedBeaconUrl: String? = null

    /**
     * Triggered whenever the adView is inflated.
     */
    var adInflatedBeaconUrl: String? = null
    var errorUrl: String? = null
    var adReactionBeaconUrl: String? = null
    var adShareBeaconUrl: String? = null
    override var requestUrl: String? = null
    var landingUrlAdditional: MutableList<String> = ArrayList()
    var customVideoTrackers: CustomVideoTrackers? = null

    var uniqueAdId: Int = 0
    override val parentIds = HashSet<Int>()
    val customParameter = HashMap<String, String>() //todo mukesh need more work
    var contentBaseUrl: String? = null

    @Transient
    var omSessionState: MutableMap<Int, OMSessionState?>? = null
    // OM SDK: JsonArray of verification vendors' data
    var omVendorsInfo: List<OMVendorInfo>? = null
    var omImpressionFired: Boolean = false

    var shareability: Shareability? = null
    override var adContextRules: AdContextRules? = null //Context assigned based on the flush configurations
    //native ad part
    override var positionWithTicker: Int? = null
    var id: String? = null
    override var aduid: String? = null
    override var adGroupId: String? = null
    override var type: AdContentType? = null
    var span: Int? = null
    var spanInMS: Long? = null

    /**
     * number of swipes after which a PGI ad is normally shown.
     */
    var pageSwipeCount: Int? = null

    /**
     * Number of swipes after which to display first PGI ad.
     */
    var firstAdSwipeCount: Int? = null

    /**
     * Max swipes after which any cached PGI ad is discarded & new ad request is sent
     */
    var maxPageSwipeCount: Int? = null

    /**
     * swipeCounts after which to request pgi ad on good network
     */
    var requestSwipeCountGood : Int? = null

    /**
     * swipeCounts after which to request pgi ad on slow network
     */
    var requestSwipeCountSlow : Int? = null

    /**
     * swipeCounts after which to request pgi ad on average network
     */
    var requestSwipeCountAverage :Int? = null
    /**
     * minimum swipe count to show PGI ad in current session
     * regardless of previous session swipe count.
     */
    var minThresholdSwipeCount: Int? = null
    /**
     * Number of sessions for which we do not have to persist swipe count
     * across sessions to show pgi ads.
     */
    var sessionCount: Int? = null

    @PgiAdsConfig.HTMLPgiDisplayType
    var interstitialDisplayType: String? = null

    /**
     * Insert webview only when PGI fragment is visible to user, overriding the default behavior
     */
    var showHTMLPgiOnlyOnVisible: Boolean? = null
    override var dedupId: String? = null
    override var dedupDistance: Int? = null

    var showTitle: Boolean? = null
    var useInternalBrowser: BrowserType? = null
    var useWideViewPort: Boolean? = null
    var useDhFont: String? = null
    var showCount: Int? = null// This many times we can show saved splash ad.

    var adCacheGood: Int? = null // number of ads to be cached at good speed network
    var adCacheAverage: Int? = null // number of ads to be cached at average speed network
    var adCacheSlow: Int? = null // number of ads to be cached at slow speed network
    override var adTemplate: AdTemplate? = null
    var showBorder: Boolean? = null
    var containerBorderColor: String? = null
    var containerBackgroundColor: String? = null
    var adBorderColor: String? = null
    // Ad tag to specify subcategory within a zone like viewOrder for supplement-ads.
    override var adTag: String? = null
    var interactiveAd: Boolean? = null
    var showOnlyImage: Boolean? = null
    var clearHistoryOnPageLoad: Boolean? = null
    var externalBrowsers: List<String>? = null //todo mukesh json check
    // OM Track info for the ad.
    var omTrackType: OMTrackType? = null
    var showPlayIcon: Boolean? = null
    var needsBackupAds: Boolean? = null
    var timeOffset: Long = 0
    var extras: Any? = null

    var adTagPosition: AdTagPositionType? = null
    var showLearnMore: Boolean? = null
    open var content: Content? = null
    open var brand: Brand? = null
    var width: Int? = null
    var height: Int? = null
    override val uniqueAdIdentifier: String
        get() {
            if (uniqueAdId == 0) {
                uniqueAdId = uniqueAdIdCounter++
            }
            return "$id-$uniqueAdId"
        }
    @SerializedName("card-position")
    override var cardPosition: Int? = null
    @SerializedName("position")
    override var adPosition: AdPosition? = null
    @SerializedName("banner-fill")
    var bannerFill: BannerFill? = null
    @SerializedName("sdk-order")
    override var sdkOrder: Int = 0
    @SerializedName("adFeedback")
    var reportAdsMenuFeedBackEntity: ReportAdsMenuFeedBackEntity? = null
    @SerializedName("impFcap")
    override var fcData: FCData? = null
    @SerializedName("bImpFcap")
    override var bannerFCData: FCData? = null
    var adLPTimeSpentBeaconUrl: String? = null

    fun isContentEnabledAd(): Boolean = content?.id?.isNotBlank() ?: false

    fun copyFrom(fromAd: BaseDisplayAdEntity) {
        super.copyFrom(fromAd)

        fromAd.width?.let { width = it }
        fromAd.height?.let { height = it }
        fromAd.content?.let { content = it }

        fromAd.cardPosition?.let { cardPosition = it }
        fromAd.positionWithTicker?.let { positionWithTicker = it }
        fromAd.minAdDistance?.let { minAdDistance = it }
        fromAd.isLargeAd?.let { isLargeAd = it }
        fromAd.largeAdDistance?.let { largeAdDistance = it }

        fromAd.adTemplate?.let { adTemplate = it }

        fromAd.adTagPosition?.let { adTagPosition = it }
        fromAd.bannerFill?.let { bannerFill = it }
        fromAd.extras?.let { extras = it }
        fromAd.showOnlyImage?.let { showOnlyImage = it }
        fromAd.showPlayIcon?.let { showPlayIcon = it }
        fromAd.needsBackupAds?.let { needsBackupAds = it }
        fromAd.showLearnMore?.let { showLearnMore = it }
        fromAd.brand?.let { brand = it }
        fromAd.interactiveAd?.let { interactiveAd = it }
        fromAd.clearHistoryOnPageLoad?.let { clearHistoryOnPageLoad = it }
        fromAd.useWideViewPort?.let { useWideViewPort = it }
        fromAd.span?.let { span = it }
        fromAd.spanInMS?.let { spanInMS = it }
        fromAd.showCount?.let { showCount = it }

        fromAd.showBorder?.let { showBorder = it }
        fromAd.adBorderColor?.let { adBorderColor = it }
        fromAd.containerBackgroundColor?.let { containerBackgroundColor = it }
        fromAd.containerBorderColor?.let { containerBorderColor = it }

        fromAd.adCacheAverage?.let { adCacheAverage = it }
        fromAd.adCacheGood?.let { adCacheGood = it }
        fromAd.adCacheSlow?.let { adCacheSlow = it }

        fromAd.firstAdSwipeCount?.let { firstAdSwipeCount = it }
        fromAd.maxPageSwipeCount?.let { maxPageSwipeCount = it }
        fromAd.pageSwipeCount?.let { pageSwipeCount = it }
        fromAd.requestSwipeCountGood?.let { requestSwipeCountGood = it }
        fromAd.requestSwipeCountSlow?.let { requestSwipeCountSlow = it }
        fromAd.requestSwipeCountAverage?.let { requestSwipeCountAverage = it }
        fromAd.minThresholdSwipeCount?.let { minThresholdSwipeCount = it }
        fromAd.sessionCount?.let { sessionCount = it }
        fromAd.interstitialDisplayType?.let { interstitialDisplayType = it }
        fromAd.showHTMLPgiOnlyOnVisible?.let { showHTMLPgiOnlyOnVisible = it }
        fromAd.dedupId?.let { dedupId = it }
        fromAd.dedupDistance?.let { dedupDistance = it }
        fromAd.showTitle?.let { showTitle = it }
    }

    /**
     * Provides text with color information.
     *
     * @author shreyas.desai
     */
    class ItemTag : Serializable {

        // Attributes
        var color: String? = null
        // Tag
        var data: String? = null
        @SerializedName("color-night")
        var colorNight: String? = null
        var maxLines: Int? = AdConstants.AD_BANNER_TITLE_DEFAULT_MAXLINES

        var type: LogoType? = null
        @SerializedName("background-color")
        var backgroundColor: String? = null

        @SerializedName("background-color-night")
        var backgroundColorNight: String? = null

        fun getThemeBasedBgColor(isNightMode: Boolean): String? {
            return if (!isNightMode) {
                backgroundColor
            } else
                backgroundColorNight
        }

        fun getThemeBasedTextColor(isNightMode: Boolean): String? {
            return if (!isNightMode) {
                color
            } else colorNight
        }

        companion object {
            private const val serialVersionUID = -1988372206738179945L
        }
    }

    /**
     * Provides width, height and image url for any image.
     *
     * @author heena.arora
     */
    class ItemImage : Serializable {
        // Attributes
        var width: Int = AdConstants.AD_NEGATIVE_DEFAULT
        var height: Int = AdConstants.AD_NEGATIVE_DEFAULT
        var data: String? = null //imageURL
        var type: LogoType? = null

        companion object {
            private const val serialVersionUID = -4419463197591298484L
        }
    }

    /**
     * Base class for content tag. Each ad-type will have its own
     * content tag implementation.
     *
     * @author shreyas.desai
     */

    class Content : Serializable {

        // Attributes
        var id: String? = null
        var language: String? = null
        var sourceAlphabet: String? = null
        var itemTag: ItemTag? = null
        var itemTitle: ItemTag? = null
        var itemSubtitle1: ItemTag? = null
        var itemSubtitle2: ItemTag? = null
        var reportText: ItemTag? = null
        var learnMoreText: ItemTag? = null
        @SerializedName("bg-color")
        var bgColor: String? = null
        @SerializedName("bg-color-night")
        var bgColorNight: String? = null

        var iconLink: String? = null
        var itemImage: ItemImage? = null
        var itemDescription: String? = null
        var actionText: String? = null
        var imgLink: String? = null
        var shortInfo: String? = null
        var htmlBody: String? = null
        var timeOfIngestion: String? = null

        fun getThemeBasedBgColor(isNightMode: Boolean): String? {
            return if (!isNightMode) {
                bgColor
            } else bgColorNight
        }

        companion object {
            private const val serialVersionUID = 5389553807359705115L
        }
    }

    class CustomVideoTrackers : Serializable {
        var adVideoStart: String? = null
        var adVideoEnd: String? = null
        var adVideoPause: String? = null
        var adVideoPlay: String? = null
        var adVideoMute: String? = null
        var adVideoUnMute: String? = null
    }

    data class Brand(var brandTitle: ItemTag? = null,
                     var brandSubTitle: ItemTag? = null,
                     var brandLogo: ItemImage? = null,
                     var brandFallbackText: ItemTag? = null) : Serializable

    enum class LogoType {
        S, // Square
        R  // Rectangle
    }

    companion object {
        private const val serialVersionUID = 775057266414274065L
        private var uniqueAdIdCounter = 1
    }

    enum class AdTagPositionType {
        TOP_RIGHT,
        TOP_OVERLAY_LEFT,
        TOP_OVERLAY_RIGHT,
        BOTTOM_OVERLAY_LEFT,
        BOTTOM_OVERLAY_RIGHT
    }

}
