/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.NativeViewHelper
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.DataUtil
import java.util.ArrayList

/**
 * ViewHolder to display native ads with AdTemplate as 'LOW'.
 *
 * @author raunak.yadav
 */
class NativeViewHolder(private val viewBinding: ViewDataBinding,
                       uniqueRequestId: Int,
                       private var lifecycleOwner: LifecycleOwner? = null)
    : AdsViewHolder(viewBinding, uniqueRequestId,lifecycleOwner), LifecycleObserver {

    private var adChoicesView: View? = null
    private var nativeHelper: NativeViewHelper? = null
    private val clickableViews: MutableList<View>

    init {
        viewBinding.lifecycleOwner = lifecycleOwner
        clickableViews = ArrayList()
        val titleView = viewBinding.root.findViewById<View>(R.id.ad_title)
        val ctaView = viewBinding.root.findViewById<View>(R.id.cta_button)
        val adIcon = viewBinding.root.findViewById<ImageView>(R.id.ad_icon)
        val adAttr : View? = viewBinding.root.findViewById(R.id.ad_attr)
        adAttr?.let {
            clickableViews.addAll(listOf(titleView, ctaView, adIcon, it))
        } ?: clickableViews.addAll(listOf(titleView, ctaView, adIcon))

        clearableImageViews.add(adIcon)
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    override fun updateView(activity: Activity, baseAdEntity: BaseAdEntity) {
        if (baseAdEntity !is BaseDisplayAdEntity) {
            return
        }
        nativeHelper = getNativeViewHelper(activity, baseAdEntity) ?: return

        if (nativeHelper?.shouldClearClickListeners() == true) {
            AdsUtil.removeClickListenerFromAllChilds(viewBinding.root as ViewGroup)
        }


        val nativeAssets = nativeHelper?.getNativeAssets() ?: return
        updateView(baseAdEntity)

        // Remove any adchoicesView that was previously added.
        val parent = adChoicesView?.parent as? ViewGroup
        parent?.removeView(adChoicesView)
        adChoicesView = nativeHelper?.addAdChoicesView(viewBinding.root as ViewGroup)

        viewBinding.setVariable(BR.item, nativeAssets)
        viewBinding.setVariable(BR.adEntity, baseAdEntity)
        viewBinding.setVariable(BR.showCta, !DataUtil.isEmpty(nativeAssets.ctaText))
        viewBinding.executePendingBindings()

        nativeHelper?.registerViewForInteraction(viewBinding.root, clickableViews)
        baseAdEntity.adReportInfo = AdsUtil.getAdReportInfo(nativeAssets)

    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        if (!baseAdEntity.isShown) {
            super.onCardView(baseAdEntity)

            nativeHelper?.recordImpression()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onDestroy() {
        onDestroy(nativeHelper)
        (viewBinding.root.parent as? ViewGroup)?.removeView(viewBinding.root)
        lifecycleOwner?.lifecycle?.removeObserver(this)
        lifecycleOwner = null
    }
}
