/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity

import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.dataentity.common.helper.common.AdLiveDataEvent
import java.io.Serializable

/**
 * Event fired for viewed Ads.
 * To remove them from other places where inserted but not seen.
 *
 * @author raunak.yadav
 */
class AdViewedEvent(val adId: String,
                    val viewedParentId: Int,
                    val parentIds: MutableSet<Int>?,
                    val adPosition: AdPosition,
                    val adTag: String? = null,
                    val entityId: String? = null) : Serializable, AdLiveDataEvent {

    companion object {
        private const val serialVersionUID = 2766303196966537213L
    }
}