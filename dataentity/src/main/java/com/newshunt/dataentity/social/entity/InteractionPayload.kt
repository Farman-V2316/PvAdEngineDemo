/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

/**
 * api payload
 * @author satosh.dhanyamraju
 */
data class InteractionPayload(val items: List<InteractionPayloadItem>? = null,
                              val itemsDeleted: List<InteractionPayloadItem>? = null) {
    data class InteractionPayloadItem(
            val entityId: String,
            val entityType: String,
            val action: String,
            val actionTime: Long
    )
}