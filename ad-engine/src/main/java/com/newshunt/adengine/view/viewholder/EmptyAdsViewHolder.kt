/*
 * Copyright (c) 2021  Newshunt. All rights reserved.
 */

package com.newshunt.adengine.view.viewholder

import android.app.Activity
import androidx.databinding.ViewDataBinding
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.common.helper.common.Logger

/**
 * Created by helly.patel on 4/6/21.
 */

private const val LOG_TAG = "EmptyAdsViewHolder"
class EmptyAdsViewHolder(viewBinding: ViewDataBinding, uniqueRequestId: Int = -1)
    : AdsViewHolder(viewBinding, uniqueRequestId) {

    override fun updateView(activity: Activity?, baseDisplayAdEntity: BaseAdEntity?) {
        baseDisplayAdEntity?.let { super.updateView(it, true) }
        Logger.d(LOG_TAG, "updateView")
    }

    override fun onDestroy() {
        Logger.d(LOG_TAG, "onDestroy")
    }
}