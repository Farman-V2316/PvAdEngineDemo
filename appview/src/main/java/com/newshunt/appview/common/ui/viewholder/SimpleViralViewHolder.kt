/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.viewholder

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset

private const val LOG_TAG_VIRAL = "SimpleViralPostViewHolder"

/**
 * @author shrikant.agrawal
 */
class SimpleViralViewHolder(private val viewBinding: ViewDataBinding,
                            pageReferrer: PageReferrer?,
                            parentLifecycleOwner: LifecycleOwner?,
                            override val uniqueScreenId: Int,
                            override val section: String,
							val isDetailView: Boolean = false,
							displayIndex: Int) : SCVViewHolder(viewBinding.root, uniqueScreenId, section, pageReferrer),
	LifecycleObserver {

	init {
		parentLifecycleOwner?.lifecycle?.addObserver(this)
		contentAdDelegate = ContentAdDelegate(uniqueScreenId)
		this.displayTypeIndex = displayIndex
	}

	override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
		Logger.d(LOG_TAG_VIRAL, "bind $adapterPosition")
		if (item !is CommonAsset) return
		isSCVFired = false
		contentAdDelegate?.bindAd(item)
		analyticsItem = item
		viewBinding.setVariable(BR.adDelegate, contentAdDelegate)
		viewBinding.setVariable(BR.item, item)
		viewBinding.setVariable(BR.cardPosition, cardPosition)
		val imageView = viewBinding.root.findViewById<NHImageView>(R.id.image)
		clearableImageViews.add(imageView)
		if (lifecycleOwner != null) {
			viewBinding.lifecycleOwner = lifecycleOwner

		}
		viewBinding.executePendingBindings()
		if (isDetailView) {
			postEvent(0L)
		}
	}


	@OnLifecycleEvent(Lifecycle.Event.ON_STOP)
	fun onStop() {
		super.onStopCb()
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
	fun onResume() {
		super.onResumeCb()
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
	fun onDestroy() {
		super.onDestroyCb()
	}
}