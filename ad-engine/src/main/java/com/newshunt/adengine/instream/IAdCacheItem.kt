package com.newshunt.adengine.instream

import android.app.Activity
import com.newshunt.dataentity.common.asset.CommonAsset
import com.newshunt.dataentity.common.pages.PageEntity
import java.lang.ref.WeakReference

class IAdCacheItem(val videoId: String, val activity: Activity?, val videoParams: Map<String, String>,
                   val adExtras: String?, val position: Int, val commonAsset: CommonAsset?,
                   val pageEntity: PageEntity?, val section: String?,
                   playerCacheCallbacks: IAdCachePlayerCallbacks?) {
    private var playerCacheCallbacksWeakReference: WeakReference<IAdCachePlayerCallbacks>? = null
    private var instreamAdsHelperWeakReference: WeakReference<IAdHelper>? = null

    init {
        if (playerCacheCallbacks != null)
            this.playerCacheCallbacksWeakReference = WeakReference(playerCacheCallbacks)
    }

    val playerCacheCallbacks: IAdCachePlayerCallbacks?
        get() = playerCacheCallbacksWeakReference?.get()

    var instreamAdsHelper: IAdHelper?
        get() = instreamAdsHelperWeakReference?.get()
        set(instreamAdsHelper) {
            this.instreamAdsHelperWeakReference = WeakReference<IAdHelper>(instreamAdsHelper)
        }

    var requestDone: Boolean = false
}
