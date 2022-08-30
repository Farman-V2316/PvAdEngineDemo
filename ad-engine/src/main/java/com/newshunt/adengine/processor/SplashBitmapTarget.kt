/*
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.processor

import android.graphics.Bitmap
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.newshunt.adengine.model.AdReadyHandler
import com.newshunt.adengine.model.entity.PgiArticleAd
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.adengine.util.SplashAdPersistenceHelper
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.io.FileOutputStream

/**
 * For downloading splash image, saving a trimmed png version of bitmap.
 * @author srikanth.r on 06/03/2021
 */
// splash image will be as big as the screen; so specify those dimensions
class SplashBitmapTarget(private val defaultSplashAd: PgiArticleAd,
                         private val adReadyHandler: AdReadyHandler) : SimpleTarget<Bitmap?>(CommonUtils.getDeviceScreenWidth(), CommonUtils.getDeviceScreenHeight()) {

    override fun onResourceReady(bitmap: Bitmap, p1: Transition<in Bitmap?>?) {
        CommonUtils.runInBackground {
            try {
                AdsUtil.getPersistedSplashFile()?.let { splashFile ->
                    val fileOutputStream = FileOutputStream(splashFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                    fileOutputStream.close()
                    // Save after successful download
                    val url = defaultSplashAd.content?.itemImage?.data
                    PreferenceManager.savePreference(GenericAppStatePreference.SPLASH_IMAGE_URL, url)
                    SplashAdPersistenceHelper.updateDefaultSplashTimeOut(defaultSplashAd.span)
                }
                adReadyHandler.onReady(defaultSplashAd)
            } catch (e: Exception) {
                adReadyHandler.onReady(null)
                Logger.caughtException(e)
            }
        }
    }
}