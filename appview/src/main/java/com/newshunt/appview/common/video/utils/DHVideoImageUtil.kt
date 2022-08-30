/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.video.utils

import android.content.Context
import com.dailyhunt.tv.helper.TVConstants
import com.newshunt.dataentity.common.asset.VideoAsset
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.news.model.entity.server.asset.ContentScale

/**
 * Created by umesh.isran on 10/9/2019.
 */
object DHVideoImageUtil {
    fun getScaledParamsForData(item: VideoAsset): ContentScale {
        var scale: ContentScale? = null
        if (item.height <= 0 || item.width <= 0) {
            //Default fix to 9 : 16 ratio
            val width = CommonUtils.getDeviceScreenWidth()
            val height = width * 9 / 16
            scale = getScale(CommonUtils.getApplication(), width, height)
        }

        if (item.inExpandMode) {
//            scale = item.dataExpandScale
        }

        if (scale == null) {
            scale = getScale(CommonUtils.getApplication(), item.width,
                    item.height)
        }
        return scale
    }

    private fun getScale(context: Context, contentWidth: Int, contentHeight: Int): ContentScale {

        val scale = ContentScale()
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        var tempWidth = contentWidth
        var tempHeight = contentHeight
        val finalWidth: Int
        val finalHeight: Int
        val maxContentHeight = metrics.heightPixels - CommonUtils.getPixelFromDP(
                TVConstants.TV_STATUS_BAR_HEIGHT * 2, context)

        if (tempWidth >= tempHeight) {
            tempWidth = screenWidth
            tempHeight = screenWidth * contentHeight / contentWidth

            if (tempHeight <= maxContentHeight) {
                finalWidth = tempWidth
                finalHeight = tempHeight
            } else {
                finalHeight = maxContentHeight
                finalWidth = finalHeight * contentWidth / contentHeight
            }
        } else {
            tempWidth = screenWidth
            tempHeight = screenWidth * contentHeight / contentWidth

            if (tempHeight > maxContentHeight) {
                tempHeight = maxContentHeight
                tempWidth = tempHeight * contentWidth / contentHeight
            }
            finalHeight = tempHeight
            finalWidth = tempWidth
        }

        scale.width = finalWidth
        scale.height = finalHeight
        return scale
    }


}
