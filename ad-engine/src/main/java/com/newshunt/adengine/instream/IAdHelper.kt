/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.instream

import `in`.dailyhunt.money.contentContext.ContentContext
import android.app.Activity
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.dataentity.common.pages.PageEntity
import com.squareup.otto.Subscribe
import java.util.*

/**
 * In Stream Helper for managing the Ads of type [InStream ] and Exo
 *
 *
 * a) Maintains the current ads honored / played count using this combination [In Memory]
 * b) Requests Ads in Background , if there is no AD in inventory to serve based on conditions
 * c) Switches b/w Fragment( ExoView + AD) and Fragment (ExoView only) based on
 * i)   Item type                -- We support AD for EXO
 * ii)  Current AD played count  -- [PGI , P1 and Inline Video]
 * iii) AD Current Inventory for playing
 * d) Reset the state when we switch back to the other sections .
 * Initialisation is done once for TV Section
 *
 * @author ranjith
 */

class IAdHelper(private val activity: Activity?, private val uniqueRequestId: Int,
                val cacheItem: IAdCacheItem,
                private val playerCacheCallbacks: IAdCacheCallbacks?) {

    private val AD_REQ_COUNT = 1
    private var adsUseCase: GetAdUsecaseController? = null
    private var busRegistered: Boolean = false
    private val uiBus = BusProvider.getUIBusInstance()

    private val adEntities: ArrayList<BaseAdEntity> = ArrayList()
    private val videoAdParams: HashMap<String, String> = HashMap(cacheItem.videoParams)

    /**
     * Method to return an InStream - AD from Queue , make a request for getting next instream AD
     *
     * @return -- Ad Entity
     */
    //Request , if queue is empty ..
    val inStreamAd: BaseAdEntity?
        get() {
            Logger.d(TAG, "Can show AD If Possible")

            if (adEntities.isEmpty()) {
                Logger.d(TAG, "There is no ad to serve ..")
                return null
            }

            val displayAdEntity = adEntities[0]
            displayAdEntity.isShown = true
            requestADsIfRequired()
            return displayAdEntity
        }

    /**
     * Method to return an InStream - AD from Queue,
     * Here we wont request for getting next Ads
     *
     * @return -- Ad Entity
     */
    val getCurrentInstreamAd: BaseAdEntity?
        get() {
            Logger.d(TAG, "Can show AD If Possible")

            if (adEntities.isEmpty()) {
                Logger.d(TAG, "There is no ad to serve ..")
                return null
            }

            return adEntities[0]
        }

    fun start() {
        try {
            if (playerCacheCallbacks != null) {
                videoAdParams["vTimeSinceLast"] = playerCacheCallbacks.timeSinceLastAdPlayer.toString()
            }

            // We have to request AD , if there is no Current AD to serve
            requestADsIfRequired()
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
    }

    private fun registerBus() {
        if (!busRegistered) {
            uiBus.register(this)
            busRegistered = true
        }
    }

    fun stop() {
        if (busRegistered) {
            busRegistered = false
            uiBus.unregister(this)
        }
        adsUseCase?.destroy()
    }

    private fun nonShownADsPresent(): Boolean {
        val entityIterator = adEntities.iterator()
        while (entityIterator.hasNext()) {
            val entity = entityIterator.next()
            if (entity.isShown) {
                entityIterator.remove()
            }
        }
        return adEntities.isNotEmpty()
    }

    private fun requestADsIfRequired() {
        if (nonShownADsPresent()) {
            Logger.d(TAG, "Ad Entities are present ..")
            return
        }
        requestAds(AD_REQ_COUNT, AdPosition.INSTREAM_VIDEO)
    }

    @Subscribe
    fun onAdResponseRetrieved(adContainer: NativeAdContainer) {
        if (adContainer.uniqueRequestId != uniqueRequestId) {
            return
        }

        if (adContainer.adPosition != AdPosition.INSTREAM_VIDEO) {
            Logger.d(TAG, "Ad Position is not Instream. Ignore.")
            return
        }

        if (adContainer.baseAdEntities == null) {
            playerCacheCallbacks?.onAdResponseError(this)
            Logger.d(TAG, "Instream Ad Response is empty")
            return
        }

        // Process the AD Entities ..
        adContainer.baseAdEntities?.forEach { adEntity ->
            if (adEntity.isShown) {
                Logger.d(TAG, "AD is null / already shown ")
                return@forEach
            }

            adEntities.add(adEntity)
            playerCacheCallbacks?.onAdReponseReceived(this)
            return
        }
        playerCacheCallbacks?.onAdResponseError(this)
    }

    private fun requestAds(numOfAds: Int, adPosition: AdPosition) {
        val adRequest = buildAdsRequest(numOfAds, adPosition)
        activity?.let {
            activity.runOnUiThread {
                registerBus()
            }
        }

        if (adsUseCase == null) {
            adsUseCase = GetAdUsecaseController(uiBus, uniqueRequestId)
        }

        adsUseCase?.requestAds(adRequest)
    }

    private fun buildAdsRequest(numOfAds: Int, adPosition: AdPosition):
            AdRequest {
        val commonAsset = cacheItem.commonAsset
        val pageEntity = cacheItem.pageEntity
        val contentContext = getContentContext(commonAsset, adPosition)
        val parentContent = getParentContext(pageEntity, adPosition)

        return AdRequest(adPosition, numOfAds = numOfAds,
                groupKey = getGroupKeyBasedOnSection(cacheItem.section),
                section = cacheItem.section,
                postId = commonAsset?.i_id(),
                entityId = pageEntity?.id,
                entityType = pageEntity?.entityType,
                entitySubType = pageEntity?.subType,
                dhtvAdParams = videoAdParams,
                adExtras = cacheItem.adExtras,
                contentContextMap = contentContext?.let { mapOf(adPosition.value to it) },
                parentContextMap = parentContent?.let { mapOf(adPosition.value to it) },
                // Skipping common cache as Instream cache is separate.
                skipCacheMatching = true)
    }

    private fun getContentContext(commonAsset: CommonAsset?, adPosition: AdPosition) :
            ContentContext? {
        if(commonAsset?.i_adSpec() != null) {
            return AdsUtil.getContentContextFor(commonAsset?.i_adSpec(), adPosition.value)
        }

        return AdsUtil.getContentContextFor(IAdCacheManager.getAdSpec(commonAsset?.i_source()?.id),
                adPosition.value)
    }

    private fun getParentContext(pageEntity: PageEntity?, adPosition: AdPosition) :
            ContentContext? {
        return AdsUtil.getContentContextFor(IAdCacheManager.getAdSpec(pageEntity?.id),
                adPosition.value)
    }

    private fun getGroupKeyBasedOnSection(section: String?) : String {
        if(section?.equals(AppSection.TV.name, true) == true) {
            return AdConstants.BUZZ_GROUP
        }
        return AdConstants.NEWS_GROUP
    }


    companion object {
        private const val TAG = "IAdHelper"
    }
}
