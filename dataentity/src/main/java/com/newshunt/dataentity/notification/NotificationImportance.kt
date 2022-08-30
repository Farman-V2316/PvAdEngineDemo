/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

import java.io.Serializable
/**
 * Created by aman.roy on 03/11/2021.
 *  Class which holds object for defining importance of notification.
 */
data class NotificationImportance(val p:Int? = null,
                                val end:Long? = null) : Serializable