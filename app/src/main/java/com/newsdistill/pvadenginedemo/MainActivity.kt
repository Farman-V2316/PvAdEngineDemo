package com.newsdistill.pvadenginedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.newshunt.adengine.domain.controller.GetAdUsecaseController
import com.newshunt.adengine.view.helper.AdsHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println("panda: PVAd demo launched....")
    }
}