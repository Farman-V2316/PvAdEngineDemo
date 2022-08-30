/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */

package com.newshunt.common.helper.common

import com.google.gson.reflect.TypeToken
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.preference.SavedPreference

/**
* Contains functions to convert-store-read userSeg properties from multiple APIs
* @author satosh.dhanyamraju
*/
private const val PREF_USER_SEG = "pref_user_seg"
private val mapType = object : TypeToken<HashMap<String, String>>() {}.type
private const val LOG_TAG: String = "UserSegHelper"
/**
 * Read json string pref, convert to map and return
 */
fun readUserSegFromPref() : Map<String, String> {
    val pref: String? = PreferenceManager.getString(PREF_USER_SEG, "")
    val map = if (!pref.isNullOrEmpty()) {
        runCatching {JsonUtils.fromJson<HashMap<String, String>>(pref, mapType) }
                .getOrNull()
    } else null

    val map1: Map<String, String> = map ?: emptyMap()
    Logger.d(LOG_TAG, "readUserSegFromPref: $map1")
    return map1
}

/**
 * Reads existing pref, adds/overwrites entries with [props], saves it back to pref
 */
fun storeUserSegToPref(props: Map<String, String>?) {
    if (props.isNullOrEmpty()) {
        Logger.w(LOG_TAG, "storeUserSegToPref: empty props")
        return
    }
    val stored = readUserSegFromPref().toMutableMap()
    stored.putAll(props)
    PreferenceManager.saveString(PREF_USER_SEG, JsonUtils.toJson(stored))
    Logger.d(LOG_TAG, "storeUserSegToPref: saved $props")
}

fun storeMapToPref(props: Map<String, String>?, path: SavedPreference){
    if(props.isNullOrEmpty()){
        Logger.w(LOG_TAG, "storeMapToPref: empty props")
        return
    }
    val stored = readStoredMapFromPref(path).toMutableMap()
    stored.putAll(props)
    PreferenceManager.savePreference(path, JsonUtils.toJson(stored))
}

/**
 * Read json string pref, convert to map and return
 */
fun readStoredMapFromPref(path: SavedPreference) : Map<String, String> {
    val pref: String? = PreferenceManager.getPreference(path, "")
    val map = if (!pref.isNullOrEmpty()) {
        runCatching {JsonUtils.fromJson<HashMap<String, String>>(pref, mapType) }
                .getOrNull()
    } else null

    val map1: Map<String, String> = map ?: emptyMap()
    Logger.d(LOG_TAG, "readUserSegFromPref: $map1")
    return map1
}
