/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui



/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */


import androidx.lifecycle.ViewModel
import androidx.fragment.app.Fragment
import com.newshunt.dataentity.common.asset.Location
import com.newshunt.dataentity.common.asset.Locations


/**
 * @author priya.gupta
 * An interface created so that lower modules like news-home can interact with search module.
 */

interface UpdateLocationListInterface {
    fun showSearchedLocations(list: MutableList<Locations>)

    fun showErrorOnSearchFailure(errorTitle: String?, errorSubTitle: String?)

    fun userStartedTypingQuery()
}