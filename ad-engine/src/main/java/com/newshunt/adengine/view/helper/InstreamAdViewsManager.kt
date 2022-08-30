/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view.helper

import android.view.ViewGroup
import com.newshunt.adengine.instream.IAdLogger
import com.newshunt.adengine.listeners.PlayerInstreamAdListener
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.view.InstreamAdWrapper
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * - Manages video ads that provide complete view instead of tagURL.
 * - Orders the ads and shows them at proper timeOffsets.
 * -  Only the just previous ad must be shown,
 * in case user seeks to a further position.
 *
 * @author raunak.yadav
 */
class InstreamAdViewsManager(adEntity: BaseAdEntity,
                             private val adContainer: ViewGroup) {

    private var adEntities: MutableList<BaseDisplayAdEntity> = ArrayList()
    private var currentInstreamAd: InstreamAdWrapper? = null
    private var listener: PlayerInstreamAdListener? = null

    init {
        if (adEntity is BaseDisplayAdEntity) {
            adEntities.add(adEntity)
        } else {
            adEntities.addAll((adEntity as MultipleAdEntity).baseDisplayAdEntities)
        }
    }

    companion object {
        const val LOG_TAG = "InstreamAdViewsManager"

        fun create(adEntity: BaseAdEntity?, adContainer: ViewGroup): InstreamAdViewsManager? {
            adEntity ?: return null

            if (AdsUtil.isIMAVideoAd(adEntity))
                return null
            IAdLogger.d(LOG_TAG, "Instream ads received. $adEntity")
            return InstreamAdViewsManager(adEntity, adContainer)
        }
    }

    fun onTimeUpdate(position: Long) {
        if (adEntities.isEmpty()) {
            return
        }

        val index = findAd(position / 1000)
        if (index == -1) {
            return
        }
        val adEntity = adEntities[index]
        if (adEntity.isShown) return

        currentInstreamAd = InstreamAdViewFactory.create(adContainer, adEntity)
        IAdLogger.d(LOG_TAG, "Found an eligible ad for t=${adEntity.timeOffset}, Ad : $currentInstreamAd")
        currentInstreamAd?.setAdStateListener(listener)
        currentInstreamAd?.startPlayingAd().also {
            // Mark the ad once rendered.
            adEntity.isShown = true
            if (index == 0) {
                adEntities.removeAt(0)
            }
        }
        if (currentInstreamAd?.isValid() == false) {
            currentInstreamAd = null
        }
    }

    /**
     * Find if any ad is eligible to play.
     * If offset of 1 or more ads has been crossed due to seek, play the last passed ad.
     */
    private fun findAd(position: Long): Int {
        var index = -1
        for (adEntity in adEntities) {
            if (adEntity.timeOffset > position) {
                return index
            }
            if (adEntity.timeOffset == position) {
                return index.inc()
            }
            index++
        }
        return index
    }

    fun isPlayingAd(): Boolean {
        return currentInstreamAd != null
    }

    fun setVisibility(visible: Boolean) {
        currentInstreamAd?.setVisibility(visible)
    }

    fun setAdStateListener(listener: PlayerInstreamAdListener?) {
        this.listener = listener
    }

    fun setLayoutParams(width: Int, height: Int) {
        currentInstreamAd?.setLayoutParams(width, height)
    }

    fun onAdComplete() {
        currentInstreamAd?.setVisibility(false)
        currentInstreamAd?.destroy()
        currentInstreamAd = null
    }

    fun destroy() {
        onAdComplete()
        IAdLogger.d(LOG_TAG, "Destroying the remaining ads ${adEntities.size}")
        //Destroy remaining ads.
        for (adEntity in adEntities) {
            adEntity.isShown = true
            AdsUtil.destroyAd(adEntity, -1)
        }
    }

    fun hasUnseenEmptyAds() : Boolean {
        if(CommonUtils.isEmpty(adEntities)) return false

        adEntities.forEach {
            if(it is EmptyAd && !it.isShown) {
                return true
            }
        }
        return false
    }

}