package com.newsdistill.pvadenginedemo

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.newsdistill.pvadenginedemo.ads.initAd
import com.newsdistill.pvadenginedemo.ads.insertAd
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.common.helper.common.BusProvider
import com.squareup.otto.Subscribe

class MainActivity : AppCompatActivity() {

    private lateinit var  adContainer: RelativeLayout
    private  var uiBus = BusProvider.getUIBusInstance()
    private val uniqueRequestId = 111

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adContainer = findViewById(R.id.ad_container)

        initAd(uiBus, uniqueRequestId)
    }

    override fun onStart() {
        super.onStart()
        uiBus.register(this)
    }

    override fun onStop() {
        super.onStop()
        uiBus.unregister(this)
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        println("panda: setAdResponse-------------------> $nativeAdContainer")

        if (nativeAdContainer.baseAdEntities == null ||
            nativeAdContainer.uniqueRequestId != uniqueRequestId
        ) {
            return
        }
        insertAd(this, nativeAdContainer, adContainer)
    }
}