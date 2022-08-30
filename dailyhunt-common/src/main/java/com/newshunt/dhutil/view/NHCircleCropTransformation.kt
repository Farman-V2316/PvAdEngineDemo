/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

/***
 * @author amit.chaudhary
 * */
class NHCircleCropTransformation(private val borderWidth: Float,
                                 private val borderColor: Int) : BitmapTransformation() {
    private fun coverError(borderWidth: Float): Float = borderWidth * 0.1f
    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(getId().toByteArray())
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap? {
        return circleCrop(pool, toTransform)
    }

    private fun circleCrop(pool: BitmapPool, source: Bitmap?): Bitmap? {
        if (source == null) return null

        val size = Math.min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2

        val squared = Bitmap.createBitmap(source, x, y, size, size)

        var result: Bitmap? = pool.get(size, size, Bitmap.Config.ARGB_8888)
        if (result == null) {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        }

        val canvas = Canvas(result!!)
        val paint = Paint()
        paint.shader = BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        paint.isAntiAlias = true
        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)
        if (borderWidth > 0) {
            val stroke = Paint().apply {
                this.color = borderColor
                this.style = Paint.Style.STROKE
                this.isAntiAlias = true
                this.strokeWidth = borderWidth
            }
            canvas.drawCircle(r, r, r - (borderWidth / 2) + coverError(borderWidth), stroke)
        }
        return result
    }

    fun getId(): String {
        return javaClass.name
    }
}
