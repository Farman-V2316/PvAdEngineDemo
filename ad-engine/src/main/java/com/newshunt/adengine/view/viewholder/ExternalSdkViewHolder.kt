/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.databinding.ExternalAdContainerBinding
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.ExternalSdkAd
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.view.helper.AmazonAdViewHelper
import com.newshunt.adengine.view.helper.DfpViewHelper
import com.newshunt.adengine.view.helper.FacebookAdViewHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Handles External banner/interstitial sdk ads including dfp, FB etc.
 *
 * @author raunak.yadav
 */
private const val LOG_TAG = "ExternalSdkViewHolder"
class ExternalSdkViewHolder(private val viewBinding: ViewDataBinding, uniqueRequestId: Int = -1, parentLifecycleOwner: LifecycleOwner?)
    : AdsViewHolder(viewBinding, uniqueRequestId, parentLifecycleOwner) {

    private val view = viewBinding.root
    private var activity: Activity? = null
    private var adContainerLayout: ConstraintLayout? = null

    init {
        this.view.visibility = View.GONE
        if(viewBinding is ExternalAdContainerBinding) {
            adContainerLayout = viewBinding.externalAdContainer
            viewBinding.externalAdLayout.setTag(R.id.dh_view_tag_id, AdConstants.DH_VIEW_TAG)
            viewBinding.sponsoredText.setTag(R.id.dh_view_tag_id, AdConstants.DH_VIEW_TAG)
        }
        viewBinding.lifecycleOwner = parentLifecycleOwner
    }

    override fun updateView(activity: Activity, baseDisplayAdEntity: BaseAdEntity) {
        this.activity = activity
        if (baseDisplayAdEntity !is ExternalSdkAd) {
            return
        }
        if (CommonUtils.isEmpty(baseDisplayAdEntity.external?.adUnitId)) {
            return
        }

        updateView(baseDisplayAdEntity)
        baseDisplayAdEntity.external?.data ?: return

        removeSdkOwnedViews()

        if (baseDisplayAdEntity.external?.data?.startsWith(AdConstants.DFP_AD) == true) {
            val dfpViewHolder = DfpViewHelper(view, activity)
            dfpViewHolder.updateView(baseDisplayAdEntity)

        } else if (baseDisplayAdEntity.external?.data?.startsWith(AdConstants.FB_AD) == true) {
            val facebookAdViewHolder = FacebookAdViewHelper(activity)
            facebookAdViewHolder.updateView(baseDisplayAdEntity)
        } else if (baseDisplayAdEntity.external?.data?.startsWith(AdConstants.AMAZON_AD) == true) {
            val amazonAdViewHolder = AmazonAdViewHelper(view, activity)
            amazonAdViewHolder.updateView(baseDisplayAdEntity)
        }

        viewBinding.setVariable(BR.adEntity, baseDisplayAdEntity)
        viewBinding.executePendingBindings()

        when (baseDisplayAdEntity.adPosition) {
            AdPosition.LIST_AD, AdPosition.P0, AdPosition.PP1,
            AdPosition.SUPPLEMENT, AdPosition.STORY -> {
                val padding = CommonUtils.getDimension(R.dimen.ad_content_top_bottom_margin)
                view.setPadding(0, padding, 0, padding)
            }
            else -> {
            }
        }
    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        if (baseAdEntity.isShown) {
            return
        }
        if (baseAdEntity is ExternalSdkAd && baseAdEntity.external?.manualImpression == true) {
            (baseAdEntity.nativeAdObject as? AdManagerAdView?)?.let {
                Logger.d(LOG_TAG, "DFP recordManualImpression() called")
                it.recordManualImpression()
            }
        }
        super.onCardView(baseAdEntity)
    }

    override fun onDestroy() {
    }

    private fun removeSdkOwnedViews() {
        adContainerLayout?.let {
            for (i in 0 until it.childCount) {
                val childView = it.getChildAt(i)
                // remove only non DH child views
                if (childView.getTag(R.id.dh_view_tag_id) == null) {
                    it.removeView(childView)
                }
            }
        }
    }
}
