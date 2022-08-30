/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.dailyhunt.tv.players.customviews

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.TransitionManager
import com.dailyhunt.tv.players.R
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.CompanionAdsConfig
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider

/**
 * Custom Container to host and handle companionAdSlot, Ad header.
 *
 * @author raunak.yadav
 */
class CompanionAdView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr) {

    private val adHeader: View
    private val heading: TextView
    private val companionSlot: FrameLayout
    private val toggleView: ImageView
    private val expandedText: String?
    private val collapsedText: String?
    private val mode: AdTheme

    private var expanded: Boolean = true
    private var isHeaderValid: Boolean = false
    var isFilled: Boolean = false

    enum class AdTheme(val mode: Int) {
        DAY(0),
        NIGHT(1);

        companion object {
            fun getAdTheme(mode: Int): AdTheme {
                values().forEach {
                    if (it.mode == mode) {
                        return it
                    }
                }
                return DAY
            }
        }
    }

    init {
        var array: TypedArray? = null
        try {
            array = context.obtainStyledAttributes(attrs, R.styleable.CompanionAdView, defStyleAttr, 0)
            mode = AdTheme.getAdTheme(array.getInteger(R.styleable.CompanionAdView_adTheme, 0))
        } finally {
            array?.recycle()
        }

        LayoutInflater.from(context).inflate(R.layout.layout_companion_slot, this)
        visibility = View.GONE

        adHeader = findViewById(R.id.ad_header)
        heading = findViewById(R.id.text_heading)
        companionSlot = findViewById(R.id.companion_slot)
        toggleView = findViewById(R.id.toggle_btn)

        setBackgroundColor(if (mode == AdTheme.NIGHT) CommonUtils.getColor(com.newshunt.dhutil.R.color
                .companion_ad_background_color_night)
        else CommonUtils.getColor(com.newshunt.dhutil.R.color.companion_ad_background_color))

        if (mode == AdTheme.NIGHT) {
            heading.setTextColor(CommonUtils.getColor(com.newshunt.dhutil.R.color.companion_ad_background_color_night))
            findViewById<TextView>(R.id.ad_icon).setTextColor(CommonUtils.getColor(com.newshunt.dhutil.R.color
                .companion_ad_background_color_night))
            findViewById<View>(R.id.top_divider).setBackgroundColor(CommonUtils.getColor(com.newshunt.dhutil.R.color
                .story_card_divider_color_night))
        }
        val config = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo?.instreamAdsConfig
        expandedText = config?.companionAdsConfig?.expandHeading
        collapsedText = config?.companionAdsConfig?.collapseHeading

        if (!(expandedText.isNullOrBlank() || collapsedText.isNullOrBlank())) {
            isHeaderValid = true
            heading.text = expandedText
            heading.setOnClickListener {
                expanded = !expanded
                if (expanded) {
                    expandAd()
                } else {
                    collapseAd()
                }
            }
        }
    }

    fun getAdContainer(): ViewGroup? {
        return companionSlot
    }

    /**
     * To decide if the ad should remain expanded/collapsed when content resumes
     * @param - if the content's aspect ratio allows the companion ad.
     */
    fun onVideoAdEnded(allowCompanionAd: Boolean) {
        if (!allowCompanionAd) {
            hideAd()
            return
        }
        val displayType = AdsUpgradeInfoProvider.getInstance()
            .adsUpgradeInfo?.instreamAdsConfig?.companionAdsConfig?.showAfterVideoAd

        displayType ?: hideAd()

        when (displayType) {
            CompanionAdsConfig.CompanionDisplayType.COLLAPSE -> collapseAd()
            CompanionAdsConfig.CompanionDisplayType.HIDE -> hideAd()
            else -> {
            }
        }
    }

    private fun collapseAd() {
        if (parent is ViewGroup) {
            TransitionManager.beginDelayedTransition(parent as ViewGroup)
        }
        if (isHeaderValid) {
            heading.text = collapsedText
            toggleView.setImageResource(if (mode == AdTheme.DAY) com.newshunt.dhutil.R.drawable.arrow_expand
            else com.newshunt.dhutil.R.drawable.arrow_expand_night)
        }
        companionSlot.visibility = View.GONE
        expanded = false
    }

    private fun expandAd() {
        if (parent is ViewGroup) {
            TransitionManager.beginDelayedTransition(parent as ViewGroup)
        }
        if (isHeaderValid) {
            heading.text = expandedText
            toggleView.setImageResource(if (mode == AdTheme.DAY) com.newshunt.dhutil.R.drawable.arrow_collapse
            else com.newshunt.dhutil.R.drawable.arrow_collapse_night)
            adHeader.visibility = View.VISIBLE
        }
        companionSlot.visibility = View.VISIBLE
        visibility = View.VISIBLE
        expanded = true
    }

    fun onCompanionSlotLoaded(companionAdLoaded: Boolean) {
        if (companionAdLoaded) {
            isFilled = true
            expandAd()
        } else {
            hideAd()
        }
    }

    fun hideAd() {
        if(parent !is ViewGroup) return
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup)
        visibility = View.GONE
        adHeader.visibility = View.GONE
        expanded = false
        isFilled = false
    }

    fun showIfFilled() {
        if (isFilled) {
            visibility = View.VISIBLE
            adHeader.visibility = View.VISIBLE
        }
    }
}