/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.view.customview.fontview

import android.content.Context
import android.util.AttributeSet

/**
 * <code>NHTextView</code> which notifies the listeners of change in the number of lines.
 *
 * Created by karthik on 12/05/18.
 */
open class LengthNotifyingTextView : NHTextView {

    var lineCountListener : LineCountListener? = null;

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        calculate()
    }

    private fun calculate() {
        val textLayout = layout
        if (textLayout != null && lineCountListener != null) {
            lineCountListener?.onLineCountAvailable(textLayout.lineCount)
        }
    }

    fun setLineCountListerner(lineCountListener : LineCountListener) {
        this.lineCountListener = lineCountListener
    }

    public interface LineCountListener {
        fun onLineCountAvailable(lineCount : Int);
    }
}