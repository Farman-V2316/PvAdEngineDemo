/*
* Copyright (c) 2020 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity

import com.newshunt.dataentity.ads.AdFCType
import java.io.Serializable

/**
 * Event fired for FC exhausted campaigns.
 * To remove them from other places where inserted but not seen.
 *
 * @author raunak.yadav
 */
class AdFCLimitReachedEvent(val adId: String,
                            val capId: String,
                            val type: AdFCType) : Serializable