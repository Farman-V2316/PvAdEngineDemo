/*
* Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.model.entity.omsdk

import com.google.gson.annotations.SerializedName

/**
 * @author raunak.yadav
 */
enum class OMTrackType {

    @SerializedName("none")
    NONE,
    @SerializedName("native")
    NATIVE,
    @SerializedName("web")
    WEB,
    @SerializedName("web_video")
    WEB_VIDEO;
}
