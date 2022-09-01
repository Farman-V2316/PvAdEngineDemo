package com.newsdistill.pvadenginedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.adengine.view.helper.AdsHelper
import com.newshunt.common.helper.common.BusProvider
import io.reactivex.plugins.RxJavaPlugins

class MainActivity : AppCompatActivity() {
    val uiBus = BusProvider.getUIBusInstance()
    val uniqueRequestId = 11
    val usecase = GetAdUsecaseController(uiBus, uniqueRequestId)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("panda: PVAd demo launched....")
    }

    override fun onStart() {
        super.onStart()
        val adRequest = AdRequest(AdPosition.MASTHEAD, 1)
        usecase.requestAds(adRequest)

        tempFix()
    }

    // fix rxjava2 crash
    private fun tempFix() {
        RxJavaPlugins.setErrorHandler {
            it?.printStackTrace()
        }
    }
}