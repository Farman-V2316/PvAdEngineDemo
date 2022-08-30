/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.view

import com.newshunt.adengine.model.entity.BaseAdEntity

/**
 * Class that will use the producer to fetch ads for its consumption.
 *
 * @author raunak.yadav
 */
interface AdEntityConsumer {
    val excludeBannerIds: List<String>

    fun consumeNextSet(baseAdEntities: List<BaseAdEntity>?, fromNetwork: Boolean)

    fun doneProcessingRequest()
}