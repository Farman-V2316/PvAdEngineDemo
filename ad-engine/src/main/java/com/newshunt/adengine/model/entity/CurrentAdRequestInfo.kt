/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity

import com.newshunt.adengine.model.entity.version.AdRequest

/**
 * Meta for ads as requested by the current view.
 *
 * @author raunak.yadav
 */
data class CurrentAdRequestInfo(val uniqueRequestId: Int,
                                var adRequest: AdRequest,
                                var nwRequestInitiated: Boolean = false)