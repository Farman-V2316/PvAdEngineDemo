/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.news.model.utils

import com.newshunt.dataentity.common.asset.AnyCard

/**
 * @author satosh.dhanyamraju
 */
interface TransformNewsList {
    fun transf(list: List<AnyCard>): List<AnyCard>
}