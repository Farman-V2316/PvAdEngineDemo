/*
 * Created by Rahul Ravindran at 10/10/19 1:36 PM
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */

package com.newshunt.notification.model.entity.server

import com.newshunt.dataentity.notification.BaseInfo
import java.io.Serializable

data class CreatePostBaseInfo(val progress: Double, val cpId: Long, val notificationId: Int,
                              val isImageAttached: Boolean) : BaseInfo(), Serializable