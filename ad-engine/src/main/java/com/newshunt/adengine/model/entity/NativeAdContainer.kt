/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity

import com.newshunt.adengine.model.entity.version.AdPosition

/**
 * Wrapper for list of ads so that it can be sent across event bus.
 *
 * @author raunak.yadav
 */
data class NativeAdContainer(var uniqueRequestId: Int,
                             var adPosition: AdPosition,
                             var baseAdEntities: List<BaseAdEntity>? = null,
                             var doneRequestProcessing: Boolean = false,
                             val serverError: Boolean = false)