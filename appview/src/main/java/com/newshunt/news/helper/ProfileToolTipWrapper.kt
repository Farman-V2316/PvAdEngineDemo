/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.appview.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Tooltip popup window to be displayed
 *
 * @author helly.patel
 */

class ProfileToolTipWrapper(private val context: Context, private val resId: Int) {

    private val popupWindow: PopupWindow = PopupWindow(context)
    private val contentView: View = LayoutInflater.from(context).inflate(resId, null)
    private lateinit var toolTipAutoHideDisposable: Disposable

    fun showProfileTooltip(title: String, message: String?, displayTime: Int, anchor: View,
                           anchorHighLighter: ImageView?) {

        val titleTv = contentView.findViewById<NHTextView>(R.id.title)
        titleTv.text = title
        message?.let {
            val messageTv = contentView.findViewById<NHTextView>(R.id.message)
            messageTv.text = it
        }


        popupWindow.contentView = contentView
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        popupWindow.setOnDismissListener {
            this.cancelToolTipAutoHideTimer(anchorHighLighter)
        }

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
                    var position_x = anchor_rect.right - (contentViewWidth) + CommonUtils.getDimension(R.dimen
                            .tooltip_padding)
                    var position_y = anchor_rect.bottom - anchor.getHeight() / 2 + CommonUtils.getDimension(R.dimen
                            .tooltip_padding)

                    if (resId == R.layout.view_profile_tool_tip) {
                        position_x = anchor_rect.left - CommonUtils.getDimension(R.dimen.tooltip_padding_top_urdu)
                        position_y = anchor_rect.bottom - anchor.getHeight() / 2 + CommonUtils.getDimension(R.dimen
                                .tooltip_padding)
                    } else if (resId == R.layout.view_edit_profile_tooltip_urdu) {
                        position_x = anchor_rect.left - CommonUtils.getDimension(R.dimen.tooltip_padding_top_urdu)
                        position_y = anchor_rect.bottom - anchor.getHeight() / 2 + CommonUtils.getDimension(R.dimen
                                .tooltip_padding)
                    } else if (resId == R.layout.group_settings_tooltip) {
                        position_x = anchor_rect.right - (contentViewWidth) + CommonUtils.getDimension(R.dimen.group_tooltip_padding)
                        position_y = anchor_rect.bottom - anchor.height / 2 + CommonUtils.getDimension(R.dimen.tooltip_padding)
                    }

                    popupWindow.showAtLocation(anchor,
                            Gravity.NO_GRAVITY,
                            position_x, position_y)


                    anchorHighLighter?.visibility = View.VISIBLE
                }
            }
        }

        scheduleHideToolTipTimer(displayTime.toLong())
    }

    private fun cancelToolTipAutoHideTimer(anchorHighLighter: ImageView?) {
        if (!::toolTipAutoHideDisposable.isInitialized || toolTipAutoHideDisposable.isDisposed()) {
            return
        }
        toolTipAutoHideDisposable.dispose()
        anchorHighLighter?.visibility = View.GONE
    }

    private fun scheduleHideToolTipTimer(delay: Long) {
        if (delay <= 0) {
            return
        }

        if (::toolTipAutoHideDisposable.isInitialized && !toolTipAutoHideDisposable.isDisposed) {
            return
        }

        toolTipAutoHideDisposable = Observable.just(1)
                .delay(delay, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { integer -> hideToolTip() }
    }

    fun hideToolTip() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss()
        }
    }
}