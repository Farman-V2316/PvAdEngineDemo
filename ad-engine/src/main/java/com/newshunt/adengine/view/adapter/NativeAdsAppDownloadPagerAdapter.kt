/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.PagerAdapter
import com.newshunt.adengine.R
import com.newshunt.adengine.client.AsyncAdImpressionReporter
import com.newshunt.adengine.databinding.NewsItemTypeNativeAdBinding
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.util.AdConstants
import com.newshunt.adengine.util.OMSdkHelper.recordImpression
import com.newshunt.adengine.view.viewholder.AppDownloadAdPagerViewHolder
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Adapter to show each ad in a separate viewholder for 'appdownload' carousel.
 * @author raunak.yadav
 */
class NativeAdsAppDownloadPagerAdapter(private var multipleAdEntity: MultipleAdEntity?,
                                       private val parentActivity: Activity,
                                       private val uiComponentId: Int,
                                       private val lifecycleOwner: LifecycleOwner?) :
    PagerAdapter() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(parentActivity)
    private val renderedPositions = HashSet<Int>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val vb: NewsItemTypeNativeAdBinding = DataBindingUtil.inflate(layoutInflater,
            R.layout.news_item_type_native_ad, container, false)
        val view = vb.root
        container.addView(view)
        renderedPositions.add(position)

        val vp = vb.borderContainer.layoutParams as ConstraintLayout.LayoutParams
        if (position == 0) {
            vp.marginStart = CommonUtils.getDimension(com.dailyhunt.tv.ima.R.dimen.carousel_ad_margin)
            vb.borderContainer.layoutParams = vp
        } else if (position == count - 1) {
            vp.marginEnd = CommonUtils.getDimension(com.dailyhunt.tv.ima.R.dimen.carousel_ad_margin)
            vb.borderContainer.layoutParams = vp
        }

        multipleAdEntity?.baseDisplayAdEntities?.get(0)?.let { ad ->
            val width = CommonUtils.getDimension(R.dimen.carousel_card_width)
            val lp = vb.bannerImage.layoutParams
            val adHeight = ad.height ?: AdConstants.DEFAULT_AD_SIZE
            val adWidth = ad.width ?: AdConstants.DEFAULT_AD_SIZE
            if (adHeight != 0 && adWidth != 0) {
                val ratio = adHeight.toFloat() / adWidth
                val height = (width * ratio).toInt()
                lp.height = height
            } else {
                lp.height = CommonUtils.getDimension(R.dimen.carousel_card_image_height)
            }
            lp.width = width
            vb.bannerImage.layoutParams = lp
            val tv = vb.bannerTitle
            val maxLines = ad.content?.itemTitle?.maxLines?: AdConstants.AD_BANNER_TITLE_DEFAULT_MAXLINES
            tv.maxLines = maxLines
            // TODO : match exact height with UI spec
            if (maxLines == 0) {
                tv.visibility = View.GONE
            } else {
                tv.layoutParams.height = maxLines * CommonUtils.getDimension(R.dimen.carousel_card_title_height_1_lines)
            }
        }
        val viewHolder = AppDownloadAdPagerViewHolder(vb, uiComponentId, lifecycleOwner)
        multipleAdEntity?.baseDisplayAdEntities?.get(position)?.let { adEntity ->
            viewHolder.updateView(parentActivity, adEntity)
        }
        return view
    }

    fun onCardView(position: Int, adPositionIndex: Int): Boolean {
        val baseDisplayAdEntity = getAppDownloadAdEntity(position) ?: return false
        return if (renderedPositions.contains(position) && !baseDisplayAdEntity.isShown) {
            baseDisplayAdEntity.isShown = true
            recordImpression(baseDisplayAdEntity, uiComponentId)
            val asyncAdImpressionReporter = AsyncAdImpressionReporter(baseDisplayAdEntity)
            baseDisplayAdEntity.notifyObservers()
            asyncAdImpressionReporter.onCardView(adPositionIndex = adPositionIndex)
            true
        } else false
    }

    private fun getAppDownloadAdEntity(position: Int): BaseDisplayAdEntity? {
        return multipleAdEntity?.baseDisplayAdEntities?.let { ads ->
            if (position >= 0 && position < ads.size) {
                ads[position]
            } else null
        }
    }

    override fun getItemPosition(item: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return multipleAdEntity?.baseDisplayAdEntities?.size ?: 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    fun update(multipleAdEntity: MultipleAdEntity?) {
        this.multipleAdEntity = multipleAdEntity
        renderedPositions.clear()
        notifyDataSetChanged()
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getPageWidth(position: Int): Float {
        return if (position == 0 || position == count - 1) {
            1.0f * CommonUtils.getDimension(R.dimen.carousel_card_width) / (CommonUtils.getDeviceScreenWidth() - CommonUtils.getDimension(
                        com.dailyhunt.tv.ima.R.dimen.ads_spaces))
        } else 1.0f * CommonUtils.getDimension(R.dimen.carousel_card_width) / CommonUtils.getDeviceScreenWidth()
    }
}