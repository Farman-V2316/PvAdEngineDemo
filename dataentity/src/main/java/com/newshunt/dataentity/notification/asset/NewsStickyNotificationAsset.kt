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
class NewsStickyNotificationAsset(var stickyItems: List<DeeplinkModel>?,
                                  val forceShow: Boolean = false,
                                  val refreshInterval: Long = -1,
                                  val channelId: String = Constants.EMPTY_STRING,
                                  val url: String = Constants.EMPTY_STRING,
                                  val metaUrl:String = Constants.EMPTY_STRING,
                                  val langFilter: String = Constants.EMPTY_STRING,
                                  val disableEvents: Boolean = false ): BaseNotificationAsset()