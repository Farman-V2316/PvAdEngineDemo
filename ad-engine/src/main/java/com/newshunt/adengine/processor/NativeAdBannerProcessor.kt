/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.processor

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.transition.Transition
import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.NativeAdBanner
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.model.entity.version.AdTemplate
import com.newshunt.adengine.util.AdLogger
import com.newshunt.sdk.network.image.Image
import com.newshunt.sdk.network.image.Image.ImageTarget

/**
 * Processes [NativeAdBanner]
 *
 * @author raunak.yadav
 */
class NativeAdBannerProcessor(
    private val baseAdEntity: BaseAdEntity,
    private val adReadyHandler: AdReadyHandler
) : BaseAdProcessor, AdReadyHandler {

    override fun processAdContent(adRequest: AdRequest?) {
        if (baseAdEntity !is NativeAdBanner) return
        val content = baseAdEntity.content

        // if AdTemplate is Low, we need not download image.
        if (baseAdEntity.adTemplate == AdTemplate.LOW) {
            adReadyHandler.onReady(baseAdEntity)
            return
        }
        content?.let {
            if (it.iconLink.isNullOrBlank()) {
                // Can fallback to LOW template in case of no imageUrl.
                baseAdEntity.adTemplate = AdTemplate.LOW
                adReadyHandler.onReady(baseAdEntity)
                return
            }
            downloadImage(it)
        }
    }

    /**
     * Downloads image
     *
     * @param content - ad Content
     */
    private fun downloadImage(content: BaseDisplayAdEntity.Content) {
        val imageTarget: ImageTarget = object : ImageTarget() {

            override fun onResourceReady(o: Any, transition: Transition<*>?) {
                if (AdTemplate.ENHANCED_HIGH == baseAdEntity.adTemplate) {
                    if (content.id.isNullOrBlank()) {
                        AdLogger.e(LOG_TAG, "Ad ${baseAdEntity.uniqueAdIdentifier} has E_H template but no content " +
                                "id. Fail.")
                        adReadyHandler.onReady(null)
                    } else {
                        ContentAdProcessor(baseAdEntity, this@NativeAdBannerProcessor)
                            .processAdContent()
                    }
                } else {
                    adReadyHandler.onReady(baseAdEntity)
                }
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                adReadyHandler.onReady(null)
            }
        }
        Image.load(content.iconLink).into(imageTarget)
    }

    override fun onReady(baseAdEntity: BaseAdEntity?) {
        adReadyHandler.onReady(baseAdEntity)
    }

    companion object {
        private const val LOG_TAG = "NativeAdBannerProcessor"
    }

}