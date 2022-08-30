/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity

import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse

/**
 * Internal login response for SSO components
 *
 * @author arun.babu
 */
data class LoginResponse(var ssoResult: SSOResult? = null,
                         var userLoginResponse: UserLoginResponse? = null,
                         val isAccountLinkingInProgress: Boolean = false) {
    val loginType: LoginType?
        get() = userLoginResponse?.userAccountType
}
