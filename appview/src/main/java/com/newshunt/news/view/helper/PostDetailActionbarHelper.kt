/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *  
 */

package com.newshunt.news.view.helper

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.fragment.NewsDetailFragment2
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.theme.ThemeUtils

/**
* Existing action bar implementation
* @author satosh.dhanyamraju
*/
class PostDetailActionbarHelper: PostDetailActionbarInterface {
    lateinit var toolbar: Toolbar

    override fun initActionBar(parent: View, parentFragment: Fragment?, activity: Activity?, menuClickListener: Toolbar.OnMenuItemClickListener, inflater: LayoutInflater) {
        toolbar = parent.findViewById<Toolbar>(R.id.actionbar)

        val toolbarBackButtonContainer: RelativeLayout = toolbar.findViewById(R.id
                .actionbar_back_button_layout)
        toolbarBackButtonContainer.setOnClickListener {
            if (parentFragment is NewsDetailFragment2) {
                (parentFragment as? NewsDetailFragment2)?.handleActionBarBackPress(false)
            }
            activity?.onBackPressed()
        }

        toolbar.inflateMenu(R.menu.menu_post_detail)
        toolbar.setOnMenuItemClickListener(menuClickListener)
    }

    override fun isOnTheTop() = true

    override fun setActionMoreVisible(b: Boolean) {
        toolbar.menu.findItem(R.id.action_more_newsdetail).isVisible = b
    }

    override fun setActionDisclaimerVisible(b: Boolean) {
        toolbar.menu.findItem(R.id.action_disclaimer_newsdetail).isVisible = b
    }

    override fun hideActionMoreView() {
        toolbar.findViewById<View>(R.id.action_more_newsdetail)?.visibility = View.GONE
    }

    override fun updateToolbarVisibility(v: Int) {
        toolbar.visibility = v
    }


    override fun hideWithAnimation() {
        toolbar.animate().translationY((-toolbar.height).toFloat()).interpolator =
                AccelerateInterpolator(2f)
    }

    override fun showWithAnimation() {
        toolbar.animate().translationY(0f).interpolator = DecelerateInterpolator(2f)
    }

    override fun setTransparentToolbar( alpha:Float) {
        toolbar.setBackgroundColor(CommonUtils.getColor(R.color.transparent))

    }

    override fun setToolbar() {
        if (ThemeUtils.isNightMode()) {
            toolbar.setBackgroundColor(CommonUtils.getColor(R.color.theme_night_background))
        } else {
            toolbar.background = CommonUtils.getDrawable(R.drawable.location_card_shadow_background)
        }
    }
}