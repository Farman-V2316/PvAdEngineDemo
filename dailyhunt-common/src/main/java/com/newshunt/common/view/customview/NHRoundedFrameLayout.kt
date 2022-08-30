package com.newshunt.common.view.customview

/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

import android.content.Context
import android.graphics.*
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import com.newshunt.common.helper.common.AndroidUtils
import com.newshunt.dhutil.R

/**
 * Frame layout which supports Rounding of corners and border both.
 */
class NHRoundedFrameLayout
/**
 * @param context      context of activity
 * @param attrs        attributes for configuration
 * @param defStyleAttr style attributes
 */
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    private val path = Path()
    private val strokePaint: Paint
    private var cornerRadius: Int = 0
    private var strokeWidth = 0
    private var rectShape: RoundRectShape? = null
    private var fillColor: Int = Color.TRANSPARENT
    private var borderColor : Int  = Color.TRANSPARENT

    init {
        strokePaint = Paint()
        strokePaint.strokeWidth = 0f
        strokePaint.color = 0
        strokePaint.isAntiAlias = true

        val a = context.obtainStyledAttributes(attrs, com.newshunt.common.util.R.styleable.RoundedCornerImageView)
        cornerRadius = a.getDimensionPixelSize(com.newshunt.common.util.R.styleable.RoundedCornerImageView_cornerRadius, 0)
        setRoundedBackground()
        a.recycle()
    }

    fun setCornerRadius(radius: Int) {
        if (cornerRadius == radius) {
            return
        }
        cornerRadius = radius
        configureCornerRounding()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        configureCornerRounding()
    }

    override fun onDraw(canvas: Canvas) {
        if (rectShape != null) {
            rectShape!!.draw(canvas, strokePaint)
        } else {
            canvas.drawPaint(strokePaint)
        }
        if (!path.isEmpty) {
            canvas.clipPath(path)
        }
        super.onDraw(canvas)
    }

    private fun setPath() {
        path.rewind()
        val width = width
        val height = height
        rectShape = RoundRectShape(
                FloatArray(8) { i -> cornerRadius.toFloat() }, RectF(
                strokeWidth.toFloat(),
                strokeWidth.toFloat(),
                strokeWidth.toFloat(),
                strokeWidth.toFloat()
        ), FloatArray(8) { i -> cornerRadius.toFloat() }
        )
        rectShape!!.resize(width.toFloat(), height.toFloat())
        val rectFPath = RectF(
                strokeWidth.toFloat(),
                strokeWidth.toFloat(),
                (width - strokeWidth).toFloat(),
                (height - strokeWidth).toFloat()
        )
        path.addRoundRect(rectFPath, cornerRadius.toFloat(), cornerRadius.toFloat(), Path.Direction.CW)
        path.close()
    }

    private fun setRoundedBackground() {
        if (cornerRadius > 0) {
            background = AndroidUtils.makeRoundedRectDrawable(
                    cornerRadius, fillColor,
                    strokeWidth, Color.TRANSPARENT
            )
            clipToOutline = true
        }
    }

    private fun configureCornerRounding() {
        setPath()
        setRoundedBackground()
        invalidate()
    }

    /**
     * @param width width of border
     * @param color color of border
     */
    fun setStroke(width: Int, color: Int) {
        strokeWidth = width
        strokePaint.strokeWidth = width.toFloat()
        strokePaint.color = color
        configureCornerRounding()
    }

    fun fillColor(fillColor: Int) {
        this.fillColor = fillColor
        setRoundedBackground()
    }

}