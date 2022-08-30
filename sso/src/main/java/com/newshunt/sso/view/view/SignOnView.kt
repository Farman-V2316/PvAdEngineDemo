/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.view.view

import com.newshunt.common.view.view.BaseMVPView
import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse
import com.newshunt.sso.model.entity.SSOResult

/**
 * View interface for Sign On screen with Email
 *
 * @author arun.babu
 */
interface SignOnView : BaseMVPView {
    fun facebookLogin() {}

    fun googleLogin() {}

    fun phoneNumberLogin() {}

    fun showLoadingProgress(show: Boolean, text: String?) {}

    fun showSignOnView(show: Boolean) {}

    fun showToast(message: String?) {}

    fun showLoginSuccessMessage(name: String?) {}

    fun showUnexpectedError() {}

    fun onLoginSuccessful(name: String?, loginType: LoginType, loginResponse: UserLoginResponse?) {}

    fun onLoginFailed(loginType: LoginType, failureResult: SSOResult? = null) {}

    fun isAccountLinkingFlow(): Boolean {
        return false
    }
}
