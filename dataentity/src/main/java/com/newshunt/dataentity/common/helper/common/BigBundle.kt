/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */

package com.newshunt.dataentity.common.helper.common

import android.util.Log
import com.newshunt.dataentity.BuildConfig

/**
 * - To be used for storing heavy objects that are shared between activities.
 * - For heavy objects, use this instead of passing in the bundle as serializables or json-strings,
 * to avoid [android.os.TransactionTooLargeException]
 * - Meant for storing non-primitive types, so it just uses [Any] & not concrete-types
 * or generics (there are other classes of issues with generics). Caller should do type-casting after [get]
 *
* @author satosh.dhanyamraju
*/
class BigBundle {
    private val map = hashMapOf<Long, Any>()
    /**
     * @return unique key that is to be used with [get]
     */
    fun put(value: Any?) : Long {
        value?:run {
            log("put: null")
            return -1L
        }
        val key = System.nanoTime()
        val v = map.put(key, value)
        log("put(#${value.hashCode()}): ($key, #${v.hashCode()})")
        return key
    }

    /**
     * @return value for given key. null if not found.
     */
    fun get(key: Long?, remove: Boolean = true): Any? {
        key ?: return null
        val any = if (remove) map.remove(key) else map.get(key)
        log("get($key, $remove): #${any.hashCode()}")
        return any
    }

    private fun log(message: String) {
        // Not using [Logger] because this module does not depend on 'common' gradle module
        if(BuildConfig.DEBUG) Log.d("BigBundle", "$message; mapSize=${map.size}")
    }

    companion object {
        @JvmField
        internal val BIG_BUNDLE = BigBundle()
    }
}