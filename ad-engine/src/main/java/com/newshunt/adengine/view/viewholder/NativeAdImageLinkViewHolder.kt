/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.newshunt.adengine.R
import com.newshunt.adengine.databinding.NewsItemTypeImageLinkAdBinding
import com.newshunt.adengine.model.entity.NativeAdImageLink
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.view.customview.NHRoundedCornerImageView
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Represents data to be shown in image link type ad.
 *
 * @author Mukesh Yadav
 */
class NativeAdImageLinkViewHolder(viewBinding: NewsItemTypeImageLinkAdBinding,
                                  uniqueRequestId: Int, parentLifecycleOwner: LifecycleOwner?) :
    SimpleImageLinkAdVH(viewBinding, uniqueRequestId, parentLifecycleOwner) {

    private val imageView: NHRoundedCornerImageView
    private var view: View = viewBinding.root

    init {
        this.view.visibility = View.GONE

        imageView = viewBinding.imglink
        clearableImageViews.add(imageView)
        borderContainer = viewBinding.borderContainer
        viewBinding.lifecycleOwner = parentLifecycleOwner
    }

    override fun getImageView(): NHRoundedCornerImageView {
        return imageView
    }

    override fun setImageHeight(adEntity: NativeAdImageLink) {
        val adWidth = adEntity.width?:AdConstants.DEFAULT_AD_SIZE
        val adHeight = adEntity.height?:AdConstants.DEFAULT_AD_SIZE
        val aspectRatio = adWidth.toFloat() / adHeight
        val width = CommonUtils.getDeviceScreenWidth() - 2 * CommonUtils.getDimension(R.dimen.ad_content_margin)
        imageView.layoutParams?.height =
            AdsUtil.getHeightWithAspectRatio(adWidth, adHeight, aspectRatio, width)
    }

    override fun getLogTag(): String {
        return LOG_TAG
    }
}

private const val LOG_TAG = "NativeAdImageLinkViewHolder"
