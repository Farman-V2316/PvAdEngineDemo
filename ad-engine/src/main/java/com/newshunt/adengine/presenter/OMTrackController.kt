/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.presenter

import android.view.View
import android.webkit.WebView
import com.iab.omid.library.versein.adsession.AdSession
import com.iab.omid.library.versein.adsession.CreativeType
import com.iab.omid.library.versein.adsession.ImpressionType
import com.iab.omid.library.versein.adsession.Owner
import com.newshunt.adengine.R
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.omsdk.OMSessionState
import com.newshunt.adengine.model.entity.omsdk.OMTrackType
import com.newshunt.adengine.model.entity.omsdk.OMVendorInfo
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.OMSdkHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger

/**
 * @author raunak.yadav
 */
class OMTrackController private constructor(private val trackType: OMTrackType?,
                                            private val trackedAdId: String?,
                                            private val vendorsInfo: List<OMVendorInfo?>?) {

    fun registerViewToTrack(adSession: AdSession?, view: View) {
        try {
            adSession?.registerAdView(view)
        } catch (ex: Exception) {
            Logger.caughtException(ex)
        }
    }

    /**
     * Registers and starts an adSession for this view.
     *
     * @param adView adView
     * @return session
     */
    fun startTracking(adView: View, contentUrl: String? = null): OMSessionState? {
        val sessionState: OMSessionState?

        when (trackType) {
            OMTrackType.NATIVE ->
                //create list of resources.
                sessionState = OMSdkHelper.createAdSessionForNativeAd(vendorsInfo, contentUrl)

            OMTrackType.WEB_VIDEO -> {
                if (adView is WebView) {
                    sessionState = OMSdkHelper.createAdSessionForWebAd(adView, contentUrl,
                            CreativeType.DEFINED_BY_JAVASCRIPT, ImpressionType.DEFINED_BY_JAVASCRIPT,
                            Owner.JAVASCRIPT, Owner.JAVASCRIPT)
                    adView.setTag(R.id.omid_adview_tag_id, Constants.OM_WEBVIEW_TAG)
                } else {
                    return null
                }
            }
            OMTrackType.WEB ->
                if (adView is WebView) {
                    sessionState = OMSdkHelper.createAdSessionForWebAd(adView, contentUrl,
                            CreativeType.HTML_DISPLAY, ImpressionType.ONE_PIXEL, Owner.NATIVE, Owner.NONE)
                    adView.setTag(R.id.omid_adview_tag_id, Constants.OM_WEBVIEW_TAG)
                } else {
                    return null
                }
            else -> return null
        }

        sessionState?.let {
            registerViewToTrack(it.adSession, adView)
            it.adSession?.start()
            AdLogger.d(TAG, "OM ad session started for ad $trackedAdId")
        }
        return sessionState
    }

    companion object {

        private const val TAG = "OMTrackController"

        /**
         * Creates and returns an OM tracker if the ad allows tracking
         * and ad contains data for tracking.
         *
         * @param adEntity ad
         * @return tracker, if ad data is valid for OM tracking.
         */
        fun createTracker(adEntity: BaseDisplayAdEntity?): OMTrackController? {
            if (adEntity == null || !OMSdkHelper.isOMSdkEnabled) {
                return null
            }

            val trackType = adEntity.omTrackType
            return if (trackType == null || trackType == OMTrackType.NONE ||
                    trackType == OMTrackType.NATIVE && adEntity.omVendorsInfo.isNullOrEmpty()) {
                null
            } else OMTrackController(trackType, adEntity.uniqueAdIdentifier, adEntity.omVendorsInfo)

        }
    }
}