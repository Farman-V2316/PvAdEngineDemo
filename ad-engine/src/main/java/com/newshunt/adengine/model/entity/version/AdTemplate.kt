/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity.version

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Indicates template type of the ad.
 *
 * @author raunak.yadav
 */
enum class AdTemplate(val value: String) : Serializable {
    @SerializedName("H")
    HIGH("H"),

    @SerializedName("L")
    LOW("L"),

    @SerializedName("E_H")
    ENHANCED_HIGH("E_H");
}