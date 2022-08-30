/*
 * Copyright (c) 2020 . All rights reserved.
 */

package com.newshunt.news.helper.handler

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.newshunt.appview.R
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper2
import java.util.concurrent.TimeUnit

/**
 * Created by helly.patel on 10/6/20.
 */
class NudgeTooltipWrapper {
    fun showFollowTooltip(context: Context, resId: Int, title: String, displayTime: Long, anchor: View, isFromTvDetail: Boolean = false) {

        val popupWindow: PopupWindow = PopupWindow(context)
        val contentView: View = LayoutInflater.from(context).inflate(resId, null)
        val eventType = "follow_nudge"

        val titleTv = contentView.findViewById<NHTextView>(R.id.title)
        titleTv.text = title

        popupWindow.contentView = contentView
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        anchor.post {

            (context as? Activity)?.let {

                if (!it.isFinishing) {

                    var screen_pos: IntArray = IntArray(2)
                    anchor.getLocationOnScreen(screen_pos)

                    val anchor_rect = Rect(screen_pos[0], screen_pos[1], screen_pos[0]
                            + anchor.getWidth(), screen_pos[1] + anchor.getHeight());

                    contentView.measure(WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT)

                    val contentViewWidth = contentView.getMeasuredWidth()
                    var position_x = anchor_rect.right - (contentViewWidth) - CommonUtils.getDimension(R.dimen.square_option_icon_small_size)
                    var position_y = anchor_rect.bottom + CommonUtils.getDimension(R.dimen.discussion_scroll_margin)

                    if (resId == R.layout.nudge_tooltip_follow_middle_arrow) {
                        position_x = anchor_rect.right - (contentViewWidth) / 2 - anchor_rect.width() / 2
                    }
                    if(isFromTvDetail) {
                        position_x = anchor_rect.right - (contentViewWidth) + CommonUtils.getDimension(R.dimen.topbar_icons_margin)
                    }
                    popupWindow.showAtLocation(anchor,
                            Gravity.NO_GRAVITY,
                            position_x, position_y)
                    contentView.setOnClickListener {
                        anchor.performClick()
                        popupWindow.dismiss()
                    }
                }
            }
        }

        AndroidUtils.getMainThreadHandler().postDelayed({ popupWindow.dismiss() }, TimeUnit.SECONDS.toMillis(displayTime))
        AnalyticsHelper2.logFeatureNudgeEvent(eventType)
    }
}