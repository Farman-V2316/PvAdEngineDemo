/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.notification

import com.newshunt.dataentity.search.SearchPayloadContext
import java.io.Serializable

data class SearchNavModel(val query: String? = null,
                          val payload: SearchPayloadContext? = null,
                          val hint: String? = null,
                          val context:String?=null,
                          val presearch: Boolean = false) : BaseModel(), Serializable {
    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.SEARCH_MODEL
    }
}