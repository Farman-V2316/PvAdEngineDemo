/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.newshunt.dhutil.R


/**
 * A custom NHWebView which has rounded edges.
 * <p>
 * Created by srikanth.ramaswamy on 08/03/2018
 */
open class RoundedCornersWebView : HorizontalSwipeWebView {

    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var radius: Int = 0
    private val paint: Paint = Paint()
    private val path : Path = Path()
    private val viewRect : RectF = RectF()


    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context!!.obtainStyledAttributes(attrs, R.styleable.RoundedCornerImageView)
        if (typedArray != null) {
            radius = typedArray.getDimensionPixelSize(R.styleable.RoundedCornerImageView_cornerRadius, 0)
            typedArray.recycle()
        }
    }

    init {
        paint.color = Color.TRANSPARENT
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        path.fillType = Path.FillType.INVERSE_WINDING
    }

    override fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)
        viewWidth = newWidth
        viewHeight = newHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        viewRect.set(0f, scrollY.toFloat(), viewWidth.toFloat(), scrollY.toFloat() + viewHeight)
        path.addRoundRect(viewRect, radius.toFloat(), radius.toFloat(), Path.Direction.CW)
        canvas.drawPath(path, paint)
    }
}