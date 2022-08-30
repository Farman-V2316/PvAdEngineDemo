/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.appview.common.ui.helper

import androidx.lifecycle.MutableLiveData
import com.newshunt.dhutil.RepostLiveEvent

/**
 * to be used like [NavigationHelper] for card related events.
 * @author satosh.dhanyamraju
 */
object CardClickEventHelper {
    val reposts = MutableLiveData<RepostLiveEvent>()
}