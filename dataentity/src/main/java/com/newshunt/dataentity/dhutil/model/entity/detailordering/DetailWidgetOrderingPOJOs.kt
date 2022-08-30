package com.newshunt.dataentity.dhutil.model.entity.detailordering

import com.newshunt.common.helper.common.Constants

/**
 * Created by karthik.r on 05/06/20.
 */
data class DetailWidgetOrderingResponse(val version: String,
                                        val rules: Map<String, List<String>> = HashMap(),
                                        val actionBarVariation: PostDetailActionbarVariation? = PostDetailActionbarVariation.TOP,
                                        val userSeg: Map<String, String>? = emptyMap()) {
    constructor() : this(Constants.EMPTY_STRING, HashMap())
}
