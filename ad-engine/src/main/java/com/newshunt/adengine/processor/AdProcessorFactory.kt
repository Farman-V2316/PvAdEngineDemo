/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.processor

import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.AdsFallbackEntity
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ContentAd
import com.newshunt.adengine.model.entity.EmptyAd
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.model.entity.NativeAdAppDownload
import com.newshunt.adengine.model.entity.NativeAdBanner
import com.newshunt.adengine.model.entity.NativeAdHtml
import com.newshunt.adengine.model.entity.NativeAdImageLink
import com.newshunt.adengine.model.entity.PgiArticleAd
import com.newshunt.adengine.model.entity.version.AdClubType
import com.newshunt.adengine.model.entity.version.AdContentType
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.view.BackupAdsCache
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.util.concurrent.ExecutorService

/**
 * Provides {BaseAdProcessor} for given ad type.
 *
 * @author raunak.yadav
 */
object AdProcessorFactory {

    internal class DummyBaseAdProcessor(private val adReadyHandler: AdReadyHandler,
                                        val baseAdEntity: BaseAdEntity) : BaseAdProcessor {

        override fun processAdContent(adRequest: AdRequest?) {
            AdLogger.d(LOG_TAG, "Sending ad with type = " + baseAdEntity.type)
            adReadyHandler.onReady(baseAdEntity)
        }

        companion object {
            private const val LOG_TAG = "DummyBaseAdProcessor"
        }
    }

    fun getAdProcessor(adGroup: AdsFallbackEntity, adReadyHandler: AdReadyHandler,
                       backupAdsCache: BackupAdsCache?, responseExecutor: ExecutorService,
                       isPersistedAd: Boolean = false): BaseAdProcessor {
        if (adGroup.clubType === AdClubType.SEQUENCE) {
            if (!CommonUtils.isEmpty(adGroup.baseAdEntities) && adGroup.baseAdEntities[0] is MultipleAdEntity) {
                return SequentialAdsProcessor(adGroup.baseAdEntities[0] as MultipleAdEntity,
                        adReadyHandler, backupAdsCache, responseExecutor)
            }
        }
        return AdsFallbackProcessor(adGroup, adReadyHandler, backupAdsCache, responseExecutor, isPersistedAd)
    }

    fun getAdProcessor(baseAdEntity: BaseAdEntity,
                       adReadyHandler: AdReadyHandler): BaseAdProcessor {
        return when (baseAdEntity) {
            is ExternalSdkAd -> ExternalSdkAdProcessor(baseAdEntity, adReadyHandler)
            is NativeAdHtml -> NativeAdHtmlProcessor(adReadyHandler, baseAdEntity)
            is NativeAdImageLink -> ImageLinkAdProcessor(baseAdEntity, adReadyHandler)
            is PgiArticleAd -> PgiArticleAdProcessor(baseAdEntity, adReadyHandler)
            is NativeAdBanner -> NativeAdBannerProcessor(baseAdEntity, adReadyHandler)
            else -> if (baseAdEntity.type == AdContentType.CONTENT_AD) {
                ContentAdProcessor(baseAdEntity, adReadyHandler)
            } else DummyBaseAdProcessor(adReadyHandler, baseAdEntity)
        }
    }

    fun getAdProcessorToProcessPartially(baseAdEntity: BaseAdEntity,
                                         adReadyHandler: AdReadyHandler): BaseAdProcessor {
        if (baseAdEntity is MultipleAdEntity && baseAdEntity.type == AdContentType.EXTERNAL_SDK) {
            return ExternalSdkAdProcessor(baseAdEntity, adReadyHandler)
        } else if (baseAdEntity is NativeAdHtml) {
            return NativeAdHtmlProcessor(adReadyHandler, baseAdEntity, true)
        } else if (baseAdEntity is NativeAdImageLink) {
            return ImageLinkAdProcessor(baseAdEntity, adReadyHandler)
        }
        return DummyBaseAdProcessor(adReadyHandler, baseAdEntity)
    }

    fun fromAdContentType(contentAdType: AdContentType?): Class<*>? {
        return when (contentAdType) {
            AdContentType.EMPTY_AD -> EmptyAd::class.java
            AdContentType.IMAGE_LINK -> NativeAdImageLink::class.java
            AdContentType.APP_DOWNLOAD -> NativeAdAppDownload::class.java
            AdContentType.EXTERNAL_SDK -> ExternalSdkAd::class.java
            AdContentType.NATIVE_BANNER -> NativeAdBanner::class.java
            AdContentType.PGI_ZIP, AdContentType.MRAID_ZIP, AdContentType.PGI_EXTERNAL, AdContentType.MRAID_EXTERNAL -> NativeAdHtml::class.java
            AdContentType.PGI_ARTICLE_AD -> PgiArticleAd::class.java
            AdContentType.CONTENT_AD -> ContentAd::class.java
            else -> null
        }
    }
}
