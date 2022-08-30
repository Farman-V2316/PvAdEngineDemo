/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.sso.model.entity

import com.newshunt.dataentity.model.entity.LoginType
import com.newshunt.dataentity.model.entity.UserBaseProfile
import java.io.Serializable

/**
 * Response for User Login API
 *
 * @author anshul.jain
 */

open class UserLoginResponse(var email: String? = null,
                             var mobile: String? = null,
                             var userAccountType: LoginType? = null,
                             var userData: String? = null,
                             var userMigrationCompleted: Boolean? = null,
                             var linkedAccounts: List<AccountLinkType>? = null) : UserBaseProfile()

/**
 * POJO representing conflicting mobile number accounts
 */
data class ConflictingDHAccount(val mobile: List<DHAccount>? = null)

data class LoginPayload(val clientIdentifier: String? = null,
                        val userAuthType: String? = null,
                        val token: String? = null,
                        val explicit: String? = null,
                        val fullName: String? = null,
                        val mobileNo: String? = null,
                        val tcPayload: TrueCallerPayload? = null): Serializable

data class TrueCallerPayload(val signedValue: String?,
                             val payload: String?,
                             val signedAlgo: String?) : Serializable

data class AccountLinkType(val loginType: LoginType?): Serializable

/**
 * POJO represents a DH Account for account linking purposes.
 */
data class DHAccount(val keyIdentifier: String? = null, //Temp id
                     val userId: String? = null, //User id
                     val profileImage: String? = null, //Profile image
                     val name: String? = null, //Name
                     val handle: String? = null, //Handle
                     val mobile: String? = null,
                     val loggedIn: Boolean = true, //Is this account logged in?
                     var linkedAccounts: List<AccountLinkType>? = null) //All linked accounts

/**
 * POJO representing list of social and conflicting mobile number accounts
 */
data class AvailableAccounts(val dhAccounts: List<DHAccount>? = null,
                             val conflictAccounts: ConflictingDHAccount? = null)

/**
 * POJO representing a selected DHAccount
 */
data class DHAccountId(val selectedId: String? = null,
                       val defunctId: String? = null)

/**
 * POJO representing a conflicting mobile number accounts
 */
data class ConflictingAccountId(val mobile: DHAccountId?)

/**
 * POJO to represent POST body for selecting primary account
 */
data class PrimaryAccountPayload(val dhAccount: DHAccountId? = null,
                                 val conflictAccounts: ConflictingAccountId? = null)

/**
 * Enum for different results of Account linking process
 */
enum class AccountLinkingResult {
    NO_ACC_LINKED,
    SAME_ACC_LINKED,
    DIFFERENT_ACC_LINKED
}