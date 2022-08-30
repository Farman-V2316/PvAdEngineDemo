/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.profile.view

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.newshunt.appview.R
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.model.entity.MyProfile
import com.newshunt.dataentity.sso.model.entity.AccountLinkType
import com.newshunt.dhutil.helper.theme.ThemeUtils

/**
 * View binding helper functions for the Profile module can be placed here
 *
 * @author srikanth on 06/15/2020
 */
class ProfileViewBindingUtils {
    companion object {
        @JvmStatic
        fun isAccountVerified(linkedAccounts: List<AccountLinkType>?, accountType: LoginType):
                Boolean {
            return if (linkedAccounts.isNullOrEmpty()) {
                false
            } else {
                linkedAccounts.any {
                    it.loginType == accountType
                }
            }
        }

        @JvmStatic
        fun getConnectText(linkedAccounts: List<AccountLinkType>?, accountType: LoginType): String {
            return if (isAccountVerified(linkedAccounts, accountType)) {
                if (accountType == LoginType.MOBILE) {
                    CommonUtils.getString(R.string.edit_text)
                } else {
                    CommonUtils.getString(R.string.connected)
                }
            } else {
                CommonUtils.getString(R.string.connect)
            }
        }

        @JvmStatic
        fun getConnectTextColor(linkedAccounts: List<AccountLinkType>?, accountType: LoginType): Int {
            return if (isAccountVerified(linkedAccounts, accountType)) {
                CommonUtils.getColor(R.color.connect_color_grey)
            } else {
                if(ThemeUtils.isNightMode()) {
                    CommonUtils.getColor(R.color.white)
                } else {
                    CommonUtils.getColor(R.color.black)
                }
            }
        }

        @JvmStatic
        fun isAccountConnectEnabled(linkedAccounts: List<AccountLinkType>?, accountType: LoginType): Boolean {
            return if (isAccountVerified(linkedAccounts, accountType)) {
                accountType == LoginType.MOBILE
            } else {
                true
            }
        }

        @JvmStatic
        fun getAccountTypeText(myProfile: MyProfile?, account: String, accountType: LoginType): String? {
            return if (accountType == LoginType.MOBILE && myProfile?.mobileDetail?.id.isNullOrEmpty().not()) {
                myProfile?.mobileDetail?.id
            } else {
                account
            }
        }
    }
}

@BindingAdapter("bind:accountTypeIcon")
fun setAccountTypeIcon(imageView: ImageView,
                       loginType: LoginType) {
    when (loginType) {
        LoginType.GOOGLE -> {
            imageView.setImageResource(R.drawable.ic_circular_google_icon)
        }
        LoginType.FACEBOOK -> {
            imageView.setImageResource(R.drawable.ic_circular_fb_icon)
        }
        LoginType.MOBILE -> {
            imageView.setImageResource(R.drawable.ic_circular_tc_icon)
        }
    }
}
