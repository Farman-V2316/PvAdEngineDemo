/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.upgrade

import com.newshunt.dataentity.model.entity.DEFAULT_DESCRIPTION_CHAR_LIMIT
import com.newshunt.dataentity.model.entity.DEFAULT_MAX_CARDS_GUEST
import com.newshunt.dataentity.model.entity.DEFAULT_MAX_RETRIES_OTP
import com.newshunt.dataentity.model.entity.DEFAULT_PROFILE_NAME_CHAR_LIMIT
import com.newshunt.dataentity.model.entity.DEFAULT_SIGNIN_BEFORE_LOGIN

/**
 * POJO for all profile related configs in the handshake response
 * <p>
 * Created by srikanth.ramaswamy on 04/17/2019.
 */
data class ProfileConfig(val fullNameCharLimit: Int = DEFAULT_PROFILE_NAME_CHAR_LIMIT, //Character limit for user's full name
                         val maxCardViewForGuestLogin: Int = DEFAULT_MAX_CARDS_GUEST, //Number of cards to view in guest login
                         val signInBeforeProfileLaunchCount: Int = DEFAULT_SIGNIN_BEFORE_LOGIN, //Number of times user is forced to sign in screen before landing to profile
                         val apisNeedingMigrationHeader: Map<String, List<String>>? = null, //Domain->api list mapping  which need the migration header "*" is the wild card
                         val disableTrueCallerLogin: Boolean = false, //Disable true caller?
                         val maxOtpAttemptsForTruecaller: Int = DEFAULT_MAX_RETRIES_OTP, //Number of retries for OTP
                         val descriptionMaxChars: Int = DEFAULT_DESCRIPTION_CHAR_LIMIT) //Character limit for bio and group description