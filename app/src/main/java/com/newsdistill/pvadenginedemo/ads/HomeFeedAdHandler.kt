package com.newsdistill.pvadenginedemo.ads

import android.app.Activity
import android.widget.RelativeLayout
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.squareup.otto.Bus

class HomeFeedAdHandler(lifecycleOwner: LifecycleOwner) {
    private val adRenderer = AdRenderer(lifecycleOwner)
    var adRequestID = -1


    fun loadHomeFeedAd(adPosition: AdPosition, bus: Bus, zoneAdType: String) {
        adRequestID = if (zoneAdType == AdZoneType.PGI_IMAGE.name) 111 else 100
        initAd(adPosition, adRequestID, bus, zoneAdType)
    }

    fun initAd(adPosition: AdPosition, uniqueRequestId: Int, uiBus: Bus, zoneAdType: String) {
        val useCase = GetAdUsecaseController(uiBus, uniqueRequestId)
        useCase.requestAds(AdRequest(adPosition, 1, skipCacheMatching = true, zoneAdType = zoneAdType))
    }

    fun insertAd(
        activity: Activity?,
        nativeAdContainer: NativeAdContainer?,
        adContainer: RelativeLayout,
    ) {
        if (nativeAdContainer == null || CommonUtils.isEmpty(nativeAdContainer.baseAdEntities)) {
            return
        }

        val baseAdEntity = nativeAdContainer.baseAdEntities!![0]
        println("panda: Ad to be shown: ${baseAdEntity}")
        adRenderer.renderAd(activity, baseAdEntity, adContainer)
    }
}