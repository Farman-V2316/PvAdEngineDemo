/*
* Copyright (c) 2022 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade

/**
 * @author raunak.yadav
 */
data class EvergreenAdsConfig(
    val enabled: Boolean = false,
    val endpoint: String? = null,
    // api not to be hit before this delay has passed (in seconds)
    val apiFetchDelay: Long? = 3600,
    val noOfAdsToProcess: Int? = 3,
    val substituteTimeout: SubstituteTimeout? = SubstituteTimeout(),
    val retryOnFailureAfterTimeInSec: Long? = 10L,
    val isRegUser: Boolean? = true
)

data class SubstituteTimeout(val regularUser: TimeOutValues? = TimeOutValues(500L, 1500L),
                             val thinUser: TimeOutValues? = TimeOutValues(0L, 500L))

data class TimeOutValues(val firstImpressionMS: Long?, val impressionMS: Long?)