/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.customview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.theme.ThemeUtils

/**
 * Bottom Bar Items for Menu
 *
 * Created by karthik.r on 09/06/20.
 */
class NHMenuViewItem(context: Context, val nightModeNotSupported: Boolean) : LinearLayout(context) {

    private var highlightIcon: View?
    private var tvNotificationBadge: NHTextView
    private var buttonBackground: View?
    private var ivIcon: ImageView
    private var view: View = LayoutInflater.from(getContext()).inflate(com.newshunt.common.util.R.layout.view_items_navigation_bar, this, true)
    private val tintColor = if (isNightMode()) com.newshunt.common.util.R.color.navbar_icon_color_night_unselected else com.newshunt.common.util.R.color.black

    init {
        ivIcon = view.findViewById(com.newshunt.common.util.R.id.navbar_appsection_icon)
        tvNotificationBadge = view.findViewById(com.newshunt.common.util.R.id.navbar_notification_count_tv)
        highlightIcon = findViewById(com.newshunt.common.util.R.id.navbar_highlight)
        buttonBackground = findViewById(com.newshunt.common.util.R.id.navbar_item_container)
        ivIcon.setImageDrawable(CommonUtils.getTintedDrawable(com.newshunt.common.util.R.drawable.vector_more_tab, tintColor))
    }

    fun isNightMode(): Boolean {
        return !nightModeNotSupported && ThemeUtils.isNightMode()
    }

    fun setSelectedColor(isSelected: Boolean) {
        if (isSelected) {
            ivIcon.setImageDrawable(CommonUtils.getTintedDrawable(com.newshunt.common.util.R.drawable.vector_more_selected, tintColor))
        }
        else {
            ivIcon.imageTintList = null
        }
    }
}