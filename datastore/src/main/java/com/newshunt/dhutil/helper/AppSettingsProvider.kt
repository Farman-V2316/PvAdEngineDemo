/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import com.newshunt.common.helper.common.*
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.util.R.dimen
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.SettingsChangeEvent
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.onboarding.model.entity.datacollection.InstalledAppInfo

/**
 * Provider class of all App Settings instead of reading preferences again and again.
 *
 *
 * @author santhosh.kc
 */
object AppSettingsProvider {

    private const val TAG = "AppSettingsProvider"

    val userLanguagesData = MutableLiveData<String>()

    private val notificationLiveData = MutableLiveData<Boolean>()

    val enableSearchBarObserver = MutableLiveData<Boolean>().apply {
        postValue(PreferenceManager.getPreference(GenericAppStatePreference.ENABLE_SEARCHBAR, true))
    }

    val settingsChangedLiveData by lazy {
        MutableLiveData<SettingsChangeEvent>()
    }

    val preferredSharableAppLiveData by lazy {
        MutableLiveData<InstalledAppInfo>()
    }

    init {
        Logger.d(TAG,"Initing AppSettings Provider")
        Logger.d(TAG,"Refreshing Search Bar Settings")
        refreshSearchBarSettings()
        Logger.d(TAG,"Refreshing user languages data in AppSettingsProvider - init")
        refreshUserLanguagesData()
        getSharableApp()
    }

    fun getSharableApp() {
        val packageName = PreferenceManager.getPreference(AppStatePreference.SELECTED_APP_TO_SHARE,Constants.EMPTY_STRING)
        if(!packageName.isNullOrEmpty() && AndroidUtils.isAppInstalled(packageName)) {
            try {
                val packageManager = CommonUtils.getApplication().packageManager
                val label: String = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
                val icon: Drawable = CommonUtils.getApplication().packageManager.getApplicationIcon(packageName)
                val bitmap = CommonUtils.getBitmapFromDrawable(icon)
                val iconDrawable: Drawable = BitmapDrawable(CommonUtils.getApplication().resources,
                    bitmap.let {
                        Bitmap.createScaledBitmap(it, CommonUtils.getDimension(dimen.share_drawable_size),
                            CommonUtils.getDimension(
                            dimen.share_drawable_size), true)
                    })
                preferredSharableAppLiveData.postValue(InstalledAppInfo(packageName, iconDrawable, label))
            } catch (e: PackageManager.NameNotFoundException) {
                Logger.caughtException(e)
            }
        }
    }

    fun refreshSearchBarSettings() {
        val isSearchBar = PreferenceManager.getPreference(GenericAppStatePreference
                .ENABLE_SEARCHBAR, true)
        Logger.d(TAG, "Refresh Search Bar Settings : $isSearchBar")
        if (isSearchBar != enableSearchBarObserver.value) {
            enableSearchBarObserver.postValue(isSearchBar)
        }
    }

    fun refreshUserLanguagesData() {
        val userLanguages = UserPreferenceUtil.getUserNavigationLanguage()
        if (!CommonUtils.isEmpty(userLanguages)){
            userLanguagesData.postValue(userLanguages)
        }
    }

}