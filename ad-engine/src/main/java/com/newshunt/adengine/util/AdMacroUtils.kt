/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import android.net.Uri
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.model.entity.NativeAdHtml
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.common.helper.common.PasswordEncryption
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.info.ConnectionInfoHelper
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.common.helper.info.LocationInfoHelper
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.model.entity.status.ClientInfo
import com.newshunt.dataentity.common.model.entity.status.ConnectionInfo
import com.newshunt.dataentity.common.model.entity.status.DeviceInfo
import com.newshunt.dataentity.common.model.entity.status.LocationInfo
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.sdk.network.connection.ConnectionManager
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Definitions : https://evp.atlassian.net/wiki/spaces/NHADSPLTL/pages/109609309/Evergreen+Ads
 * +-+Protocol+V1#Macro-Definition
 *
 * @author raunak
 */
object AdMacroUtils {

    private const val MACRO_TOKEN = "--"
    private const val CL_UUID = "--CL_UUID--"
    private const val CL_TIMESTAMP = "--CL_TIMESTAMP--"

    private const val CL_LATITUDE = "--CL_LATITUDE--"
    private const val CL_LONGITUDE = "--CL_LONGITUDE--"
    private const val CL_COUNTRY_CODE = "--CL_COUNTRY_CODE--"
    private const val CL_CLIENT_ID = "--CL_CLIENT_ID--"
    private const val CL_USER_LANGUAGE = "--CL_USER_LANGUAGE--"

    private const val CL_APP_VERSION = "--CL_APP_VERSION--"
    private const val CL_UDID = "--CL_UDID--"
    private const val CL_ADVERTISING_ID = "--CL_ADVERTISING_ID--"
    private const val CL_USER_OS = "--CL_USER_OS--"
    private const val CL_USER_OS_VERSION = "--CL_USER_OS_VERSION--"
    private const val CL_DENSITY = "--CL_DENSITY--"
    private const val CL_DEVICE_RESOLUTION = "--CL_DEVICE_RESOLUTION--"

    private const val CL_AD_PLACEMENT = "--CL_AD_PLACEMENT--"
    private const val CL_SUB_SLOT = "--CL_SUB_SLOT--"
    private const val CL_ENTITY_ID = "--CL_ENTITY_ID--"
    private const val CL_ENTITY_TYPE = "--CL_ENTITY_TYPE--"
    private const val CL_SUB_ENTITY_TYPE = "--CL_SUB_ENTITY_TYPE--"
    private const val CL_SOURCE_ID = "--CL_SOURCE_ID--"
    private const val CL_SOURCE_CAT_ID = "--CL_SOURCE_CAT_ID--"
    private const val CL_SOURCE_TYPE = "--CL_SOURCE_TYPE--"
    private const val CL_POST_ID = "--CL_POST_ID--"
    private const val CL_PAGE_REFERRER = "--CL_PAGE_REFERRER--"
    private const val CL_REFERRER_ID = "--CL_REFERRER_ID--"
    private const val CL_AUTO_PLAY_PREF = "--CL_AUTO_PLAY_PREF--"

    private const val CL_USER_CONNECTION = "--CL_USER_CONNECTION--"
    private const val CL_CONNECTION_SPEED = "--CL_CONNECTION_SPEED--"
    private const val CL_CELL_ID = "--CL_CELL_ID--"
    private const val CL_ISP = "--CL_ISP--"

    private var clientInfo: ClientInfo? = null
    private var deviceInfo: DeviceInfo? = null
    private var locationInfo: LocationInfo? = null
    private var connectionInfo: ConnectionInfo? = null
    private var connectionSpeed: String? = null
    private var langMask: String? = null

    private fun fetchUserInfo() {
        //Client Info
        clientInfo = ClientInfoHelper.getClientInfo()
        langMask = UserPreferenceUtil.getUserLanguages()
        //Device info
        deviceInfo = DeviceInfoHelper.getDeviceInfo()
        //Location Info
        locationInfo = LocationInfoHelper.getLocationInfo(isAds = true)
        //Connection Info
        connectionSpeed = ConnectionManager.getInstance()
            .getCurrentConnectionSpeed(CommonUtils.getApplication()).name
        connectionInfo = ConnectionInfoHelper.getConnectionInfo()
    }

    /**
     * Replaces the pre-defined macros with the values, if available
     *
     * If new fields are added to AdEntity that support macros, they must be processed here.
     *
     * @param adEntity Ad
     */
    fun replaceMacrosInAd(adEntity: BaseAdEntity, adRequest: AdRequest? = null) {
        fetchUserInfo()
        val macroMap = HashMap<String, String?>()
        adEntity.adPosition?.let {
            macroMap[CL_AD_PLACEMENT] = it.value
        }
        adEntity.adTag?.let {
            macroMap[CL_SUB_SLOT] = it
        }
        populateDataFrom(adRequest, macroMap)

        adEntity.action = replaceMacro(adEntity.action, true, macroMap, refreshInfo = false)
        adEntity.requestUrl = replaceMacro(adEntity.requestUrl, true, macroMap, refreshInfo = false)

        if (adEntity is BaseDisplayAdEntity) {
            replaceMacroInBaseAd(adEntity, macroMap)
        } else if (adEntity is MultipleAdEntity) {
            adEntity.baseDisplayAdEntities.forEach { ad ->
                replaceMacroInBaseAd(ad, macroMap)
            }
        }
    }

    private fun replaceMacroInBaseAd(adEntity: BaseDisplayAdEntity,
                                     macroMap: HashMap<String, String?>) {
        adEntity.id = replaceMacro(adEntity.id, macroMap = macroMap, refreshInfo = false)
        adEntity.beaconUrl = replaceMacro(adEntity.beaconUrl, true, macroMap, refreshInfo = false)
        adEntity.landingUrl = replaceMacro(adEntity.landingUrl, true, macroMap, refreshInfo = false)
        adEntity.errorUrl = replaceMacro(adEntity.errorUrl, true, macroMap, refreshInfo = false)
        adEntity.adInflatedBeaconUrl = replaceMacro(adEntity.adInflatedBeaconUrl, true, macroMap, refreshInfo = false)
        adEntity.adRespondedBeaconUrl = replaceMacro(adEntity.adRespondedBeaconUrl, true, macroMap, refreshInfo = false)
        adEntity.adLPTimeSpentBeaconUrl = replaceMacro(adEntity.adLPTimeSpentBeaconUrl, true, macroMap, refreshInfo = false)
        adEntity.adReactionBeaconUrl = replaceMacro(adEntity.adReactionBeaconUrl, true, macroMap, refreshInfo = false)
        adEntity.adShareBeaconUrl = replaceMacro(adEntity.adShareBeaconUrl, true, macroMap, refreshInfo = false)
        adEntity.adClosedBeaconUrl = replaceMacro(adEntity.adClosedBeaconUrl, true, macroMap, refreshInfo = false)

        if (adEntity is NativeAdHtml) {
            adEntity.coolAd?.content?.data = replaceMacro(adEntity.coolAd?.content?.data, true, macroMap, refreshInfo = false)
            adEntity.coolAd?.tracker?.data = replaceMacro(adEntity.coolAd?.tracker?.data, true, macroMap, refreshInfo =  false)
        }
    }

    /**
     * Replaces the pre-defined macros with the values, if available
     *
     * @param input The string containing macros
     * @param macroMap Map to store macro values so that we dont have different values in meta of
     * same ad.
     * @param refreshInfo Boolean for whether to fetch the user info fresh. For replacing macros
     * in one ads meta, we need not unnecessarily fetch data again for each url.
     */
    fun replaceMacro(input: String?, encode: Boolean = false,
                     macroMap: HashMap<String, String?> = HashMap(),
                     refreshInfo: Boolean = true): String? {
        if (input.isNullOrBlank()) return input
        if (refreshInfo) {
            fetchUserInfo()
        }

        val sb = StringBuffer()
        var macroStart: Int
        var macroEnd: Int
        var index = 0

        while (index < input.length) {
            macroStart = input.indexOf(MACRO_TOKEN, index)

            if (macroStart == -1) {
                //No macros from index to last
                sb.append(input.substring(index, input.length))
                return sb.toString()
            } else {
                if (index != macroStart) {
                    sb.append(input.substring(index, macroStart))
                    index = macroStart
                }
                macroEnd = input.indexOf(MACRO_TOKEN, macroStart + 2)
                if (macroEnd == -1) {
                    //No macro end found till last
                    sb.append(input.substring(index, input.length))
                    return sb.toString()
                } else {
                    val macro = input.substring(macroStart, macroEnd + 2)
                    if (!macroMap.containsKey(macro)) {
                        macroMap[macro] = getValueFor(macro)
                    }
                    sb.append(if (encode) Uri.encode(macroMap[macro]) else macroMap[macro])
                    index = macroEnd + 2
                }
            }
        }
        return sb.toString()
    }

    private fun populateDataFrom(adRequest: AdRequest?,
                                 macroMap: HashMap<String, String?>) {
        adRequest?.let {
            macroMap[CL_ENTITY_ID] = it.entityId
            macroMap[CL_ENTITY_TYPE] = it.entityType
            macroMap[CL_SUB_ENTITY_TYPE] = it.entitySubType
            macroMap[CL_SOURCE_ID] = it.sourceId
            macroMap[CL_SOURCE_CAT_ID] = it.sourceCatId
            macroMap[CL_SOURCE_TYPE] = it.sourceType
            macroMap[CL_POST_ID] = it.postId
            macroMap[CL_PAGE_REFERRER] = it.pageReferrer?.referrer?.referrerName
            macroMap[CL_REFERRER_ID] = it.referrerId
        }
    }

    private fun getValueFor(macro: CharSequence): String? {
        return when (macro) {
            CL_UUID -> UUID.randomUUID().toString()
            CL_CLIENT_ID -> clientInfo?.clientId
            CL_USER_LANGUAGE -> langMask
            //TODO: PANDA removed for removing dependency with SSO module
//            CL_COUNTRY_CODE -> PreferenceManager.getPreference(SSOPreference.COUNTRY_CODE,
//                Constants.EMPTY_STRING)
            CL_APP_VERSION -> deviceInfo?.appVersion
            CL_UDID -> PasswordEncryption.encryptForAds(deviceInfo?.deviceId)
            CL_ADVERTISING_ID -> PasswordEncryption.encryptForAds(ClientInfoHelper.getGoogleAdId())
            CL_USER_OS -> deviceInfo?.client
            CL_USER_OS_VERSION -> deviceInfo?.osVersion
            CL_DENSITY -> deviceInfo?.density?.toString()
            CL_DEVICE_RESOLUTION -> (deviceInfo?.width?.toInt())?.toString()?.plus('x')
                ?.plus(deviceInfo?.height?.toInt())

            CL_AUTO_PLAY_PREF -> AutoPlayHelper.getAutoPlayPreference().toString()
            CL_LATITUDE -> locationInfo?.lat
            CL_LONGITUDE -> locationInfo?.lon
            CL_USER_CONNECTION -> connectionInfo?.connection
            CL_CONNECTION_SPEED -> connectionSpeed
            CL_ISP -> connectionInfo?.apnName
            CL_CELL_ID -> connectionInfo?.cellid
            CL_TIMESTAMP -> TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
            else -> macro.toString()
        }
    }

}