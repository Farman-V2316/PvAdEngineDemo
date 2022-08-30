/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.dailyhunt.tv.players.presenters

import android.app.Activity

import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.common.presenter.BasePresenter
import com.newshunt.dataentity.news.model.entity.PageType
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

/**
 * Created by Jayanth on 09/05/18.
 */
class PlayerInlineVideoAdBeaconPresenter(private val uiBus: Bus,
                                         private val uniqueRequestId: Int,
                                         private val categoryKey: String?,
                                         private val tvVideoSource: String?) : BasePresenter() {
    private var isRegistered: Boolean = false
    private var getAdUsecaseController: GetAdUsecaseController? = null
    private var baseDisplayAdEntity: BaseDisplayAdEntity? = null
    private val pageType = PageType.BUZZGROUP

    override fun start() {
        if (!isRegistered) {
            uiBus.register(this)
            isRegistered = true
        }
    }

    /**
     * Fire request on Ad start to get the beacon url.
     */
    fun requestAd() {
        val adRequest = getAdRequest(AdPosition.INLINE_VIDEO)
        if (getAdUsecaseController == null) {
            getAdUsecaseController = GetAdUsecaseController(uiBus, uniqueRequestId)
        }
        getAdUsecaseController!!.requestAds(adRequest)
    }

    private fun getAdRequest(adPosition: AdPosition): AdRequest {
        return AdRequest(adPosition, 1, groupKey = categoryKey)
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        if (nativeAdContainer.baseAdEntities != null &&
            nativeAdContainer.uniqueRequestId == uniqueRequestId &&
            nativeAdContainer.baseAdEntities?.size ?: 0 > 0) {

            baseDisplayAdEntity = nativeAdContainer.baseAdEntities?.get(0) as? BaseDisplayAdEntity?
            sendImpressionBeacon()
        }
    }

    fun sendImpressionBeacon() {
        baseDisplayAdEntity?.let {
            if (!it.isShown) {
                it.notifyObservers()
                it.isShown = true
                val asyncAdImpressionReporter = AsyncAdImpressionReporter(it)
                asyncAdImpressionReporter.onCardView()
            }
        }
    }

    override fun stop() {
        if (isRegistered) {
            uiBus.unregister(this)
            isRegistered = false
        }
        getAdUsecaseController?.destroy()
    }
}