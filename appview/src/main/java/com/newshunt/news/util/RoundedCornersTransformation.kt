/*
 *  Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.news.util

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

/**
 * @author amit.chaudhary
 */
class RoundedCornersTransformation constructor(private val radius: Float,
                                               private val viewWidth: Float? = null,
                                               private val viewHeight: Float? = null) :
        BitmapTransformation() {
    private val ID = "com.newshunt.news.util.RoundedCornersTransformation"

    override fun transform(bitmapPool: BitmapPool, toTransform: Bitmap, i: Int,
                           i1: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val bitmap = bitmapPool.get(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setHasAlpha(true)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        drawRoundRect(canvas, paint, width.toFloat(), height.toFloat())
        return bitmap
    }

    private fun drawRoundRect(canvas: Canvas, paint: Paint, width: Float, height: Float) {
        canvas.drawRoundRect(RectF(0f, 0f, viewWidth ?: width, viewHeight ?: height), radius,
                radius,
                paint)
    }

    override fun updateDiskCacheKey(p0: MessageDigest) {
        p0.update((ID + radius).toByteArray(Charsets.UTF_8))
    }
}
