/*
 * Copyright (c) 2020  Newshunt. All rights reserved.
 */

package com.newshunt.deeplink.navigator

import android.content.Intent
import com.newshunt.common.helper.common.DHConstants
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.notification.PermissionNavModel

/**
 * created by mukesh.yadav on 07/06/20
 */
class PermissionNavigator {
    companion object ClassMember {
        const val URL = "url"
        const val IMAGE = "image"
        const val Heading = "heading"
        const val SUB_HEADING = "subHeading"
        const val CTA_ALLOW = "ctaAllow"
        const val CTA_DENY = "ctaDeny"
        const val PERMISSIONS = "permissions"
        const val LANDING_PAGE = "landingPage"
        const val SUCCESS_TRACKERS = "successTrackers"

        @JvmStatic
        fun getIntentPermissionActivity(permissionNavModel: PermissionNavModel?, pageReferrer: PageReferrer?): Intent? {
            if (permissionNavModel == null) {
                return null
            }
            return Intent(DHConstants.LAUNCH_RUNTIME_PERMISSION_DP).also {
                it.putExtra(URL, permissionNavModel.url)
                it.putExtra(IMAGE, permissionNavModel.image)
                it.putExtra(Heading, permissionNavModel.heading)
                it.putExtra(SUB_HEADING, permissionNavModel.subHeading)
                it.putExtra(CTA_ALLOW, permissionNavModel.ctaAllow)
                it.putExtra(CTA_DENY, permissionNavModel.ctaDeny)
                it.putExtra(LANDING_PAGE, permissionNavModel.landingPage)
                it.putExtra(PERMISSIONS, permissionNavModel.permissions)
                it.putExtra(SUCCESS_TRACKERS, permissionNavModel.successTrackers)
            }
        }
    }
}