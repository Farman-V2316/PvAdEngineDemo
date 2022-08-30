/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.databinding.LayoutImageLinkFullAdBinding
import com.newshunt.adengine.listeners.AdExitListener
import com.newshunt.adengine.model.AdInteraction
import com.newshunt.adengine.model.entity.NativeAdImageLink
import com.newshunt.adengine.model.entity.version.AdLPBackAction
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.common.view.customview.NHRoundedCornerImageView

private const val LOG_TAG = "ImageLinkFullAdViewHolder"

/**
 * View takes care of rendering just the image for a [NativeAdImageLink] Ad.
 * If extra views are needed, subclass this.
 *
 * @author raunak.yadav
 */
open class FullPageImageAdViewHolder(viewBinding: LayoutImageLinkFullAdBinding,
                                     uniqueRequestId: Int, parentLifecycleOwner: LifecycleOwner?,
                                     private val adExitListener: AdExitListener? = null) :
    SimpleImageLinkAdVH(viewBinding, uniqueRequestId, parentLifecycleOwner) {

    private val imageView: NHRoundedCornerImageView = viewBinding.imglink

    init {
        clearableImageViews.add(imageView)
    }

    override fun getImageView(): NHRoundedCornerImageView {
        return imageView
    }

    override fun getLogTag(): String {
        return LOG_TAG
    }

    override fun onAdClicked(nativeAdImageLink: NativeAdImageLink) {
        super.onAdClicked(nativeAdImageLink)

        if (nativeAdImageLink.adPosition == AdPosition.EXIT_SPLASH) {
            when (nativeAdImageLink.backFromLpAction) {
                AdLPBackAction.EXIT_APP -> adExitListener?.closeToExitApp(nativeAdImageLink, AdInteraction.USER_CLICK)
                AdLPBackAction.BACK_TO_APP -> adExitListener?.cancelExitApp(AdInteraction.USER_CLICK)
            }
        }
    }
}
