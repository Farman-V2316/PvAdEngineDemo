/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.view.customview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.constraintlayout.widget.ConstraintLayout
import com.newshunt.dhutil.R

/**
 * This constraintlayout draws isometric depth UI on its own canvas.
 * If the width given to container is 100dp and depth is 4dp, then effective width available is
 * 96dp for content.
 * Shadow is customised such that it does not overlap the isometric UI.
 *
 * @author raunak.yadav
 */
class IsometricView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr) {

    // defines paint and canvas
    private var drawPaint: Paint? = null
    private var eraser: Paint? = null

    /**
     * View Width minus depth
     */
    private var effectiveWidth = 0

    /**
     * View Height minus depth
     */
    private var effectiveHeight = 0
    private var depth: Int = 0
    private var depthColor: Int? = Color.TRANSPARENT
    private var orientation: IsoMetricDepthOrientation = IsoMetricDepthOrientation.BOTTOM_RIGHT

    private fun points(): List<Pair<Int, Int>> {
        return when (orientation) {
            IsoMetricDepthOrientation.TOP_RIGHT -> listOf(Pair(0, depth),
                Pair(effectiveWidth, depth),
                Pair(effectiveWidth, effectiveHeight + depth),
                Pair(effectiveWidth + depth, effectiveHeight),
                Pair(effectiveWidth + depth, 0),
                Pair(depth, 0))
            else -> listOf(Pair(0, effectiveHeight),
                Pair(depth, effectiveHeight + depth),
                Pair(effectiveWidth + depth, effectiveHeight + depth),
                Pair(effectiveWidth + depth, depth),
                Pair(effectiveWidth, 0),
                Pair(effectiveWidth, effectiveHeight))
        }
    }

    /**
     * Corner triangle to be cut out of canvas.
     */
    private fun triangle1(): List<Pair<Int, Int>> {
        return when (orientation) {
            IsoMetricDepthOrientation.TOP_RIGHT -> listOf(
                Pair(0, 0),
                Pair(0, depth),
                Pair(depth, 0))
            else -> listOf(
                Pair(0, effectiveHeight),
                Pair(0, effectiveHeight + depth),
                Pair(depth, effectiveHeight + depth))
        }
    }

    /**
     * Corner triangle to be cut out of canvas.
     */
    private fun triangle2(): List<Pair<Int, Int>> {
        return when (orientation) {
            IsoMetricDepthOrientation.TOP_RIGHT -> listOf(
                Pair(effectiveWidth, effectiveHeight + depth),
                Pair(effectiveWidth + depth, effectiveHeight + depth),
                Pair(effectiveWidth + depth, effectiveHeight))
            else -> listOf(
                Pair(effectiveWidth, 0),
                Pair(effectiveWidth + depth, 0),
                Pair(effectiveWidth + depth, depth))
        }
    }

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        var array: TypedArray? = null
        try {
            array = context.obtainStyledAttributes(attrs,
                R.styleable.IsometricView, defStyleAttr, 0)
            depthColor = array.getInteger(R.styleable.IsometricView_depth_color, Color.TRANSPARENT)
            depth = array.getDimensionPixelSize(R.styleable.IsometricView_depth, 0)
            orientation = IsoMetricDepthOrientation.getDepthOrientation(array.getInteger(R.styleable
                .IsometricView_depth_orientation, 1))
        } finally {
            array?.recycle()
        }
        when (orientation) {
            IsoMetricDepthOrientation.TOP_RIGHT -> setPaddingRelative(paddingStart,
                paddingTop + depth, paddingEnd + depth, paddingBottom)
            else -> setPaddingRelative(paddingStart, paddingTop,
                paddingEnd + depth, paddingBottom + depth)
        }
        setupPaint()
    }

    private fun setupPaint() {
        drawPaint = Paint().apply {
            color = depthColor ?: Color.TRANSPARENT
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        //To cut out the triangles at the end.
        eraser = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        drawPaint?.let { paint ->
            canvas?.drawPath(getPath(points()), paint)
        }
        eraser?.let {
            canvas?.drawPath(getPath(triangle2()), it)
            canvas?.drawPath(getPath(triangle1()), it)
        }
        canvas?.save()
        super.onDraw(canvas)
        canvas?.restore()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //coordinates of actual area used by content.
        val rect = when (orientation) {
            IsoMetricDepthOrientation.TOP_RIGHT ->
                Rect(0, depth, w - depth, h)
            else ->
                Rect(0, 0, w - depth, h - depth)
        }
        outlineProvider = IsometricOutlineProvider(rect)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        effectiveWidth = width - depth
        effectiveHeight = height - depth
    }

    companion object {

        fun getPath(points: List<Pair<Int, Int>>): Path {
            val path = Path()
            path.moveTo(points[0].first.toFloat(), points[0].second.toFloat())
            for (i in 1 until points.size) {
                path.lineTo(points[i].first.toFloat(), points[i].second.toFloat())
            }
            path.close()
            return path
        }
    }

}

/**
 * Provides custom outline that will be used for shadow.
 */
class IsometricOutlineProvider(private val rect: Rect) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRect(rect)
    }
}

enum class IsoMetricDepthOrientation(val mode: Int) {
    TOP_RIGHT(0),
    BOTTOM_RIGHT(1);

    companion object {
        fun getDepthOrientation(mode: Int): IsoMetricDepthOrientation {
            values().forEach {
                if (it.mode == mode) {
                    return it
                }
            }
            return BOTTOM_RIGHT
        }
    }
}
