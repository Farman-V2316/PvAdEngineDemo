/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification

/**
 * Created by kajal.kumari on 25/04/22.
 */
class InAppNotificationModel:BaseModel() {
    override fun getBaseModelType(): BaseModelType {
        return BaseModelType.IN_APP
    }
}