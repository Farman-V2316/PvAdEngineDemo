/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.accounts.view.helper

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.newshunt.appview.R
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.AvailableAccounts
import com.newshunt.dataentity.sso.model.entity.DHAccount
import com.newshunt.helper.ImageUrlReplacer
import com.newshunt.sdk.network.image.Image

/**
 * View binding helper functions for the SSO module can be placed here
 *
 * @author srikanth on 06/12/2020
 */

class SSOViewBindingUtils {
    companion object {
        @JvmStatic
        fun getAccountIconVisibility(dhAccount: DHAccount, accType: LoginType): Int {
            val linkedAccount = dhAccount.linkedAccounts?.filter {
                it.loginType == accType
            }
            return if (linkedAccount.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        @JvmStatic
        fun getAccountLinkingUIVisibility(accountLinkingResult: AvailableAccounts?): Int{
            return if(accountLinkingResult == null || (accountLinkingResult.dhAccounts == null &&
                            accountLinkingResult.conflictAccounts == null)) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        @JvmStatic
        fun getSocialAccountsLinkUIVisibility(accountLinkingResult: AvailableAccounts?): Int {
            return if(getAccountLinkingUIVisibility(accountLinkingResult) == View.GONE) {
                return View.GONE
            } else {
                if (accountLinkingResult?.dhAccounts?.isNullOrEmpty()?.not() == true) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }

        @JvmStatic
        fun getMobileAccountLinkUIVisibility(accountLinkingResult: AvailableAccounts?): Int {
            return if (getAccountLinkingUIVisibility(accountLinkingResult) == View.GONE) {
                return View.GONE
            } else {
                if (accountLinkingResult?.conflictAccounts?.mobile?.isNullOrEmpty()?.not() == true) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }
}

@BindingAdapter("bind:linkProfileImg")
fun loadProfileThumbnail(imgView: ImageView, url: String?) {
    val width = CommonUtils.getDimension(R.dimen.profileIcon_widthHeight)
    val qualifiedUrl = ImageUrlReplacer.getQualifiedImageUrl(url, width, width)
    Image.load(qualifiedUrl)
            .placeHolder(CommonUtils.getDrawable(R.drawable.vector_user_avatar))
            .into(imgView)
}