/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.viewholder

import android.app.Activity
import android.graphics.Rect
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.ViewPager
import com.newshunt.adengine.BR
import com.newshunt.adengine.R
import com.newshunt.adengine.databinding.AdsPagerLayoutBinding
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.view.adapter.NativeAdsAppDownloadPagerAdapter
import com.newshunt.dataentity.common.helper.common.CommonUtils

/**
 * Allows horizontal swiping of ad. Also, should stop at each ad shown.
 *
 * @author Mukesh Yadav
 */
class SwipableAdsHolder(private val viewBinding: AdsPagerLayoutBinding,
                        private val uniqueRequestId: Int,private val parentLifecycleOwner : LifecycleOwner?) :
        AdsViewHolder(viewBinding, uniqueRequestId,parentLifecycleOwner) {
    private val viewPagerAds: ViewPager
    private var nativeAdsAppDownloadPagerAdapter: NativeAdsAppDownloadPagerAdapter? = null
    private var selectedPosition: Int = 0
    private var multipleAdEntity: MultipleAdEntity? = null
    private var viewedAds: Int = 0
    private var view = viewBinding.root

    /**
     * Since the page size is reduced, need to fire beacon for adjacent visible ads also.
     * Offset determines the number of ads for which impression is fired.
     *
     * @return
     */
    private val offsetForBeacon: Int
        get() {
            val visibleItems = 1.0 * CommonUtils.getDeviceScreenWidth() / (CommonUtils.getDimension(R.dimen.carousel_card_width) + 2 * CommonUtils.getDimension(R.dimen.carousel_card_side_margin))
            val fullyVisibleAds = visibleItems.toInt()
            return if (visibleItems > fullyVisibleAds) fullyVisibleAds + 1 else fullyVisibleAds
        }

    //beacons should be hit only when visible on screen.
    private val isAdvisibleOnscreen: Boolean
        get() {
            val rect = Rect()
            view.getLocalVisibleRect(rect)
            return rect.width() > 0 && rect.top <= 0
        }

    init {
        viewPagerAds = viewBinding.viewpagerAds
        viewPagerAds.offscreenPageLimit = 2
    }

    override fun updateView(activity: Activity, baseAdEntity: BaseAdEntity) {
        if (baseAdEntity !is MultipleAdEntity) {
            return
        }

        updateView(baseAdEntity, false)
        multipleAdEntity = baseAdEntity
        if (multipleAdEntity?.isShown == false) {
            viewedAds = 0
        }
        if (nativeAdsAppDownloadPagerAdapter == null) {
            nativeAdsAppDownloadPagerAdapter = NativeAdsAppDownloadPagerAdapter(multipleAdEntity,
                    activity, uniqueRequestId, parentLifecycleOwner)
            viewPagerAds.adapter = nativeAdsAppDownloadPagerAdapter
        } else {
            nativeAdsAppDownloadPagerAdapter?.update(multipleAdEntity)
        }

        val pageChangeListener = object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                selectedPosition = position
                hitBeacon()
            }
        }

        viewPagerAds.addOnPageChangeListener(pageChangeListener)
        pageChangeListener.onPageSelected(0)
        viewPagerAds.currentItem = 0
        viewPagerAds.pageMargin = CommonUtils.getDimension(R.dimen.carousel_card_side_margin)

        if (!CommonUtils.isEmpty(multipleAdEntity?.baseDisplayAdEntities)) {
            val firstAd = multipleAdEntity?.baseDisplayAdEntities?.get(0)
            viewBinding.setVariable(BR.adEntity, firstAd)
            viewBinding.executePendingBindings()
        }
    }

    override fun onCardView(baseAdEntity: BaseAdEntity) {
        multipleAdEntity?.let {
            super.onCardView(it)
            hitBeacon()
        }
    }

    override fun onDestroy() {
        AdsUtil.destroyAd(multipleAdEntity, uniqueRequestId)
    }

    //Loop through all visible ads and hit beacons if visible on screen
    private fun hitBeacon() {
        var size = 0
        multipleAdEntity?.baseDisplayAdEntities?.size?.let {
            size = it
        }
        if (viewedAds < size && (isAdvisibleOnscreen)) {
            var offset = 0
            val offsetLimit = offsetForBeacon
            while (offset < offsetLimit) {
                if (nativeAdsAppDownloadPagerAdapter?.onCardView(selectedPosition + offset, adapterPosition) ==
                        true) {
                    viewedAds++
                }
                offset++
            }
        }
    }

    override fun onVisible(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        multipleAdEntity?.let {
            onCardView(it)
        }
    }

    override fun onUserEnteredFragment(viewVisibilityPercentage: Int, percentageOfScreen: Float) {
        multipleAdEntity?.let {
            onCardView(it)
        }
    }
}
