/*
 *  *Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper

import android.app.Activity
import android.content.pm.PackageManager
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.dataentity.dhutil.model.entity.notifications.ChineseDeviceInfo
import com.newshunt.dhutil.view.BatteryOptimizationDialogHelper
import com.newshunt.dhutil.view.BatteryOptimizationDialogHelper.setAutoStartEnableDialogShown
import com.newshunt.dataentity.news.analytics.NewsReferrer

/**
 * @author anshul.jain A helper for launching dialogs for battery optimization.
 */
class BatteryOptimizationDeviceInfoHelper {

    companion object {

        @JvmStatic
        fun handleChineseDeviceInfoResponse(activity: Activity, deviceInfoList:
        List<ChineseDeviceInfo?>, referrer: PageReferrer?) {
            launchDialog(activity, referrer, null)
        }

        private fun launchDialog(activity: Activity, referrer: PageReferrer?,
                                 deviceInfo: ChineseDeviceInfo?) {
            if (NewsReferrer.NEWS_HOME == referrer?.referrer) {
                BatteryOptimizationDialogHelper.handleAutoStartDialogOnHome(activity,
                        deviceInfo?.security_app_packagename,
                        deviceInfo?.security_app_activityname, referrer)
            } else if (NhGenericReferrer.NOTIFICATION_INBOX == referrer?.referrer) {
                BatteryOptimizationDialogHelper.handleAutoStartDialogOnInboxOrSetting(activity,
                        deviceInfo?.security_app_packagename,
                        deviceInfo?.security_app_activityname, referrer)
            }
        }
    }
}
