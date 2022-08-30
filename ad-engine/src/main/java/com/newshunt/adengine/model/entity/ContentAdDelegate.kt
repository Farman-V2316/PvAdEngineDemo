/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity

import android.app.Activity
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdFrequencyStats
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.helper.AdBinderRepo
import com.newshunt.adengine.view.helper.PgiAdHandler
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.helper.player.PlayerControlHelper

/**
 * Manages adOperations for Promoted Content.
 *
 * @author raunak.yadav
 */
class ContentAdDelegate(private val uiComponentId: Int,
                        private val entityId: String? = Constants.EMPTY_STRING) {

    var adEntity: BaseDisplayAdEntity? = null

    var asyncAdImpressionReporter: AsyncAdImpressionReporter? = null

    fun getPromotedTag(): String? {
        return adEntity?.content?.itemTag?.data
    }

    fun bindAd(item: CommonAsset?) {
        bindAd(item?.i_adId(), item?.i_id())
    }

    fun bindAd(adId: String?, postId: String?) {
        asyncAdImpressionReporter = null

        adEntity = adId?.let { adId ->
            AdBinderRepo.getAdById(adId)?.let { adEntity ->
                when (adEntity) {
                    is BaseDisplayAdEntity -> {
                        adEntity
                    }
                    is MultipleAdEntity -> {
                        val ad = adEntity.baseDisplayAdEntities.find {
                            it.content?.id == postId
                        }
                        ad
                    }
                    else -> null
                }
            }
        }?.also {
            asyncAdImpressionReporter = AsyncAdImpressionReporter(it)
        }
    }

    fun onCardView(activity: Activity? = null, adAdapterPosition: Int = -1) {
        adEntity?.let { ad ->
            if (!ad.isShown) {
                ad.isShown = true
                ad.notifyObservers()

                //If this is first ad of MultipleAdEntity, mark the full ad as viewed.
                val parentAd = AdBinderRepo.getAdById(ad.uniqueAdIdentifier)
                if (parentAd is MultipleAdEntity) {
                    parentAd.isShown = true
                    parentAd.notifyObservers()
                }
                // Only the parent ad or standalone ads should update campaign viewed count, not
                // subItems in carousel.
                parentAd?.let { AdFrequencyStats.onAdViewed(parentAd, uniqueRequestId = uiComponentId)}

                val adPositionIndex = AdsUtil.computeAdInsertedIndex(ad, adAdapterPosition)
                asyncAdImpressionReporter?.onCardView(PlayerControlHelper.isListMuteMode, adPositionIndex)

                // Remove the ad from other lists now.
                // Some zones refill cache on ad view.
                AndroidUtils.getMainThreadHandler().postDelayed({
                    val event = AdViewedEvent(ad.uniqueAdIdentifier, uiComponentId,
                            ad.parentIds, ad.adPosition!!, ad.adTag, entityId)
                    BusProvider.getUIBusInstance().post(event)
                }, if (ad.adPosition == AdPosition.MASTHEAD) 200L else 0L)
                if (ad.adPosition == AdPosition.PGI) {
                    PgiAdHandler.reset(activity)
                }
            }
        }
    }

    fun onCardClick() {
        asyncAdImpressionReporter?.onClickEvent()
    }

    fun reset() {
        adEntity = null
        asyncAdImpressionReporter = null
    }

    fun onDestroy() {
        if (!AdsUtil.isExternalSdkNativePgiAd(adEntity)) {
            AdsUtil.destroyAd(adEntity, uiComponentId)
        }
    }
}