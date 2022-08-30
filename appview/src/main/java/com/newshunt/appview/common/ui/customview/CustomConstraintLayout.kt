/*
 * Copyright (c) 2012 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.customview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * Custom Constraint layout to restrict touch event to parent
 *
 * Created  on 23/1/2020.
 */
class CustomConstraintLayout : ConstraintLayout {

    var isIntercept = false
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
            attrs, defStyleAttr) {
        init()
    }

    private fun init() {

    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if(isIntercept) {
            onTouchEvent(ev)
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }
}