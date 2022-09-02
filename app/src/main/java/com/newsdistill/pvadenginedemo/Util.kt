package com.newsdistill.pvadenginedemo

import android.app.Activity
import android.util.DisplayMetrics
import com.newsdistill.pvadenginedemo.dummydata.util.DisplayUtils

fun setDisplayMetrics(activity: Activity) {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    val instance: DisplayUtils = DisplayUtils.getInstance()
    instance.setHeightPx(displayMetrics.heightPixels)
    instance.setWidthPx(displayMetrics.widthPixels)
    instance.setDensity(displayMetrics.density)
    val screenHeight = displayMetrics.heightPixels / displayMetrics.density
    val screenWidth = displayMetrics.widthPixels / displayMetrics.density
    instance.setScreenHeight(screenHeight)
    instance.setScreenWidth(screenWidth)
    instance.setVisibleScreenHeight(screenHeight - 20) //minus statusbar
    instance.setVisibleScreenHeightMinusBottomNav(screenHeight - 20 - 55)
    if (screenHeight != 0f) {
        instance.setAspectRatio(screenWidth / screenHeight)
    }
    instance.setScaleDensity(displayMetrics.scaledDensity)
}