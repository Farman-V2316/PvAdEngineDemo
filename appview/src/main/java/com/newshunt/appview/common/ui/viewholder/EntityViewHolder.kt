package com.newshunt.appview.common.ui.viewholder

import androidx.constraintlayout.widget.Barrier
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.newshunt.analytics.entity.NhAnalyticsAppEvent
import com.newshunt.appview.BR
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.appview.databinding.EntityItemBinding
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.UserFollowView
import com.newshunt.dataentity.news.analytics.NewsReferrer
import com.newshunt.dataentity.notification.FollowModel
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.news.util.EventDedupHelper
import com.newshunt.news.util.EventKey
import com.newshunt.news.util.NewsConstants


private const val LOG_TAG = "EntityViewHolder"

class EntityViewHolder(private val viewBinding: ViewDataBinding,
					   val pageReferrer: PageReferrer? = null,
					   private val showAddPageButton: Boolean = false,
					   private val cardsViewModel: CardsViewModel,
					   val eventDedupHelper: EventDedupHelper,
					   val section: String) : CardsViewHolder(viewBinding.root) {

	override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
		Logger.d(LOG_TAG, "bind $adapterPosition")
		val isUrduSelected = CommonUtils.equals(UserPreferenceUtil.getUserNavigationLanguage(),
				NewsConstants.URDU_LANGUAGE_CODE)
		viewBinding.setVariable(BR.item, item)
		viewBinding.setVariable(BR.state, PerspectiveState())
		viewBinding.setVariable(BR.cardPosition, cardPosition)
		viewBinding.setVariable(BR.showAddButton, showAddPageButton ||
				pageReferrer?.referrer == NewsReferrer.ADD_LOCATION)
		if(viewBinding is EntityItemBinding && item is UserFollowView && isUrduSelected){
				viewBinding.frameBarrier.type = Barrier.END
		}
		try {
			if (lifecycleOwner != null) {
				viewBinding.lifecycleOwner = lifecycleOwner

			}
		} catch (e: Exception) {
			Logger.caughtException(e)
		}
		viewBinding.executePendingBindings()
		if (item is UserFollowView) {
			val isFPV = cardsViewModel.fragmentBundle?.getBoolean(Constants.BUNDLE_IS_FPV) ?: true
			val model = cardsViewModel.fragmentBundle?.getString(Constants.BUNDLE_FOLLOW_MODEL) ?: FollowModel.FOLLOWING.name
			val eventKey = EventKey(NhAnalyticsAppEvent.ENTITY_CARD_VIEW,
					mapOf(Constants.ITEM_ID to item.i_id()))
			eventDedupHelper.fireEvent(eventKey, Runnable {
				AnalyticsHelper2.logEntityCardView(item.actionableEntity,
						cardPosition, pageReferrer = pageReferrer, section = section, isFPV =
				isFPV, model = model, parent = null)
			})
		}
	}
}