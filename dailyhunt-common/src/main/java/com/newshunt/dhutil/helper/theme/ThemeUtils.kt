/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.theme

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import com.newshunt.common.helper.common.Constants
import com.newshunt.dhutil.R
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.font.FontHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.GenericCustomSnackBar
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.analytics.AnalyticsHelper
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.isDarkThemeOn

/**
 * Helper class to provide values stored in theme/style.
 *
 * @author srikanth.r on 11/22/2021
 */
private const val LOG_TAG = "ThemeUtils"
object ThemeUtils {
    private val themeMappingForUpgrades by lazy {
        mapOf(ThemeType.DAY.getName() to ThemeState(true, ThemeType.DAY),
            ThemeType.NIGHT.getName() to ThemeState(false, ThemeType.NIGHT))
    }

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

    /**
     * Function to return default text color stored in theme/style.
     *
     * @param context
     * @return
     */
    fun getTextColor(context: Context): Int {
        val currentTheme = context.theme
        val storedValueInTheme = TypedValue()
        return if (currentTheme.resolveAttribute(R.attr.default_text_color, storedValueInTheme, true)) {
            storedValueInTheme.data
        } else {
            Color.BLACK
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
    fun toggleAppliedtheme(context: Context?): ThemeType {
        var lastKnownTheme = PreferenceManager.getPreference(AppStatePreference.THEME_APPLIED_NEW, ThemeType.DAY.getName())
        PreferenceManager.savePreference(AppStatePreference.PREV_THEME_APPLIED,lastKnownTheme)
        var appliedTheme = preferredTheme
        if(canApplySystemTheme() && isAppFollowingSystemTheme()) {
            PreferenceManager.savePreference(AppStatePreference.THEME_APPLIED_NEW, preferredTheme.getName())
        } else {
            appliedTheme = if (appliedTheme == ThemeType.DAY) {
                ThemeType.NIGHT
            } else {
                ThemeType.DAY
            }
            PreferenceManager.savePreference(AppStatePreference.THEME_APPLIED_NEW, appliedTheme.getName())
        }
        Logger.e(LOG_TAG,"theme applied new"+PreferenceManager.getPreference(AppStatePreference.THEME_APPLIED_NEW,""))
        return appliedTheme
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
     * This method is called on upgrade to migrate from older theme preference to a newer theme preference
     * The logic here is if user had explicitly changed to dark mode, we let him have dark mode irrespective of system setting
     */
    fun setupThemeOnUpgrade() {
        if (!PreferenceManager.containsPreference(AppStatePreference.APPLIED_THEME)) {
            //Theme migration already handled, return!
            return
        }
        themeMappingForUpgrades[getLegacyTheme().getName()]?.let { state ->
            PreferenceManager.savePreference(AppStatePreference.THEME_APPLY_SYSTEM, state.applySystemSetting)
            PreferenceManager.savePreference(AppStatePreference.THEME_APPLIED_NEW, state.themeType.getName())
        } ?: run {
            PreferenceManager.savePreference(AppStatePreference.THEME_APPLY_SYSTEM, true)
            PreferenceManager.savePreference(AppStatePreference.THEME_APPLIED_NEW, ThemeType.DAY.getName())
        }
        kotlin.runCatching {
            PreferenceManager.remove(AppStatePreference.APPLIED_THEME)
        }
    }

    /**
     * This method determines whether or not this device supports dark theme. Only Android 10 and
     * above support dark theme
     */
    fun canApplySystemTheme(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * This method sets up the default theme for the app on first launch
     */
    fun setupThemeForFreshInstall() {
        ThemeState().apply {
            PreferenceManager.savePreference(AppStatePreference.THEME_APPLY_SYSTEM, this.applySystemSetting)
            PreferenceManager.savePreference(AppStatePreference.THEME_APPLIED_NEW, Constants.EMPTY_STRING)
        }
    }

    /**
     * This method tells whether the app's theme follows the system theme.
     */
    fun isAppFollowingSystemTheme(): Boolean {
        return PreferenceManager.getPreference(AppStatePreference.THEME_APPLY_SYSTEM, true)
    }

    /**
     * This method detects mismatch in theme and switches the theme appropriately
     * @return true if there was a theme change. false otherwise
     */
    fun checkThemeMismatch(): Boolean {
        if (canApplySystemTheme() && isAppFollowingSystemTheme()) {
            var lastKnownTheme = PreferenceManager.getPreference(AppStatePreference.THEME_APPLIED_NEW, ThemeType.DAY.getName())
            var doNotShowSb = false
            if(lastKnownTheme.isEmpty()) {
                lastKnownTheme = ThemeType.DAY.getName()
                PreferenceManager.savePreference(AppStatePreference.THEME_APPLIED_NEW,ThemeType.DAY.getName())
                doNotShowSb = true
            }
            //Check if last known theme is different from new preferred theme. If so, we need to detect mismatch
            if (preferredTheme.getName() != lastKnownTheme) {
                Logger.e(LOG_TAG, "Detected a mismatch between app and device theme, switch!!")
                if(!doNotShowSb) {
                    setThemePreferences(true, true, false, false)
                }
                toggleAppliedtheme(CommonUtils.getApplication())
                PreferenceManager.savePreference(AppStatePreference.THEME_APPLY_SYSTEM,true)
                return true
            }
        }
        return false
    }


    fun setThemePreferences(sbInList:Boolean, sbInDetail: Boolean, toastInList: Boolean, toastInDetail: Boolean) {
        PreferenceManager.savePreference(AppStatePreference.THEME_SWITCHED_SNACKBAR_LIST_NEEDED, sbInList)
        PreferenceManager.savePreference(AppStatePreference.THEME_SWITCHED_SNACKBAR_DETAIL_NEEDED, sbInDetail)
        PreferenceManager.savePreference(AppStatePreference.THEME_SWITCH_TOAST_LIST_NEEDED, toastInList)
        PreferenceManager.savePreference(AppStatePreference.THEME_SWITCH_TOAST_DETAIL_NEEDED,toastInDetail)
    }

    /**
     * Helper method to check if theme switch snackbar to be shown in the home list
     */
    fun themeAutoSwitchSnackbarNeededInList(): Boolean {
        return PreferenceManager.getPreference(AppStatePreference.THEME_SWITCHED_SNACKBAR_LIST_NEEDED, false)
    }

    /**
     * Helper method to check if theme switch snackbar to be shown in the detail page
     */
    fun themeAutoSwitchSnackbarNeededInDetail(): Boolean {
        return PreferenceManager.getPreference(AppStatePreference.THEME_SWITCHED_SNACKBAR_DETAIL_NEEDED, false)
    }

    /**
     * Helper method to check if theme switch toast to be shown in the home list
     */
    fun themeAutoSwitchToastNeededInList(): Boolean {
        return PreferenceManager.getPreference(AppStatePreference.THEME_SWITCH_TOAST_LIST_NEEDED, false)
    }

    /**
     * Helper method to check if theme switch toast to be shown in the detail page
     */
    fun themeAutoSwitchToastNeededInDetail(): Boolean {
        return PreferenceManager.getPreference(AppStatePreference.THEME_SWITCH_TOAST_DETAIL_NEEDED, false)
    }

    /**
     * Handles toggling the apply system theme.
     * @return true if there is a theme change because of the toggle. false otherwise.
     */
    fun toggleAutoApplySystemtheme(): Boolean{
        val currentTheme = PreferenceManager.getPreference(AppStatePreference.THEME_APPLIED_NEW, ThemeType.DAY.getName())
        PreferenceManager.savePreference(AppStatePreference.THEME_APPLY_SYSTEM, !isAppFollowingSystemTheme())
        if(isAppFollowingSystemTheme()) {
            if (preferredTheme.getName() != currentTheme) {
                toggleAppliedtheme(CommonUtils.getApplication())
                return true
            }
        } else {
            val prevTheme = PreferenceManager.getPreference(AppStatePreference.PREV_THEME_APPLIED, ThemeType.DAY.getName())
            if(currentTheme != prevTheme) {
                PreferenceManager.savePreference(AppStatePreference.PREV_THEME_APPLIED,currentTheme)
                PreferenceManager.savePreference(AppStatePreference.THEME_APPLIED_NEW, prevTheme)
                return true
            }
        }
        return false
    }

    /**
     * Handles theme snackbar action click
     */
    fun themeSnackbarActionClicked(enabledClicked: Boolean, referrer:PageReferrer){
        val prevTheme = PreferenceManager.getPreference(AppStatePreference.THEME_APPLIED_NEW,ThemeType.DAY.getName())
        var displayTheme = Constants.EMPTY_STRING
        var currentTheme = ThemeType.DAY.getName()
        if(toggleAutoApplySystemtheme()){
            currentTheme = PreferenceManager.getPreference(AppStatePreference.THEME_APPLIED_NEW, Constants.EMPTY_STRING)
            AppSettingsProvider.onThemeChanged()
            if(enabledClicked) {
                displayTheme = Constants.AUTO
                setThemePreferences(false,false,true,true)
            } else {
                displayTheme = currentTheme
                setThemePreferences(true,true,false,false)
            }
        }
        AnalyticsHelper.logDisplayThemeEvent(displayTheme,prevTheme,currentTheme, referrer)
    }

    fun showThemeSnackbar(view: View, duration: Int, referrer:PageReferrer) {
        if(isAppFollowingSystemTheme()) {
            val text = CommonUtils.getString(com.newshunt.common.util.R.string.theme_snackbar_on_enable)
            val actionText = CommonUtils.getString(com.newshunt.common.util.R.string.theme_disable)
            GenericCustomSnackBar.showSnackBar(view,view.context,text,duration,null,null,actionText,customActionClickListener = View.OnClickListener {
                themeSnackbarActionClicked(false,referrer)
            }, isThemeSnackbar = true).show()
        } else {
            val text = CommonUtils.getString(com.newshunt.common.util.R.string.theme_snackbar_on_disable)
            val actionText = CommonUtils.getString(com.newshunt.common.util.R.string.theme_enable)
            GenericCustomSnackBar.showSnackBar(view,view.context,text,10000,null,null,actionText,customActionClickListener = View.OnClickListener {
                themeSnackbarActionClicked(true, PageReferrer(NhGenericReferrer.DISABLE_SNACKBAR))
                showThemeToast()
            }, isThemeSnackbar = true).show()
        }
        val prevTheme = PreferenceManager.getPreference(AppStatePreference.PREV_THEME_APPLIED,ThemeType.DAY.getName())
        val currentTheme = PreferenceManager.getPreference(AppStatePreference.THEME_APPLIED_NEW, Constants.EMPTY_STRING)
        AnalyticsHelper.logDisplayThemeEvent(Constants.AUTO,prevTheme,currentTheme, referrer)
    }

    fun showThemeToast(){
        FontHelper.showCustomFontToast(CommonUtils.getApplication(), CommonUtils.getString(com.newshunt.common.util.R.string.theme_enable_toast), Toast.LENGTH_LONG)
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

    interface ToggleThemeListener {
        fun onThemeChanged()
    }
}

data class ThemeState(val applySystemSetting: Boolean = true,
                      val themeType: ThemeType = ThemeType.DAY)
