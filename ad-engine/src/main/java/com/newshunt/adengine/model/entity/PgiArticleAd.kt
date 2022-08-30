/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity

import java.io.Serializable

/**
 * Represents pgi article ad served by ad-server.
 *
 * @author raunak.yadav
 */
class PgiArticleAd : BaseDisplayAdEntity() {
    var tracker: TrackerTag? = null

    class TrackerTag : Serializable {
        var redirectWebUrl: String? = null
        var data: String? = null //trackerUrl

        companion object {
            private const val serialVersionUID = -389653197591298484L
        }
    }
}
