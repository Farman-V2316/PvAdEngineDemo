/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity

import com.newshunt.adengine.model.AdInteraction
import java.io.Serializable

/**
 * Event posted on ad close. This may lead to app exit if zone is splash-exit.
 *
 * @author raunak.yadav
 */
data class AdExitEvent(val ad: BaseAdEntity,
                       val adInteraction: AdInteraction? = null) : Serializable