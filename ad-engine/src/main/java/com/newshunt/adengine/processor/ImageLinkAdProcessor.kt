package com.newshunt.adengine.processor

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.transition.Transition
import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.NativeAdImageLink
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.util.AdLogger
import com.newshunt.sdk.network.image.Image
import com.newshunt.sdk.network.image.Image.ImageTarget

/**
 * Processes [NativeAdImageLink]
 *
 * @author raunak.yadav
 */
class ImageLinkAdProcessor(baseAdEntity: BaseAdEntity, private val adReadyHandler: AdReadyHandler) : BaseAdProcessor {
    private val nativeAdImageLink: NativeAdImageLink = baseAdEntity as NativeAdImageLink

    override fun processAdContent(adRequest: AdRequest?) {
        val content = nativeAdImageLink.content
        val imageUrl = content?.imgLink
        if (imageUrl.isNullOrBlank()) {
            AdLogger.e(TAG, "[${nativeAdImageLink.adPosition}]Image url absent. Discarding ad ${nativeAdImageLink.uniqueAdIdentifier}")
            adReadyHandler.onReady(null)
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
        val imageTarget: ImageTarget = object : ImageTarget() {
            override fun onResourceReady(resource: Any, transition: Transition<*>?) {
                adReadyHandler.onReady(nativeAdImageLink)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                AdLogger.e(TAG, "[${nativeAdImageLink.adPosition}]Image download failed $imageUrl. " +
                        "Discarding ad ${nativeAdImageLink.uniqueAdIdentifier}")
                adReadyHandler.onReady(null)
            }
        }
        Image.load(imageUrl).into(imageTarget)
    }

}
private const val TAG = "ImageLinkAdProcessor"