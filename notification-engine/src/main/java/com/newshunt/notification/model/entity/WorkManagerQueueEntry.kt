/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.notification.model.entity

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkRequest

/**
 * @author Atul Anand
 *
 *  A pojo to contain information regarding workManager jobs
 * */
data class WorkManagerQueueEntry(val workRequest: WorkRequest, val name: String? = null, val workPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP, val shouldReplaceExistingWork: Boolean = false, val isOneTimeWorkRequest: Boolean = true) {
}