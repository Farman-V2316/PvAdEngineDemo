/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.notification
/**
 * Flush Nav Model. Used in flushing of notifications.
 * <p>
 * Created by shrikant.agrawal
 */
class FlushNavModel: BaseModel() {
}


data class TimeRange(val start:Long = 0L,
                     val end: Long = 0L)

enum class DeleteType {
    TIME_RANGE, POST_IDS, DEEPLINKS
}