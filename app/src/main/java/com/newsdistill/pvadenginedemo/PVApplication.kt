package com.newsdistill.pvadenginedemo

import android.app.Application
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.sdk.network.NetworkSDK

class PVApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        NetworkSDK.init(this, null, false)
        //PANDA: removed manually for testing
        CommonUtils.setApplication(this)


        //initialize fonts to fix issue while rendering ad
        FontHelper.initializeFont(CommonUtils.getApplication(), "en")
    }
}