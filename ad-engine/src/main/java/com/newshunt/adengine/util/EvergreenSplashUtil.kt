/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import com.newshunt.adengine.model.entity.AdSelectionMeta
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.SplashAdMeta
import com.newshunt.common.helper.common.Constants
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps splash related meta from evergreen ad to quickly decide on ad rendering in Splash screen.
 *
 * @author raunak.yadav
 */
object EvergreenSplashUtil {

    const val TAG = "EvergreenSplashUtil"

    private val splashAdMeta = ConcurrentHashMap<String, SplashAdMeta>()

    /**
     * Wait time on splash screen for ad. Since we start timer before choosing ad, we will use
     * the max span of the eligible ads.
     */
    private var span = AdConstants.AD_NEGATIVE_DEFAULT

    /**
     * Add meta from an add that has splash in supportedZones.
     */
    fun addSplashMetaFrom(adEntity: BaseDisplayAdEntity, splashData: AdSelectionMeta?) {
        val meta = getMetaFrom(adEntity, splashData) ?: return
        AdLogger.d(TAG, "add Splash candidates : $meta")
        splashAdMeta[meta.adId] = meta
        // Store the bigger span.
        if (meta.span > span) {
            span = meta.span
        }
    }

    private fun getMetaFrom(ad: BaseDisplayAdEntity, splashData: AdSelectionMeta?): SplashAdMeta? {
        return if (ad.id == null || ad.campaignId == null || ad.bannerId == null) {
            AdLogger.e(TAG, "Drop Splash meta. Ids missing.")
            null
        } else {
            SplashAdMeta(adId = ad.uniqueAdIdentifier,
                campaignId = ad.campaignId ?: Constants.EMPTY_STRING,
                bannerId = ad.bannerId,
                showCount = splashData?.showCount ?: ad.showCount
                ?: AdConstants.SPLASH_DEFAULT_SHOW_COUNT,
                startEpoch = ad.startepoch,
                endEpoch = ad.endepoch,
                span = splashData?.span ?: ad.span ?: AdConstants.SPLASH_DEFAULT_SPAN)
        }
    }

    fun getEvergreenSpan(): Int = span * 1000

    fun clear() {
        splashAdMeta.clear()
        span = AdConstants.AD_NEGATIVE_DEFAULT
    }

    /**
     * Check if any splash ad might be available.
     * This does not guarantee splash rendering, though.
     */
    fun isSplashAdAvailable(): Boolean {
        if (splashAdMeta.isEmpty()) return false

        splashAdMeta.values.forEach {
            if (SplashAdPersistenceHelper.isSplashMetaValidForRendering(it)) {
                return true
            }
        }
        AdLogger.d(TAG, "No splash ad available")
        return false
    }

}