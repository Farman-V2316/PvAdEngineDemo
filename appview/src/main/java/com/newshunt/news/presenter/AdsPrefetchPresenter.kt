/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.news.presenter

import `in`.dailyhunt.money.contentContext.ContentContext
import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.newshunt.adengine.FetchAdSpecUsecase
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.presenter.BasePresenter
import com.newshunt.dataentity.common.pages.PageEntity
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dataentity.social.entity.AdSpec
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.news.model.usecase.GetPageByIdOrDefaultUsecase
import com.newshunt.news.model.usecase.toMediator2
import javax.inject.Inject
import kotlin.collections.ArrayList

private const val LOG_TAG = "AdsPrefetchPresenter"
private const val UNDEFINED_KEY = "unknown"
private const val INVALID_REQUEST_ID = -999
private const val DEFAULT_APP_SECTION = "HOME"

class AdsPrefetchPresenter @Inject constructor(pagesUseCase: GetPageByIdOrDefaultUsecase,
                                               private var adSpecUsecase: FetchAdSpecUsecase) : BasePresenter() {
    private val getAdUsecaseController = GetAdUsecaseController(BusProvider.getUIBusInstance(),
            INVALID_REQUEST_ID)
    private var pagesUsecase = pagesUseCase.toMediator2()
    private var pagesLiveData = pagesUsecase.data()
    private var adSpec = adSpecUsecase.data()

    private var pageEntity: PageEntity? = null

    override fun start() {
        AdLogger.d(LOG_TAG, "start ad prefetch")
    }

    override fun stop() {
        getAdUsecaseController.destroy()
        pagesUsecase.dispose()
        adSpecUsecase.dispose()
    }

    fun requestPrefetchAds(entityId: String?, section: String?,
                           adPosition: AdPosition, activity: Activity?) {
        val adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
        val tagSize: Int? = adsUpgradeInfo?.cardPP1AdsConfig?.tagOrder?.size ?: 0
        if (!AdsUtil.isPrefetchEnabled(adPosition, adsUpgradeInfo) || adPosition == AdPosition.PP1 && tagSize == 0) {
            return
        }
        val isHome = entityId == DEFAULT_APP_SECTION
        pagesLiveData.observe(activity as LifecycleOwner, Observer {
            // Take data for entityKey or 1st item if that is absent.
            pageEntity = it.getOrNull()
            AdLogger.d(LOG_TAG, "received entity info ${pageEntity?.id} : ${pageEntity?.entityType}")
            fetchAdspec(adPosition, activity, section, isHome, pageEntity)
        })

        adSpec.observe(activity as LifecycleOwner, Observer {
            requestAd(adPosition, it.getOrNull()?.get(pageEntity?.id), section, isHome)
            adSpec.removeObservers(activity)
        })

        val sec = if (section == PageSection.NEWS.section || section == PageSection.TV.section) {
            section
        } else {
            PageSection.NEWS.section
        }
        pagesUsecase.execute(GetPageByIdOrDefaultUsecase.bundle(sec, entityId))
    }

    private fun fetchAdspec(adPosition: AdPosition, activity: Activity?, section: String?,
                            isHome: Boolean, pageEntity: PageEntity?) {
        if (pageEntity != null) {
            adSpecUsecase.execute(listOf(pageEntity.id))
        } else {
            requestAd(adPosition, null, section, isHome)
        }
    }

    private fun requestAd(adPosition: AdPosition, adSpec: AdSpec?,
                          section: String?, isHome: Boolean) {
        getAdUsecaseController.requestAds(getAdRequest(adPosition, section, isHome, adSpec))
    }

    private fun getAdRequest(adPosition: AdPosition, section: String?,
                             isHome: Boolean, adSpec: AdSpec?): AdRequest {
        AdLogger.v(LOG_TAG, "Sending Ad Prefetch request : $adPosition :: " +
                "${pageEntity?.entityType} :: ${pageEntity?.id} :: $adSpec")
        val contextMap = mutableMapOf<String, ContentContext>()
        var numOfAds = 1
        // add contentContext per pp1 tag/slot
        val pp1TagOrders: HashSet<String> = HashSet()
        AdsUpgradeInfoProvider
                .getInstance().adsUpgradeInfo?.cardPP1AdsConfig?.tagOrder?.let {
                    pp1TagOrders.addAll(it)
                }

        if (AdPosition.PP1 == adPosition) {
            pp1TagOrders.forEach { tag ->
                val key = AdsUtil.getAdSlotName(tag, adPosition)
                AdsUtil.getContentContextFor(adSpec, key)?.let {
                    contextMap[key] = it
                }
            }
            numOfAds = pp1TagOrders.size
        } else {
            AdsUtil.getContentContextFor(adSpec, adPosition.value)?.let {
                contextMap[adPosition.value] = it
            }
        }
        return AdRequest(adPosition,
                numOfAds = numOfAds,
                section = section,
                entityId = pageEntity?.id ?: UNDEFINED_KEY,
                entityType = pageEntity?.entityType ?: UNDEFINED_KEY,
                entitySubType = pageEntity?.subType ?: UNDEFINED_KEY,
                contentContextMap = contextMap,
                isPrefetch = true,
                localRequestedAdTags = if (adPosition == AdPosition.PP1) ArrayList(pp1TagOrders) else null)
    }
}
