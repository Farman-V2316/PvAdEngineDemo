package com.newshunt.adengine.model.entity

import com.google.gson.annotations.SerializedName
import com.newshunt.adengine.model.entity.version.ShareIconPosition
import java.io.Serializable

class Shareability : Serializable {
    var text: String? = null
    var image: String? = null
    var subject: String? = null
    @SerializedName("pgiIconPosition")
    var shareIconPosition: ShareIconPosition? = null

    companion object {
        private const val serialVersionUID = -2359298874318543274L
    }
}