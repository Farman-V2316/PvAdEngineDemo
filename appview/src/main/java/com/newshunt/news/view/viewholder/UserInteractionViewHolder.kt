/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.BR
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.adapter.UpdateableCardView
import com.newshunt.appview.common.ui.viewholder.CardsViewHolder
import com.newshunt.appview.databinding.LayoutProfileActivityCardBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.bundleOf
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.news.helper.NewsListCardLayoutUtil
import com.newshunt.news.view.ListEditInterface


/**
 * A Viewholder to show the user interaction activities and responses
 * @author srikanth.r
 */

class UserInteractionViewHolder(private val viewBinding: ViewDataBinding,
                                private val listEditInterface: ListEditInterface?,
                                private val pageReferrer: PageReferrer? = null,
                                private val nsfwLiveData: MutableLiveData<Boolean>,
                                private val referrerProviderlistener: ReferrerProviderlistener? =
                                        null) : CardsViewHolder(viewBinding.root) {


    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        (item as? CommonAsset)?.let { asset ->
            (viewBinding as LayoutProfileActivityCardBinding?)?.let { binding ->

                binding.setVariable(BR.item, asset)
                binding.setVariable(BR.nsfwLiveData, nsfwLiveData)
                binding.setVariable(BR.cardPosition, cardPosition)
                viewBinding.setVariable(BR.args, bundleOf(Constants.STORY_POSITION to cardPosition))
                NewsListCardLayoutUtil.manageLayoutDirection(binding.root)

                val titleViewParams = binding.interactionChain.layoutParams as ConstraintLayout.LayoutParams

                if (listEditInterface?.isInEditMode() == true) {
                    titleViewParams.endToStart = R.id.user_interaction_delete
                    titleViewParams.marginEnd = CommonUtils.getDimension(R.dimen.history_title_rightMarginEdit)
                    viewBinding.userInteractionDelete.visibility = View.VISIBLE
                } else {
                    titleViewParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    titleViewParams.marginEnd = CommonUtils.getDimension(R.dimen
                            .history_title_rightMargin)
                    binding.userInteractionDelete.visibility = View.GONE
                }
                binding.interactionChain.layoutParams = titleViewParams
                binding.executePendingBindings()
                AnalyticsHelper2.logStoryCardViewEvent(item, pageReferrer, adapterPosition,
                        Constants.EMPTY_STRING, null, referrerProviderlistener)
            }
        }
    }
}

/**
 * ViewHolder to display
 */
class DateViewHolder(view: View) : CardsViewHolder(view) {
    private val interactionsDateView = (view as ViewGroup).findViewById<NHTextView>(R.id.interactions_date)

    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        if (item !is String) return
        interactionsDateView.text = item
    }
}