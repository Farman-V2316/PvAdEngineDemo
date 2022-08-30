/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.processor

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.transition.Transition
import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.PgiArticleAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.sdk.network.image.Image

/**
 * Processes [PgiArticleAd]
 *
 * @author raunak.yadav
 */
class PgiArticleAdProcessor(baseAdEntity: BaseAdEntity, private val adReadyHandler: AdReadyHandler) : BaseAdProcessor {
    private val pgiArticleAd: PgiArticleAd = baseAdEntity as PgiArticleAd

    override fun processAdContent(adRequest: AdRequest?) {
        val imageUrl = pgiArticleAd.content?.itemImage?.data
        // if image url is not available, return ad ready. As Pgi articale ad is flexible to have
        // either only image or only html or both
        if (imageUrl.isNullOrBlank()) {
            adReadyHandler.onReady(pgiArticleAd)
            return
        }
        downloadImage(imageUrl)
    }

    /**
     * Downloads image from image url
     *
     * @param imageUrl - image url
     */
    private fun downloadImage(imageUrl: String) {
        if (pgiArticleAd.adPosition == AdPosition.SPLASH_DEFAULT) {
            downloadSplashImage(imageUrl)
        } else {
            val imageTarget = object : Image.ImageTarget() {
                override fun onResourceReady(o: Any, transition: Transition<*>?) {
                    adReadyHandler.onReady(pgiArticleAd)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    adReadyHandler.onReady(null)
                }
            }
            Image.load(imageUrl).into(imageTarget)
        }
    }

    private fun downloadSplashImage(imageUrl: String) {
        val prevUrl = PreferenceManager.getPreference(GenericAppStatePreference.SPLASH_IMAGE_URL, Constants.EMPTY_STRING)
        val splashFile = AdsUtil.getPersistedSplashFile()
        if (!prevUrl.equals(imageUrl, true) || splashFile?.exists() != true) {
            val imageTarget = SplashBitmapTarget(pgiArticleAd, adReadyHandler)
            Image.load(imageUrl, true).into(imageTarget)
        } else {
            adReadyHandler.onReady(pgiArticleAd)
        }
    }
}
