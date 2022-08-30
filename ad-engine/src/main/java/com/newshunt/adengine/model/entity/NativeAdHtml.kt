/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.model.entity

import com.MASTAdView.core.AdData
import com.google.gson.annotations.SerializedName

import java.io.Serializable

/**
 * Represents ad of html type served by ad-server.
 *
 * @author heena.arora
 */
class NativeAdHtml : BaseDisplayAdEntity() {
    var coolAd: CoolAd? = null

    var mastAdViewData: AdData? = null

    /**
     * Provides content of the CoolAd. Its type , metaInfo etc.
     *
     * @author heena.arora
     */

    class CoolAd : Serializable {

        var type: String? = null
        @SerializedName("autoexpand-timer")
        var autoexpandTimer = 0
        @SerializedName("autoexpand-min-swipes")
        var autoexpandMinSwipes = 0
        var actmethod: String? = null
        @SerializedName("expandbackground")
        var expandBackground: String? = null
        var zipped: Boolean = false
        var meta: String? = null
        var tracker: TrackerTag? = null
        var content: CoolAdContent? = null
        var richContent: String? = null
        var title: String? = null
        var titleColor: String? = null
        var titleBgColor: String? = null
        var tapToEng: String? = null
        var tapToEngColor: String? = null
        var tapToEngBgColor: String? = null
        var isVideoAd: Boolean = false

        var zipsubcontent: ZipSubContentTag? = null

        companion object {
            private const val serialVersionUID = -2860853997149959755L
        }
    }

    /**
     * Provides attributes and content of CoolAd content tag.
     *
     * @author heena.arora
     */
    class CoolAdContent : Serializable {

        @SerializedName("mainfile")
        var mainFile: String? = null
        var unzippedPath: String? = null //todo mukesh json check
        var link: Boolean? = false
        var data: String? = null

        companion object {
            private const val serialVersionUID = -8434579341297740883L
        }
    }

    /**
     * Provides attributes and content of zipSubcontent in CoolAd tag. Its name , zipped or not.
     *
     * @author heena.arora
     */
    class ZipSubContentTag : Serializable {

        var name: String? = null
        var link: Boolean = false
        var zipped: Boolean = false
        var data: String? = ""

        companion object {

            private const val serialVersionUID = -2968219445903125356L
        }
    }

    class TrackerTag : Serializable {
        var redirectWebUrl: Boolean = false
        var data: String? = null //trackerUrl

        companion object {
            private const val serialVersionUID = -389653197591298484L
        }
    }

    companion object {

        private const val serialVersionUID = 8171206677689321262L
    }
}
