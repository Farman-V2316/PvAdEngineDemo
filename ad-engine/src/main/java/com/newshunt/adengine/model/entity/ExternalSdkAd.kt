/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity

import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.gson.annotations.SerializedName
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.AdsPreference
import java.io.Serializable
import java.net.URLDecoder
import java.util.*

/**
 * Represents Dfp ad content.
 *
 * @author heena.arora
 */
class ExternalSdkAd : BaseDisplayAdEntity() {
    var external: External? = null
    @Transient
    var nativeAdObject: Any? = null
    var customTracking: CustomTracking? = null

    //immersive view fields
    var enableImmersiveView: Boolean = false
    var forceImmersiveView: Boolean = false
    var immersiveTransitionSpan: Int = PreferenceManager.getPreference(AdsPreference.IMMERSIVE_VIEW_DISTANCE, -1)
    var immersiveViewDistance: Int = PreferenceManager.getPreference(AdsPreference.IMMERSIVE_VIEW_DISTANCE, -1)
    var companionRefreshTime: Int = PreferenceManager.getPreference(AdsPreference.IMMERSIVE_VIEW_DISTANCE, -1)
    var shownImmersive: Boolean = false //field use for impression tracker firing decision
    /**
     * Represents content of external tag in dfp ad e.g adunitid,adsizes etc.
     *
     * @author heena.arora
     */
    class External : Serializable {
        @SerializedName("type")
        var span: Int = 0
        @SerializedName("adsizes")
        var adSizes: List<String>? = null
        @SerializedName("adunitid")
        var adUnitId: String? = null
        @SerializedName("publisherid")
        var publisherId: String? = null //Additional authentication key
        var uiTemplate: String? = null
        // DFP banner to manually record impression later
        var manualImpression: Boolean? = false
        var extras: String? = null
        var data: String? = null //externalContent

        // Amazon sdk bid data
        var bidId: String? = null

        var mediaFileURL: String? = null

        var preferredAspectRatio: List<CreativeOrientation>? = null
        var videoAutoPlay: Int = 0
        var itemTag: String? = null
        var shortInfo: String? = null
        var tagURL: String? = null
        var keyMapping: String? = null
        var statusBeacon: String? = null
            set(statusBeacon) {
                if (!CommonUtils.isEmpty(statusBeacon)) {
                    try {
                        field = URLDecoder.decode(statusBeacon, Constants.TEXT_ENCODING_UTF_8)
                    } catch (e: Exception) {
                        field = null
                        Logger.caughtException(e)
                    }

                }
            }
    }

    /**
     * Represents content of CustomTracking for Instream Ad Beacon Urls
     *
     * @author vinod.bc
     */
    class CustomTracking : Serializable {
        val tracking = ArrayList<Tracking>()
        val customImmersiveViewEventTrackers: Map<String,String>? = null
        val customCompanionTrackings: Map<String, CompanionTracking>? = null
    }

    data class CompanionTracking(
            val companionClickTracking: Array<String> = emptyArray(),
            val companionBeaconTracking: Array<String> = emptyArray(),
            val companionLPTimeSpentBeaconUrl: String? = null): Serializable

    class Tracking : Serializable {
        var id: String? = null
        var extra: String? = null
        var beaconUrl: String? = null
        var landingUrl: String? = null
        var requestUrl: String? = null
        var errorBeaconUrl: String? = null
        var adLPTimeSpentBeaconUrl: String? = null
        var customCompanionTrackings: CustomCompanionTrackings? = null
    }

    class CustomCompanionTrackings : Serializable {
        var companionClickTracking: List<String>? = null
        val companionLPTimeSpentBeaconUrl: String? = null
    }

    enum class CreativeOrientation(val value: Int) : Serializable {
        @SerializedName("landscape")
        LANDSCAPE(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE),
        @SerializedName("square")
        SQUARE(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE),
        @SerializedName("portrait")
        PORTRAIT(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT),
        @SerializedName("any")
        ANY(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY)
    }
}
