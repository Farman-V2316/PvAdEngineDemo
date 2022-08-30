/*
 * Copyright (c) 2019. Newshunt. All rights reserved.
 */

package com.newshunt.deeplink.navigator

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.dhutil.model.entity.BrowserType
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dhutil.helper.common.DailyhuntConstants

/**
 * @author anshul.jain
 */

class SSONavigator {


    companion object {

        private val SIGNIN_OPEN_ACTION = AppConfig.getInstance()!!.packageName + ".openSignin"

        @JvmOverloads
        @JvmStatic
        fun getIntentForSignIn(loginType: LoginType? = null, retryLogin:
        Boolean = false, autoLogin: Boolean = false, deepLinkUrl: String? = null, browserType:
                               BrowserType? = null, useWideViewPort: Boolean = false,
                               clearHistoryOnPageLoad: Boolean = false, pageReferrer: PageReferrer? = null,
                               successPendingIntent: PendingIntent? = null,
                               skipPendingIntent: PendingIntent? = null): Intent {
            val intent = Intent(SIGNIN_OPEN_ACTION)
            intent.setPackage(AppConfig.getInstance()?.packageName)
            intent.putExtra(Constants.LOGIN_TYPE, loginType?.value)
            intent.putExtra(Constants.RETRY_LOGIN, retryLogin)
            intent.putExtra(Constants.AUTO_LOGIN, autoLogin)
            intent.putExtra(Constants.DEEPLINK_URL, deepLinkUrl)
            intent.putExtra(Constants.BROWSER_TYPE, browserType)
            intent.putExtra(DailyhuntConstants.USE_WIDE_VIEW_PORT, useWideViewPort)
            intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer)
            intent.putExtra(DailyhuntConstants.CLEAR_HISTORY_ON_PAGE_LOAD, clearHistoryOnPageLoad)
            intent.putExtra(Constants.BUNDLE_SIGNIN_SUCCESS_PENDING_INTENT, successPendingIntent)
            intent.putExtra(Constants.BUNDLE_SIGNIN_SKIP_PENDING_INTENT, skipPendingIntent)
            return intent
        }

        @JvmStatic
        fun launchSignInActivity(activity: Activity, loginType: LoginType? = null, pageReferrer:
        PageReferrer? = null) {
            val intent = getIntentForSignIn(loginType = loginType, pageReferrer = pageReferrer)
            activity.startActivity(intent)
        }

        @JvmStatic
        fun startSocialMandatory(context: Context?) {
            val intent = getIntentForSignIn()
            if (context == null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                CommonUtils.getApplication().startActivity(intent)
            } else {
                context.startActivity(intent)
            }
        }

    }
}
