/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.theme

import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat
import android.widget.ImageView

/**
 * Contains drawable related util functions
 *
 * author satosh.dhanyamraju
 */


/**
 * Create a copy of [imageView]'s drawable. mutates it. sets it back on the view.
 */
fun setTint(imageView: ImageView?, @ColorInt tint: Int) {
    val drawableCopy = imageView?.drawable?.constantState?.newDrawable()?.mutate() ?: return
    DrawableCompat.setTint(drawableCopy, tint)
    imageView.setImageDrawable(drawableCopy)
}