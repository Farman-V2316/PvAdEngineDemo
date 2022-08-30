/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity.version

import `in`.dailyhunt.money.contentContext.ContentContext
import android.app.Activity
import androidx.core.app.NotificationManagerCompat
import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents information to be given while requesting for an ad.
 *
 * @author raunak.yadav
 */
data class AdRequest(
        val zoneType: AdPosition,
        var numOfAds: Int = 1,
        var retryCount: Int = 0,

        val entityId: String? = null,
        val entityType: String? = null,
        val entitySubType: String? = null,
        val postId: String? = null,
        val sourceId: String? = null,
        val sourceCatId: String? = null,
        val sourceType: String? = null,

        // Maps because supplement slots may have different contexts but are a single request.
        val parentContextMap: Map<String, ContentContext>? = null,
        val contentContextMap: Map<String, ContentContext>? = null,

        val section: String? = null,
        val groupKey: String? = null,
        val pageReferrer: PageReferrer? = null,
        val referrerId: String? = null,
        val buzzSource: String? = null,
        val isHome: Boolean = false,

        val localRequestedAdTags: MutableList<String>? = null,
        var requiredAdtags: ConcurrentHashMap<String, Int>? = null,
        val tag: String? = null,
        val isPrefetch: Boolean = false,

        //New Params for content targeting Instream Ad
        val dhtvAdParams: Map<String, String>? = null,
        val adExtras: String? = null,
        //Temp flag to allow Instream ads to skip common cache.
        val skipCacheMatching: Boolean = false,
        var amazonSdkPayload: AmazonSdkPayload? = null,
        val isNotificationEnabled: Boolean = NotificationManagerCompat.from(CommonUtils.getApplication()).areNotificationsEnabled(),
        val activity: Activity? = null,
        var adsBaseUrl: String? = null) {

        fun copyWith(localRequestedAdTags: MutableList<String>?): AdRequest {
                return localRequestedAdTags?.let {
                        this.copy(localRequestedAdTags = mutableListOf<String>().also {
                                it.addAll(localRequestedAdTags)
                        })
                } ?: this
        }
}