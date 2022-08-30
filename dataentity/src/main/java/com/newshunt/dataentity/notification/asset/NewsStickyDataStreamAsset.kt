/*
 *
 *  * Copyright (c) 2021 Newshunt. All rights reserved.
 *
 */
package com.newshunt.dataentity.notification.asset

import com.newshunt.common.helper.common.Constants
import com.newshunt.dataentity.notification.DeeplinkModel

/**
 * @author  atul.anand on 23/10/21.
 */
class NewsStickyDataStreamAsset(val stickyItems: List<DeeplinkModel>?,
                                val forceShow: Boolean = false,
                                val startTime: Long = -1,
                                val expiryTime: Long = -1,
                                val priority: Int,
                                val url: String = Constants.EMPTY_STRING,
                                val metaUrl: String = Constants.EMPTY_STRING,
                                val refreshInterval: Long = 0,
                                val channelId:String,
                                val disableEvents: Boolean = false): BaseDataStreamAsset()