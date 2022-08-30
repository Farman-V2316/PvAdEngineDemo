/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.appview.common.ui.helper

import androidx.lifecycle.MutableLiveData

/**
 * A singleton to hold any generic livedata events to be posted and observed on
 * <p>
 * Created by srikanth.ramaswamy on 01/17/2020.
 */
object LiveDataEventHelper {
    //Livedata to observe new group or join group
    val newGroupLiveData: MutableLiveData<NewGroupEvent> = MutableLiveData()
}

/**
 * Event representing a newly created, edited group or newly joined group
 */
data class NewGroupEvent(val timestamp: Long,
                         val groupId: String)