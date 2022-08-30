package com.newshunt.common.view.customview

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

open class NHViewPager : androidx.viewpager.widget.ViewPager, DisableInterceptViewGroup {

    protected var interceptDisabled = false

    constructor(context: Context) : super(context)

    constructor(context: Context,  attrs : AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try { // crash fix
            !interceptDisabled && super.onInterceptTouchEvent(ev)
        } catch (exception: IllegalArgumentException) {
            false
        }
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        interceptDisabled = false
    }

    override fun disableIntercept(disable: Boolean) {
        interceptDisabled = disable
    }

}