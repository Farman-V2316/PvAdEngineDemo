/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.group.viewmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.View
import com.newshunt.appview.R
import com.newshunt.appview.common.ui.helper.NavigationEvent
import com.newshunt.appview.common.ui.helper.NavigationHelper
import com.newshunt.appview.common.viewmodel.ClickHandlingViewModel
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.common.view.customview.fontview.NHTextView
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.AppSection
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.onboarding.presenter.AppRegistrationHandler
import com.newshunt.sso.SSO
import com.newshunt.sso.model.entity.LoginMode
import com.newshunt.sso.model.entity.SSOLoginSourceType

/**
 * Helper class to handle clicks from full screen error layouts.
 * <p>
 * Created by srikanth.ramaswamy on 12/01/2019
 */
class ErrorClickDelegate(private val retry: () -> Unit) : ClickHandlingViewModel {

    override fun onViewClick(view: View) {
        val retry = CommonUtils.getString(com.newshunt.dhutil.R.string.dialog_button_retry)
        val home = CommonUtils.getString(com.newshunt.dhutil.R.string.btn_home)
        val settings = CommonUtils.getString(com.newshunt.dhutil.R.string.action_settings)
        val isRegistered = PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false)
        when {
            view.id == R.id.error_action -> {
                val actionText = view as NHTextView
                when (actionText.originalText) {
                    retry -> {
                        if (isRegistered) {
                            retryLogin(view.context)
                        } else {
                            AppRegistrationHandler.getInstance().performRegistration(true)
                        }
                    }
                    home -> {
                        val prevNewsAppSection = AppSectionsProvider.getAnyUserAppSectionOfType(AppSection.NEWS)
                        val navigationIntent = NavigationEvent(CommonNavigator.getNewsHomeIntent(view.context,
                                false,
                                prevNewsAppSection?.id,
                                prevNewsAppSection?.appSectionEntityKey,
                                null,
                                false))
                        NavigationHelper.navigationLiveData.postValue(navigationIntent)
                    }
                    settings -> {
                        val nwSettingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                        NavigationHelper.navigationLiveData.postValue(NavigationEvent(nwSettingIntent))
                    }
                }
            }
        }
    }

    fun retryLogin(context: Context) {
        if (SSO.getInstance().isLoggedIn(true)) {
            //Retry the request
            retry()
        } else {
            //View is showing the NO SESSION error screen, lets try guest login
            (context as? Activity?)?.let {
                SSO.getInstance().login(it, LoginMode.BACKGROUND_ONLY,
                        SSOLoginSourceType.GROUP_SCREENS)
            }
        }
    }
}