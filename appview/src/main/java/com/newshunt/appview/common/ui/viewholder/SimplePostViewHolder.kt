/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.viewholder

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.newshunt.adengine.model.entity.ContentAdDelegate
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.VideoPrefetchCallback
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dhutil.bundleOf

class SimplePostViewHolder(private val viewBinding: ViewDataBinding,
                           pageReferrer: PageReferrer?,
                           override val uniqueScreenId: Int,
                           override val section: String,
                           parentLifecycleOwner: LifecycleOwner?,
                           val isDetailView: Boolean = false,
                           prefetchListener: VideoPrefetchCallback? = null,
                           displayIndex: Int) :
    SCVViewHolder(viewBinding.root, uniqueScreenId, section, pageReferrer, prefetchListener), LifecycleObserver, CommonAssetViewHolder {

    private var prevCard: CommonAsset? = null
    private var perspectiveState: PerspectiveState? = null

    init {
        parentLifecycleOwner?.lifecycle?.addObserver(this)
        contentAdDelegate = ContentAdDelegate(uniqueScreenId)
        displayTypeIndex = displayIndex
    }

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        Logger.d(LOG_TAG, "bind $adapterPosition")
        if (item == null) {
            return
        }
        isSCVFired = false
        this.cardPosition = cardPosition
        contentAdDelegate?.reset()
        if (item is CommonAsset) {
            contentAdDelegate?.bindAd(item)
            viewBinding.setVariable(BR.isLive,item.i_videoAsset()?.liveStream ?: false)
        }
        viewBinding.setVariable(BR.item, item)
        viewBinding.setVariable(BR.adDelegate, contentAdDelegate)
        if ((item as? CommonAsset)?.i_id() == prevCard?.i_id())
            viewBinding.setVariable(BR.state, perspectiveState)
        else {
            perspectiveState = PerspectiveState()
            viewBinding.setVariable(BR.state, perspectiveState)
        }
        viewBinding.setVariable(BR.cardPosition, cardPosition)
        val imageView = viewBinding.root.findViewById<NHImageView>(R.id.news_image)
        clearableImageViews.add(imageView)
        viewBinding.setVariable(BR.args, bundleOf(Constants.STORY_POSITION to cardPosition))
        try {
            if (lifecycleOwner != null) {
                viewBinding.lifecycleOwner = lifecycleOwner

            }
        } catch (e: Exception) {
            Logger.caughtException(e)
        }
        viewBinding.executePendingBindings()
        if (item is CommonAsset) {
            analyticsItem = item
        }
        prevCard = item as? CommonAsset

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

    override fun curData(): Pair<View, CommonAsset?> = itemView to prevCard
}

class PerspectiveState : BaseObservable() {
    var collapsed: Boolean = false
        @Bindable
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.collapsed)
        }
}