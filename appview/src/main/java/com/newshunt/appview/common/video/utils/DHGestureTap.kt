/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.utils

import android.view.GestureDetector
import com.newshunt.appview.common.video.listeners.DHTouchListener
import com.newshunt.common.helper.common.Logger

/**
 * Created on 06/01/2020.
 */
class DHGestureTap(private val touchListener: DHTouchListener) : GestureDetector.SimpleOnGestureListener() {
    override fun onDoubleTap(e: android.view.MotionEvent): Boolean {
        Logger.d("onDoubleTap :", "" + e.action)
        return true
    }

    override fun onSingleTapConfirmed(e: android.view.MotionEvent): Boolean {
        Logger.d("onSingleTap :", "" + e.action)
        touchListener.onSingleTap()
        return true
    }
}