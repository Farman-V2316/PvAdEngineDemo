/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator

import android.content.Intent
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.notification.NavigationModel
import com.newshunt.dataentity.notification.NavigationType

/**
 * @author shrikant.agrawal
 */
object InboxNavigator {

    @JvmStatic
    fun getTargetIntent(
        navigationModel: NavigationModel?,
        pageReferrer: PageReferrer?
    ): Intent? {
        var pageReferrer = pageReferrer
        var targetIntent: Intent? = null
        if (null == navigationModel) {
            return null
        }
        val type = NavigationType.fromIndex(navigationModel.getsType().toInt())
        when (type) {
            NavigationType.SELF_BOARDING, NavigationType.TYPE_OPEN_APP -> {
                targetIntent = Intent()
                targetIntent.action = Constants.SPLASH_ACTION
                targetIntent.addCategory(Intent.CATEGORY_DEFAULT)
                targetIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                targetIntent.setPackage(AppConfig.getInstance().packageName)
                if (null == pageReferrer) {
                    pageReferrer =
                        PageReferrer(NhGenericReferrer.NOTIFICATION, Constants.EMPTY_STRING)
                }
            }
        }
        if (null != targetIntent) {
            if (!CommonUtils.isEmpty(navigationModel.promoId)) {
                targetIntent.putExtra(
                    Constants.PROMO_ID_PARAM_KEY,
                    navigationModel.promoId
                )
            }
            targetIntent.putExtra(
                Constants.BUNDLE_ACTIVITY_REFERRER,
                pageReferrer
            )
        }
        return targetIntent
    }
}