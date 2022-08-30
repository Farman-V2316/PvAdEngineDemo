/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.helper.preference.SavedPreference

private const val LOG_TAG = "LiveSharedPreference"

object LiveSharedPreference {
    fun <T> pref(prefKey: SavedPreference, context: Context, default: T):
            MutableLiveData<T> {
        val keyName = prefKey.name
        val mediatorLiveData = MediatorLiveData<T>()
        mediatorLiveData.value = PreferenceManager.getPreference(prefKey, default)
        mediatorLiveData.addSource(prefLiveData) { keyChanged ->
            if (keyChanged == keyName) {
                mediatorLiveData.value = PreferenceManager.getPreference(prefKey, default)
            }
        }
        val preferences = context.getSharedPreferences(prefKey.preferenceType.fileName, Context.MODE_PRIVATE)
        preferences.registerOnSharedPreferenceChangeListener(listener)
        return mediatorLiveData
    }

    private val prefLiveData = MutableLiveData<String>()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key: String ->
        prefLiveData.postValue(key)
    }
}
