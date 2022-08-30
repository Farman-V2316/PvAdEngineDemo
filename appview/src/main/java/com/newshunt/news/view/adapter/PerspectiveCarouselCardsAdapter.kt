/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.CardsBindUtils
import com.newshunt.appview.common.ui.viewholder.PerspectiveState
import com.newshunt.appview.common.viewmodel.CardsViewModel
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper2


class PerspectiveCarouselCardsAdapter(val vm: CardsViewModel?,
									  val state: PerspectiveState? = null) :
	RecyclerView.Adapter<PerspectiveCarouselCardsViewHolderNew>() {

	private var items: List<CommonAsset>? = null
	private var parent: CommonAsset? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PerspectiveCarouselCardsViewHolderNew {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.perspective_card_item, parent, false)
		return PerspectiveCarouselCardsViewHolderNew(view)
	}

	override fun getItemCount(): Int {
		return items?.size ?: 0
	}

	override fun onBindViewHolder(holder: PerspectiveCarouselCardsViewHolderNew, position: Int) {
		val asset = items?.get(position)

		val param : RecyclerView.LayoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
		val configWidth = if(CardsBindUtils.getSecondItemVisiblePercentage() == 0)
			Constants.COLLECTION_SECOND_ITEM_VISIBLE_PERCENTAGE
		else CardsBindUtils.getSecondItemVisiblePercentage()
		param.width = ((CommonUtils.getDeviceScreenWidth())*(100 - configWidth)/100f).toInt()
		holder.itemView.layoutParams = param

		holder.perspective_source_name.text = asset?.i_source()?.displayName
			?: Constants.EMPTY_STRING
		holder.perspective_title.text = asset?.i_title() ?: Constants.EMPTY_STRING

		if(CardsBindUtils.canShowCreatorBadgeInFeed(asset?.i_source())) {
			holder.perspective_source_badge.visibility = View.VISIBLE
		} else {
			holder.perspective_source_badge.visibility = View.GONE
		}

		if(items?.size == 1) {
			holder.perspective_separator.visibility = View.GONE
		}

		holder.view.setOnClickListener { v ->
			parent?.let {
				AnalyticsHelper2.logStoryCardClickEvent(it, vm?.pageReferrer,
						items?.indexOf(asset) ?: 0, null, null, false, true, parent)

				val assetId = asset?.i_id()
				assetId?.let { assetIdParam ->
					vm?.onOpenPerspective(v, it, it.i_id(), assetIdParam, vm.section, vm.pageReferrer)
				}
			}
		}

		val timeStamp = CardsBindUtils.showTimeStampWithoutCount(item = asset)
		if (vm?.isDetail() == true && CommonUtils.isEmpty(timeStamp).not()) {
			holder.perspective_time_stamp.visibility = View.VISIBLE
			holder.perspective_time_stamp.setSpannableTextWithLangSpecificTypeFaceChanges(
					timeStamp, TextView.BufferType.SPANNABLE, asset?.i_langCode())
		} else {
			holder.perspective_time_stamp.visibility = View.GONE
		}

		/*
		* Story card view
		* */

		if (vm?.isDetail() == false && state?.collapsed == false)
			parent?.let {
				AnalyticsHelper2.logStoryCardViewEvent(it, vm?.pageReferrer, items?.indexOf
				(asset) ?: 0, Constants.EMPTY_STRING, null, null, false, true, parent)
			}
	}

	private fun getPerspectiveCardImageDimension(): Pair<Int, Int> {
		return CommonUtils.getDimension(R.dimen.img_width_perspective_rec) to
			CommonUtils.getDimension(R.dimen.img_height_perspective_rec)
	}

	private fun getPerspectiveCardSourceDimension(): Pair<Int, Int> {
		return CommonUtils.getDimension(R.dimen.perspective_source_icon_height) to
			CommonUtils.getDimension(R.dimen.perspective_source_icon_height)
	}


	fun setItems(data: List<CommonAsset>?) {
		this.items = data
		notifyDataSetChanged()
	}

	fun setParent(data: CommonAsset?) {
		this.parent = data

	}
}


class PerspectiveCarouselCardsViewHolderNew(val view: View) : RecyclerView.ViewHolder(view) {
	val perspective_separator = view.findViewById<View>(R.id.perspective_separator)
	val perspective_source_badge = view.findViewById<ImageView>(R.id.creator_badge)
	val perspective_title = view.findViewById<NHTextView>(R.id.perspective_title)
	val perspective_source_name = view.findViewById<NHTextView>(R.id.perspective_source_name)
	val perspective_time_stamp = view.findViewById<NHTextView>(R.id.timestamp)
}