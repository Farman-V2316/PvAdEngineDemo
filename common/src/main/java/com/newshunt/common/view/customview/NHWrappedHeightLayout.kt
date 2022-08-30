/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.common.view.customview

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * RelativeLayout with a maxHeight limit.
 *
 * @author raunak.yadav
 */
class NHWrappedHeightLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {

    var maxHeight: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        if (maxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}