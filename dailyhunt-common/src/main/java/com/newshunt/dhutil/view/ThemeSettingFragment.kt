/*
 * Copyright (c) 2022 NewsHunt. All rights reserved.
 */
package com.newshunt.dhutil.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.R
import com.newshunt.dhutil.databinding.FragmentThemeSettingBinding

/**
 * Created by kajal.kumari on 1/06/22.
 */

class ThemeSettingFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewBinding: FragmentThemeSettingBinding =  DataBindingUtil.inflate(inflater, R.layout.fragment_theme_setting, container, false)
        val deeplink = arguments?.getString(Constants.POST_DEEPLINK_FOR_THEME)
        AndroidUtils.getMainThreadHandler().postDelayed({
            activity?.let {
                CommonNavigator.launchDeeplink(it,deeplink, PageReferrer(NhGenericReferrer.THEME_CHANGE))
                it.finish()
                it.overridePendingTransition(R.anim.slow_fade_in,R.anim.slow_fade_out)
            }
        },Constants.THEME_SCREEN_DISPLAY_DURATION)
        return viewBinding.root
    }
}