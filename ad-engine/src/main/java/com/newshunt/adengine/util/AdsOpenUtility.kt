/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.util

import android.app.Activity
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity
import com.newshunt.dhutil.helper.browser.NHBrowserUtil

/**
 * This utility class having logic to open ads in internal and external browsers.
 *
 * @author raunak.yadav
 */
object AdsOpenUtility {
    /**
     * check the useInternalBrowser attribute and select the browser to open url.
     *
     * @param url
     * @param baseDisplayAdEntity
     */
    @JvmStatic
    fun handleBrowserSelection(activity: Activity?, url: String?,
                               baseDisplayAdEntity: BaseDisplayAdEntity?) {
        baseDisplayAdEntity ?: return
        val browserType = baseDisplayAdEntity.useInternalBrowser
        val clearHistoryOnPageLoad = baseDisplayAdEntity.clearHistoryOnPageLoad ?: false
        NHBrowserUtil.handleBrowserSelection(activity, url, browserType, baseDisplayAdEntity
            .externalBrowsers, true, clearHistoryOnPageLoad)
    }
}