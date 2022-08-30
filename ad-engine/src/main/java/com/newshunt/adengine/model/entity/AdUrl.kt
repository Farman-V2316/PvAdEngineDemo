/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity

import android.net.Uri
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.adengine.model.entity.version.AdRequest
import com.newshunt.common.helper.appconfig.AppConfig
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.PasswordEncryption
import com.newshunt.common.helper.info.ClientInfoHelper
import com.newshunt.common.helper.info.ConnectionInfoHelper
import com.newshunt.common.helper.info.DeviceInfoHelper
import com.newshunt.common.helper.info.LocationInfoHelper
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dataentity.common.pages.PageSection
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper
import com.newshunt.dhutil.helper.preference.AdsPreference
import com.newshunt.dhutil.helper.preference.AppStatePreference
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer
import com.newshunt.dhutil.helper.theme.ThemeUtils
import com.newshunt.helper.player.PlayerControlHelper
import com.newshunt.sdk.network.connection.ConnectionManager
import java.io.Serializable

/**
 * Represents all the information to be given in Ad Url.
 * Like clientID, appVersion, current location, adType etc.
 *
 * @author raunak.yadav
 */
class AdUrl constructor(adRequest: AdRequest) : Serializable {
    private var adPosition: AdPosition? = null
    private var mainUrl = NewsBaseUrlContainer.getAdvertisementUrl()
    private var premiumAdvertisementUrl = NewsBaseUrlContainer.getPremiumAdvertisementUrl()

    private var client: String? = null
    private var brand: String? = null
    private var udID: String? = null
    private var clientId: String? = null
    private var appVersion: String? = null
    private var osVersion: String? = null
    private var googleAdvertisingId: String? = ClientInfoHelper.getGoogleAdId()

    private var deviceDensity: Float
    private var resolutionWidth: Int
    private var resolutionHeight: Int

    private var longitude: String? = null
    private var latitude: String? = null

    private var cellId: String? = null
    private var connectionType: String? = null
    private var connectionSpeed: String? = null

    private var entityId: String? = null
    private var entityType: String? = null
    private var entitySubType: String? = null
    private var section: String? = null

    private var postId: String? = null
    private var sourceId: String? = null
    private var sourceCatId: String? = null
    private var sourceType: String? = null

    private var langMask: String? = null
    private var bannerCount: Int = 1
    private var isHome: Boolean = false
    private var groupKey: String? = null
    private var pageReferrer: PageReferrer? = null
    private var referrerId: String? = null
    private var buzzSource: String? = null
    private var requiredAdTags: Map<String, Int>? = null
    private var isTestModeEnabled: Boolean = false

    //New Params for content targeting Instream Ad
    private var dhtvAdParams: Map<String, String>? = null

    init {
        mainUrl = if (!adRequest.adsBaseUrl.isNullOrBlank()) {
            adRequest.adsBaseUrl
        } else if (adPosition == AdPosition.P0 && !DataUtil.isEmpty(premiumAdvertisementUrl)) {
            premiumAdvertisementUrl
        } else {
            mainUrl
        }

        //Device info
        DeviceInfoHelper.getDeviceInfo().let {
            appVersion = AppConfig.getInstance()?.appVersion
            client = AppConfig.getInstance()?.client
            resolutionWidth = it.width.toInt()
            resolutionHeight = it.height.toInt()
            deviceDensity = it.density
        }

        //Location Info
        LocationInfoHelper.getLocationInfo(isAds = true).let {
            longitude = it.lon
            latitude = it.lat
        }

        //Connection Info
        connectionSpeed = ConnectionManager.getInstance()
                .getCurrentConnectionSpeed(CommonUtils.getApplication()).name
        with(ConnectionInfoHelper.getConnectionInfo()) {
            cellId = cellid
            connectionType = connection
        }

        val deviceInfo = DeviceInfoHelper.getDeviceInfo()

        //Client Info
        ClientInfoHelper.getClientInfo().let {
            osVersion = it.osVersion
            clientId = it.clientId
            udID = PasswordEncryption.encryptForAds(deviceInfo.deviceId)
            brand = it.brand
        }
        langMask = UserPreferenceUtil.getUserLanguages()

        setAdRequestParams(adRequest)
    }

    private fun setAdRequestParams(adRequest: AdRequest) {
        adPosition = adRequest.zoneType
        bannerCount = adRequest.numOfAds

        entityId = adRequest.entityId
        entityType = adRequest.entityType
        entitySubType = adRequest.entitySubType
        section = adRequest.section
        postId = adRequest.postId
        sourceId = adRequest.sourceId
        sourceCatId = adRequest.sourceCatId
        sourceType = adRequest.sourceType

        isHome = adRequest.isHome
        pageReferrer = adRequest.pageReferrer
        referrerId = adRequest.referrerId
        requiredAdTags = adRequest.requiredAdtags

        groupKey = adRequest.groupKey
        dhtvAdParams = adRequest.dhtvAdParams
    }

    override fun toString(): String {
        val uri = Uri.parse(mainUrl)

        val builder = uri.buildUpon()
        builder.appendQueryParameter("t", "nads")
        builder.appendQueryParameter("zone", adPosition?.value)

        client?.let {
            builder.appendQueryParameter("client", it)
        }

        if (!DataUtil.isEmpty(clientId)) {
            builder.appendQueryParameter("clientId", clientId)
        }
        if (!DataUtil.isEmpty(appVersion)) {
            builder.appendQueryParameter("appVer", appVersion)
        }

        if (!DataUtil.isEmpty(osVersion)) {
            builder.appendQueryParameter("osVersion", osVersion)
        }

        brand?.let {
            builder.appendQueryParameter("brand", it)
        }

        builder.appendQueryParameter("resolution", resolutionWidth.toString() + "x" + resolutionHeight)
                .appendQueryParameter("density", deviceDensity.toString())
                .appendQueryParameter("bannercount", bannerCount.toString())
                .appendQueryParameter("debug", "1")
                .appendQueryParameter("format", "json")

        if (latitude != null && longitude != null) {
            try {
                builder.appendQueryParameter("lat", latitude)
            } catch (e: Exception) {
                Logger.caughtException(e)
            }

            try {
                builder.appendQueryParameter("long", longitude)
            } catch (e: Exception) {
                Logger.caughtException(e)
            }

        }

        cellId?.let {
            builder.appendQueryParameter("cellid", it)
        }
        langMask?.let {
            builder.appendQueryParameter("lang", it)
        }
        connectionType?.let {
            builder.appendQueryParameter("conn", it)
        }
        udID?.let {
            builder.appendQueryParameter("udid", it)
        }
        googleAdvertisingId?.let {
            try {
                val encryptedGAId = PasswordEncryption.encryptForAds(it)
                builder.appendQueryParameter("gaid", encryptedGAId)
            } catch (e: Exception) {
                Logger.caughtException(e)
            }
        }
        entityId?.let {
            builder.appendQueryParameter("entityId", it)
        }
        entityType?.let {
            builder.appendQueryParameter("entityType", it)
        }
        entitySubType?.let {
            builder.appendQueryParameter("entitySubType", it)
        }
        section?.let {
            builder.appendQueryParameter("section",
                    if (section == PageSection.TV.section) section else PageSection.NEWS.section)
        }
        postId?.let { builder.appendQueryParameter("postId", it) }
        sourceId?.let { builder.appendQueryParameter("sourceId", it) }
        sourceCatId?.let { builder.appendQueryParameter("sourceCatId", it) }
        sourceType?.let { builder.appendQueryParameter("sourceType", it) }

        groupKey?.let {
            builder.appendQueryParameter("groupkey", it)
        }
        connectionSpeed?.let {
            builder.appendQueryParameter("connectionSpeed", it)
        }
        pageReferrer?.referrer?.let {
            builder.appendQueryParameter("pageReferrer", it.referrerName)
        }
        if (!CommonUtils.isEmpty(referrerId)) {
            builder.appendQueryParameter("referrerId", referrerId)
        }
        if (!CommonUtils.isEmpty(requiredAdTags)) {
            val tagBuilder = StringBuilder()
            for ((key, value) in requiredAdTags!!) {
                if (adPosition == AdPosition.PP1) {
                    tagBuilder.append("$key,")
                } else {
                    tagBuilder.append("$key:$value,")
                }
            }
            tagBuilder.deleteCharAt(tagBuilder.length - 1)
            builder.appendQueryParameter("requiredAdTags", tagBuilder.toString())
        }
        if (isHome) {
            builder.appendQueryParameter("isHome", "true")
        }
        builder.appendQueryParameter("isNightMode", ThemeUtils.isNightMode().toString())
        isTestModeEnabled = PreferenceManager.getPreference(AdsPreference.ENABLE_TEST_MODE_ALL_ADS, false)
        if(Logger.loggerEnabled() && isTestModeEnabled) {
            builder.appendQueryParameter("isTestModeEnabled", isTestModeEnabled.toString())
        }
        if (!CommonUtils.isEmpty(dhtvAdParams)) {
            for ((key, value) in dhtvAdParams!!) {
                builder.appendQueryParameter(key, value)
            }
        }
        builder.appendQueryParameter("autoPlayPref", AutoPlayHelper.getAutoPlayPreference().toString())
                .appendQueryParameter("muteState", PlayerControlHelper.isListMuteMode.toString())
                .appendQueryParameter("cardSize", if (PreferenceManager.getPreference
                        (AppStatePreference.ENABLE_SMALL_CARD, false)) CARD_SMALL else CARD_LARGE)

        return builder.toString()
    }

    companion object {
        private const val serialVersionUID = 4599181787676673468L
        private const val CARD_SMALL = "small"
        private const val CARD_LARGE = "large"
    }
}
