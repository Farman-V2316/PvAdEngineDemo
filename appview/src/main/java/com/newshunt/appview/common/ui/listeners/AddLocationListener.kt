/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.listeners

import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.pages.PageableTopicsEntity


/**
 * @author priya.gupta
 */
interface AddLocationListener {

    fun onLocationAdded(isAdded: Boolean, location: Location)
}


interface StateLocationClickListener {

    fun expandLocationList(location: Location)
}


interface LocationFollowClickListener {

    fun followed(isFollowed: Boolean,location :Location)
}

interface PageFollowClickListener {
    fun followedPage(isFollowed: Boolean,pageableTopicsEntity : PageableTopicsEntity)
}