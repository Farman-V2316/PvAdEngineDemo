/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity


import android.graphics.drawable.Drawable

import java.io.Serializable

/**
 * Basic meta for rendering Native ads
 *
 * @author raunak.yadav
 */
class NativeData : Serializable {
    var title: String? = null
    var body: String? = null
    var shortInfo: String? = null
    var ctaText: String? = null
    var sourceAlphabet: String? = null
    var sponsoredText: String? = null
    var iconUrl: String? = null
    var wideImageUrl: String? = null
    var category: String? = null
    var advertiser: String? = null
    var titleColor: String? = null
    var backgroundColor: String? = null
    var wideImageDrawable: Drawable? = null
    var iconDrawable: Drawable? = null
    var videoTagUrl: String? = null
    var showPlayIcon: Boolean = false

    companion object {
        private const val serialVersionUID = -73131986822299890L
    }
}