package com.newsdistill.pvadenginedemo

import android.app.Application
import android.net.ConnectivityManager
import com.newshunt.adengine.AdEngineGateway
import com.newshunt.adengine.handshake.helper.AdIdHelperTask
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.sdk.network.NetworkSDK
import com.newshunt.sdk.network.connection.DefaultNetworkCallback
import com.newshunt.sdk.network.image.Image
import com.newshunt.sdk.network.internal.NetworkSDKLogger
import io.reactivex.plugins.RxJavaPlugins

class PVApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        tempFix()
        initNetworkSDK()
        Image.getGlide()
        //PANDA: removed manually for testing
        CommonUtils.setApplication(this)

//        NewsBaseUrlContainer.statusEvents.observeForever(BaseUrlInitErrorObserver.initExceptionObserver)
//        AnalyticsClient.statusEvents.observeForever(BaseUrlInitErrorObserver.analyticsDoneObserver)
        NewsBaseUrlContainer.init()


        //initialize fonts to fix issue while rendering ad
        FontHelper.initializeFont(CommonUtils.getApplication(), "en")

        adInit()
    }

    private fun adInit() {
        AdIdHelperTask.init()


        AdEngineGateway.initialize()
        CommonUtils.runInBackground { AdEngineGateway.lazyInitialize() }
    }

    // fix rxjava2 crash
    private fun tempFix() {
        RxJavaPlugins.setErrorHandler {
            it?.printStackTrace()
        }
    }

    private fun initNetworkSDK() {
        // Init network sdk
        try {
            val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.registerDefaultNetworkCallback(DefaultNetworkCallback.getInstance())
        } catch (exception: Exception) {
            Logger.caughtException(exception)
        }
        NetworkSDK.init(
            this, null,
            AppConfig.getInstance().isGoBuild
        )
        NetworkSDKLogger.setLogger(Logger.getLogger())
        NetworkSDK.setLogEnabled(Logger.loggerEnabled())
    }
}