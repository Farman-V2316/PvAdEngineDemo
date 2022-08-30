/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.activity

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.view.UniqueIdHelper
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.SettingsChangeEvent
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.model.entity.ProfileUserIdInfo
import com.newshunt.deeplink.navigator.NewsNavigator
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.news.view.activity.NewsBaseActivity
import com.newshunt.sso.SSO

/**
 * Base activity for activities which depend on user id. Handles recreation when settings changed
 * etc.
 * <p>
 * Created by srikanth.ramaswamy on 09/18/2019.
 */
abstract class AuthorizationBaseActivity: NewsBaseActivity() {
    protected val activityID = UniqueIdHelper.getInstance().generateUniqueId()
    protected var appLanguage = AppUserPreferenceUtils.getUserNavigationLanguage()
    protected var themeID = ThemeUtils.preferredTheme.themeId
    protected var appLang = UserPreferenceUtil.getUserNavigationLanguage()
    protected var userLangs = UserPreferenceUtil.getUserLanguages()
    protected var myUserId: ProfileUserIdInfo = ProfileUserIdInfo(Constants.EMPTY_STRING,
            Constants.EMPTY_STRING)
    protected var isSocialLogin = SSO.getInstance().isLoggedIn(false)
    protected var pageReferrer: PageReferrer? = null
    protected var acceptableTimestamp = System.currentTimeMillis()
    protected var cardStyle = PreferenceManager.getPreference(AppStatePreference.ENABLE_SMALL_CARD, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(themeID)
        super.onCreate(savedInstanceState)
        observeSessionChanges()
        observeSettingChanges()
        observeNavigations()
    }

    override fun onStart() {
        super.onStart()
        acceptableTimestamp = System.currentTimeMillis()
    }

    protected abstract fun getLogTag(): String

    protected abstract fun showLoginError()

    protected open fun onUserIdAvailable() {}

    protected open fun getDetailFragmentHostId(): Int {
        return -1
    }

    protected fun mandateGuestLogin(): Boolean {
        if (!SSO.getInstance().isLoggedIn(true) || CommonUtils.isEmpty(SSO.getInstance().userDetails?.userLoginResponse?.userId)) {
            showLoginError()
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        mandateGuestLogin()
    }

    override fun onBackPressed() {
        handleBack(true)
    }

    protected open fun handleBack(isSystemBack: Boolean,referrerRaw:String? = null) {
        if (NewsNavigator.shouldNavigateToHome(this, pageReferrer, isSystemBack,referrerRaw)) {
            val pageReferrer = PageReferrer(NhGenericReferrer.PROFILE)
            pageReferrer.referrerAction = NhAnalyticsUserAction.BACK
            NewsNavigator.navigateToHomeOnLastExitedTab(this, pageReferrer)
            finish()
        } else {
            NavigationHelper.onBackPressed(this, R.id.detail_fragment_holder)
        }
    }

    protected open fun setUpActionBar(toolBar: Toolbar) {
    }

    protected open fun observeSessionChanges() {
        SSO.getInstance().userDetailsLiveData.observe(this, Observer {
            if (mandateGuestLogin()) {
                if (!CommonUtils.isEmpty(myUserId.userId) && myUserId.userId != it.userID) {
                    recreate()
                    Logger.d(getLogTag(), "userId changed, restarting the activity")
                    return@Observer
                }
                myUserId = ProfileUserIdInfo(it.userLoginResponse?.userId ?: Constants.EMPTY_STRING,
                        it.userLoginResponse?.handle ?: Constants.EMPTY_STRING)
                isSocialLogin = (it.loginType != LoginType.NONE && it.loginType != LoginType.GUEST)
                onUserIdAvailable()
            }
        })
    }

    private fun observeSettingChanges() {
        AppSettingsProvider.settingsChangedLiveData.observe(this, Observer {
            var needsRestart = false
            when (it.changeType) {
                SettingsChangeEvent.ChangeType.APP_LANGUAGE -> {
                    if (appLang != UserPreferenceUtil.getUserNavigationLanguage()) {
                        appLang = UserPreferenceUtil.getUserNavigationLanguage()
                        needsRestart = true
                    }
                }
                SettingsChangeEvent.ChangeType.LANGUAGES -> {
                    if (userLangs != UserPreferenceUtil.getUserLanguages()) {
                        userLangs = UserPreferenceUtil.getUserLanguages()
                        needsRestart = true
                    }
                }
                SettingsChangeEvent.ChangeType.CARD_STYLE -> {
                    val curCardStyle = PreferenceManager.getPreference(AppStatePreference.ENABLE_SMALL_CARD, false)
                    if (cardStyle != curCardStyle) {
                        cardStyle = curCardStyle
                        needsRestart = true
                    }
                }
                else -> {
                }
            }

            if (needsRestart) {
                Logger.d(getLogTag(), "Restarting the activity, setting changed")
                recreate()
            }
        })
    }

    private fun observeNavigations() {
        NavigationHelper.navigationLiveData.observe(this, Observer {
            if (it.timeStamp < acceptableTimestamp)
                return@Observer

            it.intent?.let { _ ->
                NavigationHelper.handleNavigationEvents(it, this, getDetailFragmentHostId())
            }
        })
    }
}