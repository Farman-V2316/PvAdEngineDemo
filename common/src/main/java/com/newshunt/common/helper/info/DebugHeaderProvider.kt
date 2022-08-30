/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.info

import com.newshunt.common.helper.UserConnectionHolder
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.sdk.network.connection.ConnectionManager
import java.net.URLEncoder

/**
 * A singleton to cook up the debug header
 *
 * Created by srikanth.ramaswamy on 10/05/2018
 */
const val PARTNER_REF_PARAM = "partnerRef"
private const val DEBUG_HEADER_FORMAT =
        "os=android&appVersion=%s&osVersion=%s&clientId=%s&conn_type=%s&conn_quality=%s&sessionSource=%s" +
                "&featureMask=%s&featureMaskV1=%s"
private const val DEBUG_HEADER_FORMAT_PARTNERREF = "appVersion=%s&osVersion=%s&clientId=%s&conn_type=%s&conn_quality=%s&sessionSource=%s&partnerRef=%s&featureMask=%s&featureMaskV1=%s"

object DebugHeaderProvider {
    var partnerRef: String? = null
        set(value) {
            field = if (!CommonUtils.isEmpty(value)) {
                try {
                    URLEncoder.encode(value, Constants.TEXT_ENCODING_UTF_8)
                } catch (ex: Exception) {
                    null
                }
            } else null
        }
    var sessionSource : String = "organic"
        set(value) {
            if(!CommonUtils.isEmpty(value)) {
                field = value
            }
        }

    fun getDebugHeader() : String{
        try {
            var connectionType = ConnectionInfoHelper.getConnectionType()
            if (CommonUtils.isEmpty(connectionType)) {
                connectionType = Constants.EMPTY_STRING
            }
            var connectionQuality = Constants.EMPTY_STRING
            val connectionSpeed = UserConnectionHolder.userConnectionQuality
            if (connectionSpeed != null) {
                connectionQuality = connectionSpeed
            }

            val featureMask = PreferenceManager.getPreference(GenericAppStatePreference
                    .FEATURE_MASK, Constants.FEATURE_MASK)
            val featureMaskV1 = PreferenceManager.getPreference(GenericAppStatePreference
                    .FEATURE_MASK_V1, Constants.FEATURE_MASK_V1)

            return if (!CommonUtils.isEmpty(partnerRef)) {
                String.format(DEBUG_HEADER_FORMAT_PARTNERREF, AppConfig.getInstance()!!.appVersion,
                        DeviceInfoHelper.getDeviceInfo().osVersion, AppUserPreferenceUtils
                        .getClientId(), connectionType, connectionQuality, sessionSource,
                        partnerRef!!, featureMask, featureMaskV1)
            } else {
                String.format(DEBUG_HEADER_FORMAT, AppConfig.getInstance()!!.appVersion,
                        DeviceInfoHelper.getDeviceInfo().osVersion, AppUserPreferenceUtils
                        .getClientId(), connectionType, connectionQuality, sessionSource,
                        featureMask, featureMaskV1)
            }
        } catch (ex: Exception){
            Logger.caughtException(ex)
        }
        return Constants.EMPTY_STRING
    }
}