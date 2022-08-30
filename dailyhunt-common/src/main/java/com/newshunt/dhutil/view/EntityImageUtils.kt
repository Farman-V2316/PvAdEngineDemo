/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.newshunt.app.helper.LetterToColorMapping
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.view.customview.AvatarDrawable
import com.newshunt.sdk.network.image.Image

/**
 * @author anshul.jain
 * A utiltity for loading either images or first character of entity title with circular
 * backgrounds.
 */
class EntityImageUtils {
    companion object {

        @JvmStatic
        fun loadImage(imageUrl: String? = null, titleEnglish: String? = null, imageView:
        ImageView?) {
            loadImage(imageUrl, titleEnglish, imageView, null, false)
        }

        @JvmStatic
        fun loadImage(imageUrl: String? = null, titleEnglish: String? = null, imageView:
        ImageView?, placeholder: Int? = null, isCircular: Boolean = false) {
            imageView ?: return
            if (imageUrl.isNullOrEmpty()) {
                handleError(titleEnglish, imageView, placeholder, isCircular)
                return
            }
            val requestOptions = if (isCircular) {
                RequestOptions().circleCrop()
            } else {
                RequestOptions()
            }
            val loader = Image.load(imageUrl).apply(requestOptions)
            if (placeholder != null) {
                loader.placeHolder(placeholder)
            }

            loader.apply(requestOptions).into(object : SimpleTarget<BitmapDrawable>() {
                override fun onResourceReady(bitmapDrawable: BitmapDrawable, p1: Transition<in BitmapDrawable>?) {
                    imageView.setImageDrawable(bitmapDrawable)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    handleError(titleEnglish, imageView, placeholder, isCircular)
                }
            })
        }

        @JvmStatic
        fun loadTextImage(titleEnglish: String? = null, imageView: ImageView?) {
            imageView ?: return
            imageView.setImageDrawable(AvatarDrawable("#B1DF14", titleEnglish))
        }

        private fun handleError(titleEnglish: String?, imageView: ImageView, placeholder: Int?, isCircular: Boolean = true) {
            imageView.setImageDrawable(null)
            if (titleEnglish != null) {
                val firstChar = if (!CommonUtils.isEmpty(titleEnglish)) titleEnglish!!.substring(0, 1)
                    .toUpperCase() else Constants.EMPTY_STRING
                val characterAndBackgroundColor = LetterToColorMapping.getCharacterAndColor(firstChar)
                imageView.setImageDrawable(AvatarDrawable(characterAndBackgroundColor.second, characterAndBackgroundColor.first, isCircular = isCircular))
            } else if (placeholder != null){
                imageView.setImageResource(placeholder)
            }
        }
    }
}