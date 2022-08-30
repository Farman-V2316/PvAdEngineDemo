/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.usecase

import java.io.Serializable

/**
 * Used for communicating cards-response between usecases
 *
 * @author satosh.dhanyamraju
 */
data class NLResponseWrapper(val reqUrl: String,
                             val nlResp: NLResp,
                             val key: String): Serializable