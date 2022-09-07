package com.newsdistill.pvadenginedemo.dummydata.viewholders

import android.app.Activity
import android.view.View
import android.widget.RelativeLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.newsdistill.pvadenginedemo.R
import com.newsdistill.pvadenginedemo.ads.ShortsAdHandler
import com.newshunt.adengine.model.entity.NativeAdContainer
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.common.helper.common.BusProvider
import com.squareup.otto.Subscribe

class AdViewHolder(itemView: View, viewLifecycleOwner : LifecycleOwner) : RecyclerView.ViewHolder(itemView) {
    private var adRequestID = -1
    private val bus = BusProvider.getUIBusInstance()
    private lateinit var activity: Activity

    private val adHandler: ShortsAdHandler by lazy {
        ShortsAdHandler(viewLifecycleOwner)
    }

    fun bind(context: Activity, position: Int, adRequestID: Int) {
        println("panda: binding ad at-> $position")
        this.activity = context
        this.adRequestID = adRequestID
        bus.register(this)
        val zoneAdType:String = when (position){
            1,5,9 -> "PGI_IMAGE"
            3,7-> "PGI_HTML"
            else -> {""}
        }
        adHandler.initAd(AdPosition.PGI, adRequestID, bus, zoneAdType)
    }

    @Subscribe
    fun setAdResponse(nativeAdContainer: NativeAdContainer) {
        println("panda: setAdResponse-------------------> $nativeAdContainer")
        if (nativeAdContainer.uniqueRequestId != adRequestID)
            return

        val adContainer: RelativeLayout = itemView.findViewById(R.id.short_ad_container)
        adHandler.insertAd(activity, nativeAdContainer, adContainer)
    }
}
