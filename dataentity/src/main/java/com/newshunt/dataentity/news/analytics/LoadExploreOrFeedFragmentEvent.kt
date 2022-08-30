/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.news.analytics

import com.newshunt.dataentity.analytics.referrer.PageReferrer
import com.newshunt.dataentity.common.follow.entity.FollowNavigationType
import com.newshunt.dataentity.news.model.entity.PageType
import java.io.Serializable

/**
 * @author anshul.jain
 */

data class FollowTabLandingInfoEvent
@JvmOverloads
constructor(val pageType: PageType, val navigationType:
FollowNavigationType? = null, val pageReferrer: PageReferrer? = null,
            val promotionId: String? = null) : Serializable