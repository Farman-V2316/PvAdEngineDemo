package com.newshunt.dataentity.notification.asset

import com.google.gson.annotations.SerializedName

import java.io.Serializable

class EntityStreamData : Serializable {

    @SerializedName("d1")
    val row1: String? = null

    @SerializedName("d2")
    val row2: String? = null

    @SerializedName("d3")
    val row3: String? = null

    @SerializedName("x")
    val collapseValue: String? = null
}
