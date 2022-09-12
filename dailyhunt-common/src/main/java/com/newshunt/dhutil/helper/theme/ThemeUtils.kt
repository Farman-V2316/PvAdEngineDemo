/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.theme

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.isDarkThemeOn

/**
 * Helper class to provide values stored in theme/style.
 *
 * @author srikanth.r on 11/22/2021
 */
private const val LOG_TAG = "ThemeUtils"
object ThemeUtils {

    /**
     * Function to return default background color stored in applied theme
     *
     * @param context
     * @return
     */
    @JvmStatic
    fun getBackgroundColor(context: Context?): Int {
        val contextLocal = context ?: CommonUtils.getApplication()
        val currentTheme = contextLocal.theme
        val storedValueInTheme = TypedValue()
        return if (currentTheme.resolveAttribute(R.attr.source_tab_background, storedValueInTheme, true)) {
            storedValueInTheme.data
        } else {
            Color.WHITE
        }
    }

    @JvmStatic
    val preferredTheme: ThemeType
        get() {
            if (canApplySystemTheme() && isAppFollowingSystemTheme()) {
                return if (CommonUtils.getApplication().isDarkThemeOn()) ThemeType.NIGHT else ThemeType.DAY
            }

            return readUserPreferredTheme()
        }

    @JvmStatic
    fun isNightMode(): Boolean = (preferredTheme == ThemeType.NIGHT)

    @JvmStatic
    fun getThemeColorByAttribute(context: Context?, attributeName: Int): Int {
        val contextLocal = context ?: CommonUtils.getApplication()
        val currentTheme = contextLocal.theme
        val storedValueInTheme = TypedValue()
        return if (currentTheme.resolveAttribute(attributeName, storedValueInTheme, true)) {
            storedValueInTheme.data
        } else {
            Color.WHITE
        }
    }

    @JvmStatic
    fun getThemeDrawableByAttribute(context: Context?, attributeName: Int, defaultValue: Int): Int {
        val contextLocal = context ?: CommonUtils.getApplication()
        val currentTheme = contextLocal.theme
        val storedValueInTheme = TypedValue()
        return if (currentTheme.resolveAttribute(attributeName, storedValueInTheme, true)) {
            storedValueInTheme.resourceId
        } else {
            defaultValue
        }
    }

    fun getThemeDataByAttribute(context: Context?, attributeName: Int, defaultValue: Int): Int {
        val value: Int
        val contextLocal = context ?: CommonUtils.getApplication()
        val currentTheme = contextLocal.theme
        val storedValueInTheme = TypedValue()
        value = if (currentTheme.resolveAttribute(attributeName, storedValueInTheme, true)) {
            storedValueInTheme.data
        } else {
            defaultValue
        }
        return value
    }

    /**
     * This method determines whether or not this device supports dark theme. Only Android 10 and
     * above support dark theme
     */
    fun canApplySystemTheme(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * This method tells whether the app's theme follows the system theme.
     */
    fun isAppFollowingSystemTheme(): Boolean {
        return PreferenceManager.getPreference(AppStatePreference.THEME_APPLY_SYSTEM, true)
    }

    fun readUserPreferredTheme(): ThemeType {
        val appliedTheme = PreferenceManager.getPreference(AppStatePreference.THEME_APPLIED_NEW, "")
        return if (DataUtil.isEmpty(appliedTheme) || !appliedTheme.equals(ThemeType.NIGHT.getName(), ignoreCase = true)) {
            ThemeType.DAY
        } else ThemeType.NIGHT
    }

    private fun getLegacyTheme(): ThemeType {
        val appliedTheme = PreferenceManager.getPreference(AppStatePreference.APPLIED_THEME, "")
        return if (DataUtil.isEmpty(appliedTheme) || !appliedTheme.equals(ThemeType.NIGHT.getName(), ignoreCase = true)) {
            ThemeType.DAY
        } else ThemeType.NIGHT
    }

}

data class ThemeState(val applySystemSetting: Boolean = true,
                      val themeType: ThemeType = ThemeType.DAY)
