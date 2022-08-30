/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.client

import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import com.newshunt.adengine.ReadPersistedAdUsecase
import com.newshunt.adengine.RemovePersistedAdUsecase
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.view.helper.AdBinderRepo.clear
import com.newshunt.adengine.view.helper.ExitSplashAdCommunication
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.dataentity.ads.AdFCType
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.news.model.sqlite.SocialDB.Companion.instance
import com.newshunt.news.model.usecase.toMediator2
import com.newshunt.sdk.network.Priority
import com.squareup.otto.Bus
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.HashSet

/**
 * Manages ad-inventory. When ad count reached below threshold it
 * fills it up again. It also allows ad manager to get the next ad
 * to be shown and clean up inventory in-case of source change.
 *
 * @author shreyas.desai
 */
class NativeAdInventoryManager private constructor(bus: Bus, cacheSize: Int,
                                                   cacheThreshold: Int,
                                                   adPosition: AdPosition) : ExcludedBannerProvider {
    private val adRepository: AdRepository
    private val displayedAds: MutableList<String> = ArrayList()
    private val lowMemoryObserver = Observer { isLowMemory: Boolean ->
        if (isLowMemory) {
            /*onLowMemory();*/
        }
    }

    /**
     * Returns the cached ads immediately and posts on bus, if marked so.
     * TODO(raunak.yadav) : To remove posting cached ads on bus when all zones support this.
     * For now only Masthead does.
     * If no/low cache, will also send out a network request.
     *
     * @return null - if request discarded.
     * NativeAdContainer with available cached ads.
     */
    fun requestAds(adRequest: AdRequest?, uniqueRequestId: Int,
                   priority: Priority?,
                   adsUpgradeInfo: AdsUpgradeInfo?,
                   postLocalAdsAsync: Boolean): NativeAdContainer? {
        return adRepository.requestAds(adRequest!!, uniqueRequestId, priority!!, adsUpgradeInfo,
                postLocalAdsAsync)
    }

    override val excludeBannerIds: List<String>
        get() = displayedAds

    override fun updateExcludeBannerList(adEntity: BaseAdEntity) {
        if (adEntity is BaseDisplayAdEntity && !DataUtil.isEmpty(adEntity.campaignId)) {
            displayedAds.add(adEntity.campaignId!!)
        } else if (adEntity is MultipleAdEntity) {
            val baseDisplayAdEntities: List<BaseDisplayAdEntity> = adEntity
                    .baseDisplayAdEntities
            for (baseDisplayAdEntity in baseDisplayAdEntities) {
                if (!DataUtil.isEmpty(baseDisplayAdEntity.campaignId)) {
                    displayedAds.add(baseDisplayAdEntity.campaignId!!)
                }
            }
        }
    }

    override fun updateExcludeBannerListOnContextChange(request: AdRequest) {
        // If context switch happens remove pending requests.
/*  TODO(raunak)
  if (pageType != PageType.fromName(request.getPageType()) &&
        request.getZoneType() != AdPosition.PGI) {
      displayedAds.clear();
      AdLogger.w(LOG_TAG, "Clearing requests on Context Change");
    }
    pageType = PageType.fromName(request.getPageType());*/
    }

    fun getBackupAd(request: AdRequest?): BaseAdEntity? {
        return adRepository.getBackupAd(request)
    }

    fun getAd(adRequest: AdRequest?): BaseAdEntity? {
        return adRepository.getAd(adRequest)
    }

    fun readPersistedAds(count: Int? = null) {
        adRepository.readAndProcessPersistedAds(count)
    }

    fun deletePersistedAds() {
        adRepository.deletePersistedAds()
    }

    fun clearInventory(clearOnlyUnAttachedAds: Boolean = false) {
        adRepository.clearInventory(clearOnlyUnAttachedAds)
        AndroidUtils.getMainThreadHandler().post {
            CommonUtils.getLowMemoryLiveData().removeObserver(lowMemoryObserver)
        }
    }

    companion object {
        private var cardP1Instance: NativeAdInventoryManager? = null
        private var pgiInstance: NativeAdInventoryManager? = null
        private var storyInstance: NativeAdInventoryManager? = null
        private var supplementAdsInstance: NativeAdInventoryManager? = null
        private var p0Instance: NativeAdInventoryManager? = null
        private var pp1Instance: NativeAdInventoryManager? = null
        private var vdopgiInstance: NativeAdInventoryManager? = null
        private var splashInstance: WeakReference<NativeAdInventoryManager?>? = null
        private var inlineVideoInstance: NativeAdInventoryManager? = null
        private var inStreamVideoInstance: NativeAdInventoryManager? = null
        private var mastHeadInstance: NativeAdInventoryManager? = null
        private var dhtvMastHeadInstance: NativeAdInventoryManager? = null
        private var defaultSplashInstance: NativeAdInventoryManager? = null
        private var evergreenCacheInstance: NativeAdInventoryManager? = null
        private var exitSplashInstance: NativeAdInventoryManager? = null

        fun getCardP1Instance(): NativeAdInventoryManager? {
            if (cardP1Instance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (cardP1Instance == null) {
                        cardP1Instance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0,
                                AdPosition.CARD_P1)
                    }
                }
            }
            return cardP1Instance
        }

        fun getPgiInstance(): NativeAdInventoryManager? {
            if (pgiInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (pgiInstance == null) {
                        pgiInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0,
                                AdPosition.PGI)
                    }
                }
            }
            return pgiInstance
        }

        fun getVdoPgiInstance(): NativeAdInventoryManager? {
            if (vdopgiInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (vdopgiInstance == null) {
                        vdopgiInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0,
                                AdPosition.VDO_PGI)
                    }
                }
            }
            return vdopgiInstance
        }

        fun getStoryInstance(): NativeAdInventoryManager? {
            if (storyInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (storyInstance == null) {
                        storyInstance = NativeAdInventoryManager(
                                BusProvider.getAdBusInstance(), 1, 0, AdPosition.STORY)
                    }
                }
            }
            return storyInstance
        }

        fun getSupplementAdsInstance(): NativeAdInventoryManager? {
            if (supplementAdsInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (supplementAdsInstance == null) {
                        supplementAdsInstance = NativeAdInventoryManager(
                                BusProvider.getAdBusInstance(), 1, 0, AdPosition.SUPPLEMENT)
                    }
                }
            }
            return supplementAdsInstance
        }

        fun getP0Instance(): NativeAdInventoryManager? {
            if (p0Instance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (p0Instance == null) {
                        p0Instance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0, AdPosition.P0)
                    }
                }
            }
            return p0Instance
        }

        fun getPP1Instance(): NativeAdInventoryManager? {
            if (pp1Instance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (pp1Instance == null) {
                        pp1Instance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0, AdPosition.PP1)
                    }
                }
            }
            return pp1Instance
        }

        fun getSplashInstance(): NativeAdInventoryManager? {
            if (splashInstance == null || splashInstance!!.get() == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (splashInstance == null || splashInstance!!.get() == null) {
                        val nativeAdInventoryManager = NativeAdInventoryManager(BusProvider.getAdBusInstance(),
                                1, 0, AdPosition.SPLASH)
                        splashInstance = WeakReference(nativeAdInventoryManager)
                    }
                }
            }
            return splashInstance!!.get()
        }

        fun getInlineVideoInstance(): NativeAdInventoryManager? {
            if (inlineVideoInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (inlineVideoInstance == null) {
                        inlineVideoInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1,
                                0, AdPosition.INLINE_VIDEO)
                    }
                }
            }
            return inlineVideoInstance
        }

        fun getInStreamVideoInstance(): NativeAdInventoryManager? {
            if (inStreamVideoInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (inStreamVideoInstance == null) {
                        inStreamVideoInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0,
                                AdPosition.INSTREAM_VIDEO)
                    }
                }
            }
            return inStreamVideoInstance
        }

        fun getMastHeadInstance():NativeAdInventoryManager? {
            if (mastHeadInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (mastHeadInstance == null) {
                        mastHeadInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0,
                                AdPosition.MASTHEAD)
                    }
                }
            }
            return mastHeadInstance
        }

        fun getDhtvMastHeadInstance(): NativeAdInventoryManager? {
            if (dhtvMastHeadInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (dhtvMastHeadInstance == null) {
                        dhtvMastHeadInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0,
                                AdPosition.DHTV_MASTHEAD)
                    }
                }
            }
            return dhtvMastHeadInstance
        }

        fun getDefaultSplashInstance(): NativeAdInventoryManager? {
            if (defaultSplashInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (defaultSplashInstance == null) {
                        defaultSplashInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0,
                                AdPosition.SPLASH_DEFAULT)
                    }
                }
            }
            return defaultSplashInstance
        }

        fun getEvergreenCacheInstance(): NativeAdInventoryManager? {
            if (evergreenCacheInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (evergreenCacheInstance == null) {
                        evergreenCacheInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 50, 0, AdPosition.EVERGREEN)
                    }
                }
            }
            return evergreenCacheInstance
        }

        fun getExitSplashInstance(): NativeAdInventoryManager? {
            if (exitSplashInstance == null) {
                synchronized(NativeAdInventoryManager::class.java) {
                    if (exitSplashInstance == null) {
                        exitSplashInstance = NativeAdInventoryManager(BusProvider.getAdBusInstance(), 1, 0,
                            AdPosition.EXIT_SPLASH)
                    }
                }
            }
            return exitSplashInstance
        }

        @JvmStatic
        fun clearFCExhaustedAds(capId: String, fcType: AdFCType) {
            p0Instance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            pp1Instance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            cardP1Instance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            storyInstance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            mastHeadInstance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            supplementAdsInstance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            pgiInstance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            dhtvMastHeadInstance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            inStreamVideoInstance?.adRepository?.clearFCExhaustedAds(capId, fcType)
            exitSplashInstance?.adRepository?.clearFCExhaustedAds(capId, fcType)
        }

        @JvmStatic
        fun deleteInventory() {
            cardP1Instance?.clearInventory()
            cardP1Instance = null
            pgiInstance?.clearInventory()
            pgiInstance = null
            storyInstance?.clearInventory()
            storyInstance = null
            p0Instance?.clearInventory()
            p0Instance = null
            pp1Instance?.clearInventory()
            pp1Instance = null
            splashInstance?.get()?.clearInventory()
            splashInstance = null
            vdopgiInstance?.clearInventory()
            vdopgiInstance = null
            inlineVideoInstance?.clearInventory()
            inlineVideoInstance = null
            inStreamVideoInstance?.clearInventory()
            inStreamVideoInstance = null
            supplementAdsInstance?.clearInventory()
            supplementAdsInstance = null
            mastHeadInstance?.clearInventory()
            mastHeadInstance = null
            dhtvMastHeadInstance?.clearInventory()
            dhtvMastHeadInstance = null
            exitSplashInstance?.clearInventory(true)
            exitSplashInstance = null
            ExitSplashAdCommunication.exitAdRequested = false
            clear()
        }

    /*  private void onLowMemory() {
    //When system goes low on memory, trim the contextual cache to keep only the latest one.
    AdLogger.e(LOG_TAG, "LOW MEMORY, TRIMMING REPOSITORY CACHE");
    adRepositoryCache.trimToSize(1);
  }*/
    }

    init {
        val adsDao = instance().adsDao()
        adRepository = if (adPosition == AdPosition.EVERGREEN) {
            EvergreenAdRepository(bus, cacheSize, cacheThreshold, adPosition, this,
                ReadPersistedAdUsecase(adsDao), RemovePersistedAdUsecase(adsDao).toMediator2())
        } else {
            AdRepository(bus, cacheSize, cacheThreshold, adPosition, this,
                ReadPersistedAdUsecase(adsDao), RemovePersistedAdUsecase(adsDao).toMediator2())
        }
        AndroidUtils.getMainThreadHandler()
                .post {
                    CommonUtils.getLowMemoryLiveData()
                            .observe(ProcessLifecycleOwner.get(), lowMemoryObserver)
                }
    }
}