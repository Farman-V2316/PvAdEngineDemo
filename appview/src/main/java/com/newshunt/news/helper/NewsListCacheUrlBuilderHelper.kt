/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper

import android.net.Uri
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.common.UrlUtil
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil
import com.newshunt.dataentity.news.model.entity.PageType
import com.newshunt.news.util.NewsConstants
import com.newshunt.sso.SSO
import java.util.HashMap

/**
 * @author santhosh.kc
 */

private const val KEY_USER_ID = "userId"

fun getCacheUrlBuilderFun(pageType: PageType?): (String?) -> (String?) {
    return when(pageType) {
        PageType.PROFILE_SAVED, PageType.PROFILE_SAVED_DETAIL -> getProfileSavedVideosCacheUrl()
        else -> getDefaultCacheUrlFun()
    }
}

private fun getProfileSavedVideosCacheUrl(): (String?) -> (String?) {
    return { url ->
        val basicParamsUrl = addBasicParams(url)
        try {
            val uriBuilder = Uri.parse(basicParamsUrl).buildUpon()
            uriBuilder.appendQueryParameter(KEY_USER_ID, SSO.getInstance().userDetails.userID
                    ?: Constants.EMPTY_STRING)
            uriBuilder.toString()
        } catch (e: Exception) {
            Logger.caughtException(e)
            basicParamsUrl
        }
    }
}

private fun getDefaultCacheUrlFun(): (String?) -> (String?) {
    return { url -> addBasicParams(url) }
}

fun addBasicParams(url: String?) : String? {

    if (CommonUtils.isEmpty(url)) {
        return url
    }

    val langKey = UserPreferenceUtil.getUserLanguages()
    val edition = UserPreferenceUtil.getUserEdition()
    val paramsMap = HashMap<String, String>()
    if (!CommonUtils.isEmpty(langKey)) {
        paramsMap[NewsConstants.LANGUAGE_CODE] = langKey
    }
    paramsMap[NewsConstants.EDITION_CODE] = edition
    paramsMap[NewsConstants.QUERY_PARAMETER_APP_LANGUAGE] = UserPreferenceUtil
            .getUserNavigationLanguage()
    return try {
        UrlUtil.getUrlWithQueryParamns(url, paramsMap)
    } catch (e: Exception) {
        Logger.caughtException(e)
        url
    }
}