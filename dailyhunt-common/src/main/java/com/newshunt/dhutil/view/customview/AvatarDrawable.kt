/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view.customview

import android.graphics.*
import android.graphics.drawable.Drawable
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.font.FontWeight
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.dhutil.R

/**
 * @author anshul.jain
 * A custom drawable to show a circle with a background and some text over it
 */
class AvatarDrawable(val backgroundColor: String? = null, val text: String? = null,
                     val textSize: Int = CommonUtils.getDimension(R.dimen.circular_avatar_text_default_size),
                     val textColor: Int = Color.WHITE,
                     val isCircular: Boolean = true): Drawable() {


    override fun draw(canvas: Canvas) {
        val rect = Rect(0, 0, bounds.width(), bounds.height())
        val paint = Paint()
        try {
            if (CommonUtils.isEmpty(backgroundColor)) {
                paint.color = CommonUtils.getColor(com.newshunt.common.util.R.color.empty_image_color)
            } else {
                paint.color = Color.parseColor(backgroundColor)
            }
        } catch (e: Exception) {
            // do nothing
        }

        if (isCircular) {
            canvas.drawCircle(rect.exactCenterX(), rect.exactCenterY(), rect.exactCenterX(), paint)
        } else {
            canvas.drawColor(paint.color)
        }
        if (!CommonUtils.isEmpty(backgroundColor))
            applyText(canvas, rect)
    }

    private fun applyText(canvas: Canvas, rect: Rect) {
        text ?: return
        val textPaint = Paint()
        textPaint.color = textColor
        textPaint.textSize = textSize.toFloat()
        textPaint.typeface = FontHelper.getTypeFaceFor(ClientInfoHelper.getClientInfo().appLanguage, FontWeight.BOLD.weightEnumValue)

        val textWidth = textPaint.measureText(text) * 0.5f
        val textBaseLineHeight = textPaint.fontMetrics.ascent * -0.4f
        canvas.drawText(text, rect.exactCenterX() - textWidth, rect.exactCenterY() + textBaseLineHeight, textPaint)
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity() = PixelFormat.UNKNOWN


    override fun setColorFilter(colorFilter: ColorFilter?) {
    }
}