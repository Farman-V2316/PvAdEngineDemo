/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo
import com.newshunt.dhutil.helper.preference.AdsPreference

/**
 * This class provides updated value of AdsUpgradeInfo
 *
 * @author raunak.yadav
 */
class AdsUpgradeInfoProvider private constructor() {

    var adsUpgradeInfo: AdsUpgradeInfo? = null
        get() {
            if (field == null) {
                val adsConfigJson =
                    PreferenceManager.getPreference(AdsPreference.ADS_HANDSHAKE_RESPONSE_JSON,
                        Constants.EMPTY_STRING)
                field = JsonUtils.fromJson(adsConfigJson, AdsUpgradeInfo::class.java)
            }
            return field
        }

    companion object {
        private var instance: AdsUpgradeInfoProvider = AdsUpgradeInfoProvider()

        @JvmStatic
        fun getInstance(): AdsUpgradeInfoProvider = instance
    }
}