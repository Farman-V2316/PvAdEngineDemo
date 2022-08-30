/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.sso.view.activity

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.newshunt.analytics.client.AnalyticsClient
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.model.entity.SettingsChangeEvent
import com.newshunt.common.view.customview.NHBaseActivity
import com.newshunt.dhutil.analytics.NhAnalyticsSettingsEvent
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener
import com.newshunt.dhutil.helper.AppSettingsProvider
import com.newshunt.deeplink.navigator.CommonNavigator
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.sso.R
import com.newshunt.sso.SSO
import com.newshunt.sso.view.fragment.SignOnFlow
import com.newshunt.sso.view.fragment.SignOnFragment
import java.util.*

/**
 * Sign On activity for login/sign up operations
 *
 * @author arun.babu
 */
class SignOnActivity : NHBaseActivity(), SignOnFlow, ReferrerProviderlistener {
    override fun getProvidedReferrer(): PageReferrer? {
        return providedReferrer
    }

    override fun getReferrerEventSection(): NhAnalyticsEventSection {
        return NhAnalyticsEventSection.APP
    }

    override fun getLatestPageReferrer(): PageReferrer {
        return PageReferrer(NhGenericReferrer.SIGNIN_VIEW)
    }

    private lateinit var signOnFragment: SignOnFragment
    private var providedReferrer: PageReferrer? = null
    private var themeID = ThemeUtils.preferredTheme.themeId
    private var appLang = UserPreferenceUtil.getUserNavigationLanguage()
    private var userLangs = UserPreferenceUtil.getUserLanguages()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeID = ThemeUtils.preferredTheme.themeId
        setTheme(themeID)
        setContentView(R.layout.activity_sign_on)
        observeSession()
        observeSettingChanges()
        val ivNavigation = findViewById<View>(R.id.iv_sign_on_navigation) as ImageView
        ivNavigation.setOnClickListener { onBackPressed() }

        val settingsIcon = findViewById<View>(R.id.settings_icon)
        settingsIcon.setOnClickListener {
            val paramsMap = HashMap<NhAnalyticsEventParam, Any?>()
            AnalyticsClient.log(NhAnalyticsSettingsEvent.SETTINGS_CLICKED,
                    NhAnalyticsEventSection.APP, paramsMap, latestPageReferrer)
            CommonNavigator.launchSettingsActivity(this)
        }

        signOnFragment = SignOnFragment()
        intent?.extras?.let {
            providedReferrer = it.getSerializable(Constants.BUNDLE_ACTIVITY_REFERRER) as? PageReferrer
            signOnFragment.arguments = it
        }
        supportFragmentManager.beginTransaction().add(R.id.container, signOnFragment).commit()

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
                else -> {
                }
            }

            if (needsRestart) {
                recreate()
            }
        })
    }

    override fun onLoginSuccess(pendingIntent: PendingIntent?) {
        pendingIntent?.send()
        finish()
    }

    override fun onSkipSignOn(pendingIntent: PendingIntent?) {
        pendingIntent?.send()
        finish()
    }

    private fun observeSession() {
        SSO.getInstance().userDetailsLiveData.observe(this, Observer {
            if (SSO.getInstance().isLoggedIn(false) && !isFinishing) {
                finish()
            }
        })
    }
}