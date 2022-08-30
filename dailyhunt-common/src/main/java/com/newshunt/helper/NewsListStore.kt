/*
 * Created by Rahul Ravindran at 26/9/19 7:07 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.helper

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.newshunt.common.helper.common.BusProvider
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.model.entity.DoubleBackExitEvent
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.pref.NewsPreference
import com.squareup.otto.Subscribe
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap

private val LOG_TAG = "NewsListStore"

data class RequesterMapVal<T>(val uniqueId: Int,
                         val observable: Observable<T>,
                         val disposable: Disposable) : Disposable by disposable

/**
 * This class, when used as singleton, allows temporary, in-memory storage of observables.
 * Intended for storing network responses, so that when view recreates, network-requests can
 * go-on and when view re-attaches, the (cached) observable can replay the response.
 *
 * @author satosh.dhanyamraju
 */
class NewsListStore<T> {
    private val store = ConcurrentHashMap<Any, RequesterMapVal<T>>()
    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val key = msg?.obj ?: return
            val mapVal = store[key]?:return
            if (mapVal.uniqueId == msg.what) {
                doAndLog("processing ${msg.what}, $key. Removed") {
                    store.remove(key)?.dispose()
                }
            }
        }
    }

    private val _expiry // max time for which map keeps this entry
    get() = PreferenceManager.getPreference<Int>(NewsPreference.NO_AUTO_REFRESH_ONTAB_RECREATE_SEC
            , Constants.DEFAULT_NO_AUTO_REFRESH_TAB_RECREATE) * 1000L


    init {
        // will be used as singleton. so no need to unregister.
        BusProvider.getUIBusInstance().register(this)
    }

    /**
     * If key is present in the map, returns it corresponding observable.
     * Else, turns observable returned by f() into Replayable(Connectable) observable and
     * subscribes to it immedately.
     *
     * The (un-)subscription of returned observable will not impact the network operation, because
     * this function also subscribes to it. The returned observable, when subscribed, will replay
     * all the items.
     */
    fun request(
            key: Any,
            f: () -> Observable<T>,
            uniqueId: Int,
            invalidateEntry: Boolean = false, // to be used in cases such as user pull-to-refresh
            shouldKeepInMap: Boolean = true // to be false for nextpage
    ): Observable<T> {

        if (invalidateEntry) {
            doAndLog("removed $uniqueId, presentinmap=${store.containsKey(key)}, $key due to invalidation.") {
                store.remove(key)?.dispose()
            }
        }

        val mapVal = store[key]?.copy(uniqueId) ?: run {
            val connectible = f().replay().autoConnect()
            Logger.d(LOG_TAG, "Requesting network.uniqueId=$uniqueId, key= $key")
            val disposable = connectible.subscribe(
                    {},
                    {
                        if (it.message != Constants.NOT_FOUND_IN_CACHE) {
                            doAndLog("Error ${it.message}.removed $key") {
                                store.remove(key)?.dispose()
                            }
                        }
                    }
            )
            RequesterMapVal(uniqueId, connectible, disposable)
        }
        if (shouldKeepInMap) {
            doAndLog("added newId= $uniqueId, key=$key") {
                store[key] = mapVal
            }
        }
        return mapVal.observable
    }

    /**
     * To be called when the requester(fragment) is destroyed. After the expiry time, its
     * observables will be removed from the map and subscription is disposed.
     * @param uniqueId is the fragment uniqueId
     *
     */
    fun removeLater(uniqueId: Int,
                    expiry: Long = _expiry) {
        store.filter {
            it.value.uniqueId == uniqueId
        }.forEach {
            val msg = Message.obtain(handler, it.value.uniqueId, it.key)
            handler.sendMessageDelayed(msg, expiry)
            Logger.d(LOG_TAG, "sent message ${it.value.uniqueId}, expiry=$expiry, key=${it.key}")
        }
    }

    /**
     * Will remove all items and dispose the subscriptions.
     * To be called when in cases like double-back-exit.
     */
    fun clear() {
        doAndLog("cleared map") {
            store.forEach {
                it.value.dispose()
            }
            store.clear()
        }
    }

    @Subscribe
    fun onAppExit(appExitEvent: DoubleBackExitEvent) = clear()

    private inline fun <T> doAndLog(msg: String, f: () -> T) =
            f().also {
                Logger.d(LOG_TAG, "$msg, size = ${store.size}")
            }

    /**
     * for testing
     */
    fun allItems() = store.entries
}

data class FirstPageFromStoreConfig(val invalidateEntry: Boolean, val shouldKeepInMap: Boolean)

fun getFirstPageFromStoreConfig(pageType: PageType?) : FirstPageFromStoreConfig? {
    return when(pageType) {
        PageType.PROFILE_SAVED, PageType.PROFILE_SAVED_DETAIL, PageType.PROFILE_ACTIVITY,
        PageType.PROFILE_MY_POSTS, PageType.PROFILE_TPV_POSTS, PageType.PROFILE_TPV_RESPONSES ->
            FirstPageFromStoreConfig(invalidateEntry = true, shouldKeepInMap = false)
        else -> null
    }
}
