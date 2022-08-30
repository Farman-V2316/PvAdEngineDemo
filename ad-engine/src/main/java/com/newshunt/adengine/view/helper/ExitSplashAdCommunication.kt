/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.view.helper

import androidx.lifecycle.MutableLiveData
import com.newshunt.adengine.util.AdLogger

/**
 * @author raunak.yadav
 */
object ExitSplashAdCommunication {
    var exitAdRequested: Boolean = false
    val exitSplashAdRequestLD by lazy {
        MutableLiveData<ExitSplashAdRequestInfo>()
    }

    fun requestExitSplash(from: String) {
        if (exitAdRequested) {
            AdLogger.v("ExitSplashAdCommunication", "Exit splash already requested in this session")
            return
        }
        AdLogger.d("ExitSplashAdHelper", "Exit splash request from $from")
        exitSplashAdRequestLD.postValue(ExitSplashAdRequestInfo(from))
    }
}

data class ExitSplashAdRequestInfo(val postedFrom: String?)
