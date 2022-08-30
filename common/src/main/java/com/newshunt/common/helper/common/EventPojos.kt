/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common

import com.newshunt.dataentity.search.SearchPayloadContext

/**
 * data class to start SearchActivity
 * @author satosh.dhanymaraju
 */

data class LaunchSearch @JvmOverloads constructor(val searchContext: String = "",
                                                  val searchHint:String = "",
                                                  val referrer: Any?,
                                                  val searchPayloadContext: SearchPayloadContext? = null )