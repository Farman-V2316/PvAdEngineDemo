package com.newshunt.common.view.view

import android.content.Context
import android.view.View

interface DisplayStickAudio {

    fun isAttachedToWindow() : Boolean

    fun getWindowContext() : Context

    fun getFloatingView() : View?

    fun setFloatingView(floatingView : View?)
}