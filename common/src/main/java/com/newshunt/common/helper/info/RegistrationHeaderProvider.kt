/*
 *  Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.info

import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Helper class to provide Registration related headers
 * <p>
 * Created by srikanth.ramaswamy on 04/26/21.
 */
private const val LOG_TAG = "RegistrationHeaderProvider"

object RegistrationHeaderProvider {
    private val REG_INFO_HEADER_FORMAT = "regDone=%b&ccid=%s&langCode=%s"
    private var isRegistrationDone: AtomicBoolean = AtomicBoolean(PreferenceManager.getPreference(AppStatePreference.IS_APP_REGISTERED, false))

    fun getRegistrationHeader(): String? {
        return if (!isRegistrationDone.get()) {
            String.format(REG_INFO_HEADER_FORMAT,
                    false,
                    ClientInfoHelper.getClientGeneratedClientId(),
                    UserPreferenceUtil.getUserLanguages())
        } else null
    }

    fun onRegistrationDone() {
        Logger.d(LOG_TAG, "onRegistrationDone, should stop sending the header")
        isRegistrationDone.set(true)
    }
}