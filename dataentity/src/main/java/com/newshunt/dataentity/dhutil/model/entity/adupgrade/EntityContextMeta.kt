/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.dataentity.dhutil.model.entity.adupgrade

import com.newshunt.dataentity.social.entity.AdSpec
import java.io.Serializable

/**
 * @author raunak.yadav
 */
data class EntityContextMeta(val version: String? = null,
                             val entityContextMeta: List<EntityContext>? = null) : Serializable

data class EntityContext(val entityIds: List<String>? = null,
                         val adSpec: AdSpec? = null) : Serializable