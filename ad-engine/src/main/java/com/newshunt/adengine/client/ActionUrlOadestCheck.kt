/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.client

import com.newshunt.adengine.model.entity.AdErrorRequestBody
import com.newshunt.adengine.model.entity.AdErrorType
import com.newshunt.adengine.model.entity.BaseAdEntity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.adengine.model.entity.MultipleAdEntity
import com.newshunt.adengine.util.AdLogger
import com.newshunt.adengine.util.AdsUtil
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.DataUtil
import com.newshunt.common.helper.common.Logger
import java.net.URLDecoder

/**
 * Checks for presence of oadest value in action url of ad.
 * And updates action url to oadest value if the value is
 * nhcommand.
 *
 * @author raunak.yadav
 */
object ActionUrlOadestCheck {
    fun checkOadestValue(baseAdEntity: BaseAdEntity?): Boolean {
        var isAdValid = true
        if (baseAdEntity is BaseDisplayAdEntity) {
            isAdValid = setActionAndLandingUrl(baseAdEntity)
        } else if (baseAdEntity is MultipleAdEntity) {
            val baseDisplayAdEntities = baseAdEntity.baseDisplayAdEntities
            if (baseDisplayAdEntities.isEmpty()) {
                return false
            }
            val iterator = baseDisplayAdEntities.iterator()
            while (iterator.hasNext()) {
                val baseDisplayAdEntity = iterator.next()
                isAdValid = setActionAndLandingUrl(baseDisplayAdEntity)
                if (!isAdValid) {
                    iterator.remove()
                }
            }
            if (baseDisplayAdEntities.isEmpty()) {
                isAdValid = false
            }
        }
        return isAdValid
    }

    private fun setActionAndLandingUrl(baseDisplayAdEntity: BaseDisplayAdEntity): Boolean {
        val actionUrl = baseDisplayAdEntity.action
        if (actionUrl.isNullOrBlank() ||
                !(actionUrl.startsWith(Constants.URL_HTTP_FORMAT) || actionUrl.startsWith(Constants.URL_HTTPS_FORMAT))) {
            return true
        }
        var oadestIndex = actionUrl.indexOf("oadest")
        if (oadestIndex == -1) {
            return true
        }
        oadestIndex = actionUrl.indexOf(Constants.EQUAL_CHAR, oadestIndex) + 1
        val oadestValue: String
        val oadestUrl = actionUrl.substring(oadestIndex)
        oadestValue = try {
            URLDecoder.decode(oadestUrl, Constants.TEXT_ENCODING_UTF_8)
        } catch (e: Exception) {
            Logger.caughtException(e)
            AdLogger.e("AdsUrlError", "Error occurred decoding url $oadestUrl")
            AsyncAdImpressionReporter(baseDisplayAdEntity).hitErrorBeacon(
                    AdErrorRequestBody(errorCode = AdsUtil.getErrorCodeFor(AdErrorType.MALFORMED_CLICK_URL),
                            errorMessage = e.message, url = actionUrl))
            return false
        }
        if (DataUtil.isEmpty(oadestValue) || oadestValue.startsWith(Constants.URL_HTTP_FORMAT) ||
                oadestValue.startsWith(Constants.URL_HTTPS_FORMAT)) {
            return true
        }
        baseDisplayAdEntity.addLandingUrl(actionUrl)
        baseDisplayAdEntity.action = oadestValue
        return true
    }
}