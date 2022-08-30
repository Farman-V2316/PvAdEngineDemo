/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview

import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.newshunt.common.util.R
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.appsection.HighlightParams

/**
 * Created by karthik.r on 14/11/18.
 */
class RippleBottomBGView : View, ValueAnimator.AnimatorUpdateListener {

    private var animator: ValueAnimator
    private var radius = 0f
    private var fillPaint: Paint = Paint()
    private var animationPaint: Paint = Paint()
    private var strokePaint: Paint = Paint()
    private var strokeWidth = CommonUtils.getDimension(R.dimen.bottom_bar_ripple_bg_stroke_width)
    private var sizeValue: Float = 1f
    private var rect: RectF = RectF(0f, 0f, 0f, 0f)

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context,
            attributeSet, defStyleAttr)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attributeSet, defStyleAttr, defStyleRes)

    init {
        fillPaint.isAntiAlias = true
        fillPaint.color = Color.DKGRAY
        fillPaint.style = Paint.Style.FILL

        animationPaint.isAntiAlias = true
        animationPaint.color = Color.GRAY
        animationPaint.style = Paint.Style.FILL

        strokePaint.isAntiAlias = true
        strokePaint.color = Color.LTGRAY
        strokePaint.strokeWidth = strokeWidth.toFloat()
        strokePaint.style = Paint.Style.STROKE

        animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 1000
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = 10
        animator.addUpdateListener(this)
    }

    fun setStrokeColor(color : Int) {
        strokePaint.color = color
    }

    fun setBGColor(color : Int) {
        fillPaint.color = color
    }

    fun setAnimationColor(color : Int) {
        animationPaint.color = color
    }

    fun setAnimationDuration(rippleDuration: Long) {
        animator.duration = rippleDuration
    }

    fun startAnimation(highlightParams: HighlightParams) {
        animator.repeatCount = highlightParams.rippleCount
        animator.start()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        radius = Math.min(height.toFloat() * 1.125f / 2, width.toFloat()/2)
        val animationRadius = radius * sizeValue
        animationPaint.alpha = (255 * (1.0f - sizeValue)).toInt()
        val centerX = width.toFloat() / 2
        val centerY = radius + (height.toFloat() * 1.125f  - 2 * radius)/2
        rect.set(centerX - radius + (strokeWidth / 2),
                centerY - radius + strokeWidth,
                centerX + radius - (strokeWidth / 2),
                centerY + radius)
        canvas?.drawOval(rect, fillPaint)
        canvas?.drawOval(rect, strokePaint)
        rect.set(centerX - animationRadius + (strokeWidth / 2),
                centerY - animationRadius + strokeWidth,
                centerX + animationRadius - (strokeWidth / 2),
                centerY + animationRadius)
        canvas?.drawOval(rect, animationPaint)
    }

    override fun onDetachedFromWindow() {
        animator.end()
        super.onDetachedFromWindow()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        sizeValue = animation?.animatedValue as Float
        invalidate()
    }
}