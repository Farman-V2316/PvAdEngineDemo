/*
 *  * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

/**
 * Created by kajal.kumari on 21/04/22.
 */
data class InAppTemplateInfo(val id: String,
                             val dayTextColor: String?= null,
                             val dayBgColor: String?= null,
                             val darkTextColor: String?= null,
                             val darkBgColor: String?= null,
                             val themeControl: String?= null )

data class InAppTemplateResponse(val version: String = "0",
                                 val rows: List<InAppTemplateInfo>?)