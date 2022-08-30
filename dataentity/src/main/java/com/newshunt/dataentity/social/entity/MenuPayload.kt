/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.social.entity

import com.newshunt.dataentity.common.asset.Format
import com.newshunt.dataentity.common.asset.SubFormat
import com.newshunt.dataentity.common.asset.UiType2
import java.io.Serializable

/**
 * Post payload.
 * @author satosh.dhanyamraju
 */
data class MenuPayload(
        val itemId: String,
        val format: Format,
        val subFormat: SubFormat,
        val uiType: UiType2, //   for older cached stories
        val sourceId: String? = null,
        val displayLocation: MenuLocation,
        val option: String,
        val l1: MenuL1? = null,
        val eventParam: String? = null): Serializable

data class MenuPayLoad2(
        val userId: String? = null,
        val groupId: String? = null,
        val option: String,
        val eventParam: String? = null): Serializable
