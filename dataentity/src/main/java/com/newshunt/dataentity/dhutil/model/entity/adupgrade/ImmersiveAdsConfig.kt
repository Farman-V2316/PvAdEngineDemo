/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.adupgrade

import java.io.Serializable

data class ImmersiveAdsConfig(
        val immersiveTransitionSpan:Int = 0,
        val immersiveViewDistance: Int = 0,
        val companionRefreshTime: Int= 0): Serializable