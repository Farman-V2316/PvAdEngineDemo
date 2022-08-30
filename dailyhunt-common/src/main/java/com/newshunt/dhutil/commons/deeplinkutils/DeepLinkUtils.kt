/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.dhutil.commons.deeplinkutils

import android.content.Context
import android.content.Intent
import com.newshunt.dataentity.analytics.referrer.PageReferrer

interface DeepLinkUtils {
    fun getTargetIntentFromUrl(url: String, context: Context, uniqueRequestId: Int,
                               pageReferrer: PageReferrer, skipHomeRouting: Boolean,
                               deepLinkUtilsCallback: DeepLinkUtilsCallback)
}

interface DeepLinkUtilsCallback {
    fun setDeeplinkTargetIntent(intent: Intent?)
}