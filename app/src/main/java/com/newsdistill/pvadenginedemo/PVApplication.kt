package com.newsdistill.pvadenginedemo

import android.app.Application
import com.newshunt.dataentity.common.helper.common.CommonUtils

class PVApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        //PANDA: removed manually for testing
        CommonUtils.setApplication(this)
    }
}