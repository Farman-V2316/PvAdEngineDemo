/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.notification.InAppTemplateInfo
import com.newshunt.dataentity.notification.InAppTemplateResponse

/**
 * Created by kajal.kumari on 04/05/22.
 */
object NotificationTemplateHelper {

    private const val GREY = "#424242"
    private const val WHITE = "#FFFFFF"
    private const val DARK_GREY = "#EEEEEE"
    private const val DARK_BLUE = "#50AFCD"
    private const val FILTER_BLUE = "#1F9EE1"

    @JvmStatic
    fun getDefaultColorTemplate(): InAppTemplateResponse {
        val rows = mutableListOf<InAppTemplateInfo>()
        val inAppTemplateInfoText =
            InAppTemplateInfo("defaultText", GREY, WHITE, DARK_GREY, GREY)
        val inAppTemplateInfoCta =
            InAppTemplateInfo("defaultCta", WHITE, FILTER_BLUE, WHITE, DARK_BLUE)
        rows.add(inAppTemplateInfoText)
        rows.add(inAppTemplateInfoCta)
        return InAppTemplateResponse(Constants.EMPTY_STRING, rows)
    }
}