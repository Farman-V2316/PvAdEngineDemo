/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 * data class to represent Versioned API Update response. Could be used to update any versioned API
 * in the DB via notification flow in future.
 *
 * Created by srikanth.r on 12/3/21.
 */
data class SilentVersionedApiUpdateModel(@SerializedName("raw")
                                         val rawResponse: JsonObject,
                                         @SerializedName("v_api_type")
                                         val versionedApiType: String?,
                                         @SerializedName("v_api_version")
                                         val versionedApiVersion: String?) : BaseModel()