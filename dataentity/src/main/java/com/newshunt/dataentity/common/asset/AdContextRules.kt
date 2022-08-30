/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.common.asset;

import `in`.dailyhunt.money.adContextEvaluatorEngineNative.RuleGroup
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @author raunak.yadav
 */
data class AdContextRules(val ruleGroup: RuleGroup? = null,
                          val ttl: Long? = -1,
                          val cacheType: AdCacheType? = AdCacheType.SESSION,
                     // 0 being the lowest.
                          val selectionPriority: Int? = 0) : Serializable

enum class AdCacheType {
    @SerializedName("session")
    SESSION,
    @SerializedName("persist")
    PERSIST
}