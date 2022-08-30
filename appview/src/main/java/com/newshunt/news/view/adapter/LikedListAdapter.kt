/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.newshunt.appview.R
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.view.customview.NHImageView
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.asset.LikeAsset
import com.newshunt.dataentity.common.asset.LikeListPojo
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.social.entity.LikeType
import com.newshunt.dhutil.view.EntityImageUtils
import com.newshunt.dhutil.view.LikeEmojiUtils
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.news.viewmodel.DetailsViewModel
import com.newshunt.sdk.network.image.Image
import com.newshunt.viral.utils.visibility_utils.VisibilityAwareViewHolder

class LikedListAdapter(val dvm: DetailsViewModel?, val likeListPojo: LikeListPojo?) : RecyclerView.Adapter<LikedListVH>() {

	private var items: List<LikeAsset>? = null
	private var extraCount: Int? = null
	private lateinit var parentItem: CommonAsset

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedListVH {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.liked_item, parent, false)
		return LikedListVH(view, dvm)
	}

	override fun getItemCount(): Int {
		return (items?.size ?: 0) + (if ((extraCount ?: 0) > 0) 1 else 0)
	}

	override fun onBindViewHolder(holder: LikedListVH, position: Int) {
		if (position == (getItemCount() - 1) && ((extraCount ?: 0) > 0)) {
			EntityImageUtils.loadTextImage("+${DataUtil.easyReadableString((extraCount ?: 0).toLong())}",
					holder.profileimage)
			holder.view.setOnClickListener { v ->
				dvm?.onLikeViewClick(v, parentItem, likeListPojo)
			}

			holder.iconview.setImageDrawable(null)
			return
		}

		val resId = LikeEmojiUtils.getCircularEmojiResourceId(LikeType.fromName(items?.get(position)?.action)
			?: LikeType.LIKE, false)
		holder.iconview.setImageResource(resId)
		val size = CommonUtils.getDimension(R.dimen.profile_circle_x)
		Image.load(ImageUrlReplacer.getQualifiedImageUrl(items?.get(position)?.actionableEntity?.entityImageUrl
			?: "", size, size))
			.placeHolder(R.color.empty_image_color)
			.into(holder.profileimage)

		holder.id = items?.get(position)?.actionableEntity?.handle
		holder.view.setOnClickListener { v ->
			dvm?.onProfileViewClick(v, null, items?.get(position)?.actionableEntity?.handle)
		}
	}

	fun setItems(data: List<LikeAsset>?) {
		this.items = data
		notifyDataSetChanged()
	}

	fun setExtraCount(extraCount: Int?) {
		this.extraCount = extraCount
	}

	fun setParentItem(item: CommonAsset) {
		this.parentItem = item
	}
}

class LikedListVH(val view: View, val dvm: DetailsViewModel?) : RecyclerView.ViewHolder(view), VisibilityAwareViewHolder {
	val iconview = view.findViewById<NHImageView>(R.id.like_icon)
	val profileimage = view.findViewById<NHImageView>(R.id.profile_image)
	var id: String? = null

	override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
		dvm?.onLikeVisible(id)
	}

	override fun onInVisible() {
		// Do Nothing
	}

	override fun onUserLeftFragment() {
		// Do Nothing
	}

	override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
		// Do Nothing
	}
}