/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.domain.usecase

import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo

/**
 * Use-case to get advertisement.
 *
 * @author raunak.yadav
 */
interface GetAdUsecase {
    fun requestAds(adRequest: AdRequest): NativeAdContainer?

    fun requestAds(adRequest: AdRequest, adsUpgradeInfo: AdsUpgradeInfo?): NativeAdContainer?

    fun requestAds(adRequest: AdRequest, adsUpgradeInfo: AdsUpgradeInfo?,
                   postLocalAdsAsync: Boolean): NativeAdContainer?

    fun onAdsResponse(nativeAdContainer: NativeAdContainer)

    fun sendAdsToPresenter(nativeAdContainer: NativeAdContainer)

    fun destroy()
}