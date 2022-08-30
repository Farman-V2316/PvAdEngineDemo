/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator

import android.content.Intent
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.ProfileTabType
import com.newshunt.dataentity.model.entity.UserBaseProfile
import com.newshunt.dataentity.notification.NavigationType
import com.newshunt.dataentity.notification.ProfileNavModel

/**
 * @author santhosh.kc
 */

class ProfileNavigator {

    companion object {

        @JvmStatic
        fun getTargetIntent(profileNavModel: ProfileNavModel?,
                            pageReferrer: PageReferrer? = null, userId: String?): Intent? {
            profileNavModel ?: return null
            val navigationType = NavigationType.fromIndex(
                    Integer.parseInt(profileNavModel.getsType()))

            navigationType ?: return null

            return when (navigationType) {
                NavigationType.TYPE_OPEN_PROFILE -> {
                    val userBaseProfile = UserBaseProfile()
                    userBaseProfile.userId = profileNavModel.userId ?: Constants.EMPTY_STRING
                    userBaseProfile.handle = profileNavModel.userHandle
                    val intent = CommonNavigator.getProfileHomeIntent(userBaseProfile,pageReferrer,
                            ProfileTabType.fromDeeplinkValue(profileNavModel.tabType, CommonUtils
                                    .equals(userId, profileNavModel.userId)),profileNavModel.defaultTabId)
                    intent
                }
                else -> null
            }
        }
    }
}