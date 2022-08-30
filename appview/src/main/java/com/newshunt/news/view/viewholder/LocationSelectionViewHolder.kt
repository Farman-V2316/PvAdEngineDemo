/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.viewholder

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import com.google.android.flexbox.*
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.viewholder.CardsViewHolder
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.appview.databinding.LayoutLocationSelectionViewholderBinding
import com.newshunt.common.helper.common.AndroidUtils.startActivity
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.entity.AnalyticsParam
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.EntityItem
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.analytics.NhAnalyticsNewsEvent
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam
import com.newshunt.news.util.EventDedupHelper
import com.newshunt.news.view.adapter.LocationSelectionAdapter
import com.newshunt.news.view.listener.LocationSelectListener
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder


class LocationSelectionViewHolder(val viewDataBinding: ViewDataBinding,
                                  val vm: ClickHandlingViewModel,
                                  private val section: String,
                                  val eventDedupHelper: EventDedupHelper,
                                  val pageReferrer: PageReferrer?) : CardsViewHolder(viewDataBinding.root),
        LocationSelectListener,
        SeeAllClickListener,
        VisibilityAwareViewHolder {

    private var isViewCountIncreamented: Boolean = false
    private val context = viewDataBinding.root.context
    private val locationViewBinding = viewDataBinding as LayoutLocationSelectionViewholderBinding


    private val saveButton = locationViewBinding.saveLocationSelection

    private val userSelectedLocation = mutableListOf<EntityItem>()

    lateinit var selectionLocationList: List<EntityItem>
    private val locationCard = viewDataBinding.root.findViewById<ConstraintLayout>(R.id
            .location_card)
    private var eventFired = false
    private val visibiltyPercentageForCardView = 50 //visibility percentage for this card to be

    // considered as viewed.
    private val LOG_TAG = "LocationSelection"


    override fun bind(item: Any?, lifecycleOwner: LifecycleOwner?, cardPosition: Int) {
        if (item !is CommonAsset) {
            return
        }
        selectionLocationList = item.i_entityCollection() ?: emptyList()

        if (ThemeUtils.isNightMode()) {
            locationCard.setBackgroundColor(CommonUtils.getColor(R.color.theme_night_background))
        } else {
            locationCard.background = CommonUtils.getDrawable(R.drawable.location_card_shadow_background)
        }

        locationViewBinding.selectLocationText.text = item.i_title()
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.setJustifyContent(JustifyContent.CENTER);
        layoutManager.setAlignItems(AlignItems.CENTER);
        layoutManager.flexWrap = FlexWrap.WRAP
        locationViewBinding.moreLocationList
                .layoutManager =
                layoutManager

        val adapter = LocationSelectionAdapter(selectionLocationList, this, this)
        locationViewBinding.moreLocationList.adapter = adapter


        locationViewBinding.saveLocationSelection.setOnClickListener {
            saveChanges(saveButton, item)
        }
        fireCardWidgetView(item)


    }

    private fun fireCardWidgetView(item: CommonAsset) {
        if (eventFired) {
            return
        }
        eventFired = true
        val map = mutableMapOf<NhAnalyticsEventParam, Any>()
        map[NhAnalyticsNewsEventParam.WIDGET_TYPE] = Constants.LOCATION_SELECTION
        map[NhAnalyticsNewsEventParam.WIDGET_PLACEMENT] = Constants.IN_LIST
        map[AnalyticsParam.CARD_POSITION] = adapterPosition
        AnalyticsClient.log(NhAnalyticsNewsEvent.CARD_WIDGET_VIEW, NhAnalyticsEventSection.NEWS, map)

    }

    override fun onSeeAllClicked() {
        val eventParams = HashMap<NhAnalyticsEventParam, Any>()
        eventParams[NhAnalyticsNewsEventParam.TYPE] = Constants.SEE_ALL
        fireExploreButtonEvent(eventParams)

        CommonNavigator.openLocationSelection(this.context,true, false)
    }

    private fun saveChanges(view: View, item: CommonAsset) {
        item?.let {
            val args = Bundle()
            args.putSerializable(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            vm.onFollowEntities(view, userSelectedLocation, args, item)
            val eventParams = HashMap<NhAnalyticsEventParam, Any>()
            eventParams[NhAnalyticsNewsEventParam.TYPE] = Constants.SAVE_BUTTON
            fireExploreButtonEvent(eventParams)
        }
    }

    fun fireExploreButtonEvent(eventParams: HashMap<NhAnalyticsEventParam, Any>) {
        AnalyticsClient.log(NhAnalyticsNewsEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection
                .NEWS, eventParams, PageReferrer(NhGenericReferrer.LOCATION_SELECTION_CARD))
    }


    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        incrementCardViewCount(viewVisibilityPercentage)
    }

    override fun onInVisible() {
    }

    override fun onUserLeftFragment() {
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        incrementCardViewCount(viewVisibilityPercentage)
    }

    private fun incrementCardViewCount(viewVisibilityPercentage: Int) {
        if ((viewVisibilityPercentage < visibiltyPercentageForCardView) ||
                isViewCountIncreamented) {
            return
        }
        val timesShownToUser = PreferenceManager.getPreference(GenericAppStatePreference
                .LANG_CARD_TIMES_SHOWN_TO_USER, 0) + 1
        PreferenceManager.savePreference(GenericAppStatePreference
                .LANG_CARD_TIMES_SHOWN_TO_USER, timesShownToUser)
        isViewCountIncreamented = true
        Logger.d(LOG_TAG, "The number of times the card is shown to the user is :$timesShownToUser")
    }

    override fun onLocationSelected(position: Int, selected: Boolean, autoSelected: Boolean) {
        selectionLocationList?.let { list ->
            if (userSelectedLocation.contains(list[position])) {
                userSelectedLocation.remove(list[position])
            } else {
                userSelectedLocation.add(list[position])
            }
            if (userSelectedLocation.isEmpty()) {
                saveButton.setBackgroundColor(CommonUtils.getColor(CommonUtils.getResourceIdFromAttribute(context, R
                        .attr.language_save_button_background)))
                saveButton.setTextColor(CommonUtils.getColor(CommonUtils.getResourceIdFromAttribute(context, R.attr
                        .language_save_button_textcolor)))
            } else {
                saveButton.setBackgroundColor(CommonUtils.getColor(R.color.save_language_button_selected))
                saveButton.setTextColor(CommonUtils.getColor(R.color.white_color))
            }
        }

        val eventParams = HashMap<NhAnalyticsEventParam, Any>()
        eventParams[NhAnalyticsNewsEventParam.TYPE] = Constants.LOCATION_CLICK
        fireExploreButtonEvent(eventParams)
    }
}



