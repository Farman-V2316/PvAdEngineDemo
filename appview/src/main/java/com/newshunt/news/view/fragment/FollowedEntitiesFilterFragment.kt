/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.appview.R
import com.newshunt.dataentity.common.pages.FollowFilter
import com.newshunt.news.helper.NewsListCardLayoutUtil

/**
 * @author anshul.jain
 * A fragment showing the follow related filters.
 */
class FollowedEntitiesFilterFragment(private val followFilterCallback: FollowingFilterCallback,
                                     private val filterList: List<FollowFilter>,
                                     private val selectedFilter: String?) :
        BottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet_follow, container, false)
        NewsListCardLayoutUtil.manageLayoutDirection(view)
        filterList.forEachIndexed { index, followFilterType ->
            val radionButtonLayout = LayoutInflater.from(context).inflate(com.newshunt.dhutil.R
                    .layout
                    .options_item, view as ViewGroup, false)
            val imageView = radionButtonLayout.findViewById<ImageView>(R.id.options_icon)
            val textView = radionButtonLayout.findViewById<NHTextView>(R.id.options_textView)
            val heading = view.findViewById<NHTextView>(R.id.heading)
            heading.text = CommonUtils.getString(R.string.notification_filter_heading)
            radionButtonLayout.tag = followFilterType
            view.addView(radionButtonLayout)

            textView.text = followFilterType.displayText
            val drawableIcon = if (ThemeUtils.isNightMode())
                R.drawable.filter_option_icon_notification_inbox_night
            else
                R.drawable.filter_option_icon_notification_inbox
            imageView.setImageDrawable(CommonUtils.getDrawable(drawableIcon))
            radionButtonLayout.setOnClickListener(this)
            imageView.isSelected = CommonUtils.equals(followFilterType.value, selectedFilter)

            if (selectedFilter == followFilterType.value) {
                radionButtonLayout.isSelected = true
            }
        }

        return view
    }

    override fun onClick(v: View?) {
        val filterType = v?.tag as? FollowFilter ?: return
        followFilterCallback.onFollowingFilterSelected(filterType)
        dismiss()
    }
}

