/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.helper

import androidx.lifecycle.MutableLiveData
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction

/**
 * @author umesh.isran
 */
object UserActionHelper {
    var userActionLiveData = MutableLiveData<NhAnalyticsUserAction>()
}


