/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import com.newshunt.dataentity.common.asset.AnyCard
import com.newshunt.dataentity.common.model.entity.model.MultiValueResponse

/**
 * Card list response
 * @author satosh.dhanyamraju
 */
class NLResp
@JvmOverloads constructor(var isFromNetwork: Boolean = false,
                          val entityCount: Long = 0L,
                          var timeTakenToFetch: Long = 0L) : MultiValueResponse<AnyCard>()