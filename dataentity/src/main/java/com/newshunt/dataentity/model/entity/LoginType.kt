/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.model.entity

/**
 * Enum for different types of login
 *
 * @author arun.babu
 */
enum class LoginType constructor(val value: String, val isSocial: Boolean) {
    NONE("NONE", false),
    GUEST("GUEST", false),
    GOOGLE("GOOGLE", true),
    FACEBOOK("FACEBOOK", true),
    MOBILE("MOBILE", true);


    companion object {
        @JvmStatic
        fun fromValue(value: String?): LoginType {
            for (type in LoginType.values()) {
                if (type.value.equals(value, ignoreCase = true)) {
                    return type
                }
            }
            return NONE
        }
    }
}

enum class AuthType {
    GOOGLE, FACEBOOK, TRUE_CALLER, GUEST;

    companion object {

        @JvmStatic
        fun getAuthTypeFromLoginType(loginType: LoginType?): AuthType? {
            loginType ?: return null

            return when (loginType) {
                LoginType.FACEBOOK -> FACEBOOK
                LoginType.GOOGLE -> GOOGLE
                LoginType.MOBILE -> TRUE_CALLER
                LoginType.GUEST -> GUEST
                else -> null
            }
        }
    }
}
