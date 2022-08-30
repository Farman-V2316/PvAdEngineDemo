package com.newshunt.common.view.customview

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2

class CapLeadingMarginSpan2 internal constructor(private val lines: Int, private val margin: Int) :
    LeadingMarginSpan2 {
    /*Returns the value to which must be added indentation*/
    override fun getLeadingMargin(first: Boolean): Int {
        return if (first) {
            margin
        } else {
            //Offset for all other Layout layout ) { }
            /*Returns * the number of rows which should be applied *     indent returned by getLeadingMargin (true)
             * Note:* Indent only applies to N lines of the first paragraph.*/
            0
        }
    }
    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int, text: CharSequence,
        start: Int, end: Int, first: Boolean, layout: Layout
    ) {
    }

    override fun getLeadingMarginLineCount(): Int {
        return lines
    }
}