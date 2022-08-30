/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview

import android.annotation.SuppressLint
import android.text.Selection
import android.text.Spannable
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.view.View
import com.newshunt.common.view.customview.fontview.NHTextView

fun internalLinkTouchListener(spannableText: Spannable): View.OnTouchListener = object : View.OnTouchListener {
    //No click handling on text
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        event ?: return false
        val txtView = view as? NHTextView ?: return false
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= txtView.totalPaddingLeft
            y -= txtView.totalPaddingTop

            x += txtView.scrollX
            y += txtView.scrollY

            val layout = txtView.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = spannableText.getSpans(off, off, InternalUrlSpan::class.java)

            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(txtView)
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(
                            spannableText,
                            spannableText.getSpanStart(link[0]),
                            spannableText.getSpanEnd(link[0])
                    )
                }

                return true
            } else {
                Selection.removeSelection(spannableText)
            }
        }

        return false
    }

}

class InternalUrlSpan(val url: String? = null, val internalUrlHandler: ((View, String) -> Unit)? = null) : ClickableSpan() {
    override fun onClick(view: View) {
        url ?: return
        internalUrlHandler?.invoke(view, url)
    }

    // No underline required for now if need configuration pass in constructor
    override fun updateDrawState(ds: TextPaint) {
        ds.isUnderlineText = false
    }
}