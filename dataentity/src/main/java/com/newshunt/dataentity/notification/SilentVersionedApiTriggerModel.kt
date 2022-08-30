/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

import com.google.gson.annotations.SerializedName

/**
 * data class to represent Versioned API Update Trigger response. Could be used to trigger an update
 * to any versioned api
 *
 * Created by srikanth.r on 01/04/22.
 */
data class SilentVersionedApiTriggerModel(@SerializedName("v_api_version_map")
                                          val versionMap: Map<String, String>? = null) : BaseModel()