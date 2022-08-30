/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.viewholder

import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.view.view.InstreamAdWrapper

/**
 * Wrapper for empty ad. To fire empty beacons at proper time.
 * This should not pause player.
 *
 * @author raunak.yadav
 */
class EmptyAdWrapper(val adEntity: BaseDisplayAdEntity) : InstreamAdWrapper {

    // Its a non-view ad.
    override fun isValid(): Boolean {
        return false
    }

    override fun startPlayingAd() {
        adEntity.isShown = true
        adEntity.notifyObservers()
        with(AsyncAdImpressionReporter(adEntity)) {
            hitTrackerUrl(true, adEntity.requestUrl)
            onCardView()
        }
    }
}