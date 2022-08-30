/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.view.View
import android.widget.ImageView
import com.newshunt.dhutil.helper.theme.ThemeUtils
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil

/**
 * @author santhosh.kc
 */
class HomeSearchBarView @JvmOverloads constructor(
    lifeCycleOwner: LifecycleOwner,
    private val logo: ImageView?,
    private val searchView: NHTextView?,
    private val iconPosition: Int,
    private val isSearchSection: Boolean = false
) {

    constructor(lifecycleOwner: LifecycleOwner, logo: ImageView?,
                searchView: NHTextView?) : this(lifecycleOwner, logo, searchView, ICON_POSITION_LEFT)

    init {
        AppSettingsProvider.enableSearchBarObserver.observe(lifeCycleOwner,
                Observer<Boolean> { t -> showSearchBar(t ?: true) })
    }

    private fun showSearchBar(show: Boolean) {
        if (show && isSearchSection) {
            logo?.visibility = View.GONE
            searchView?.visibility = View.VISIBLE
            if (UserPreferenceUtil.getUserNavigationLanguage() == Constants.URDU_LANGUAGE_CODE) {
                searchView?.setCompoundDrawablesWithIntrinsicBounds(ThemeUtils.getThemeDrawableByAttribute(searchView.context,R.attr.search_icon, R.drawable.search_icon), 0,
                        0, 0)
            } else {
                if (iconPosition == ICON_POSITION_RIGHT) {
                    searchView?.setCompoundDrawablesWithIntrinsicBounds(0, 0, ThemeUtils.getThemeDrawableByAttribute(searchView.context,R.attr.search_icon, R.drawable.search_icon), 0)
                } else {
                    searchView?.setCompoundDrawablesWithIntrinsicBounds(ThemeUtils.getThemeDrawableByAttribute(searchView.context,R.attr.search_icon, R.drawable.search_icon), 0, 0, 0)
                }
            }
        } else {
            logo?.visibility = View.VISIBLE
            searchView?.visibility = View.GONE
        }
    }

    companion object {
        const val ICON_POSITION_LEFT = 1
        const val ICON_POSITION_RIGHT = 2
    }
}