package com.newshunt.adengine.other.model.entity

import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingWorkPolicy.KEEP
import androidx.work.WorkRequest

/**
 * @author Atul Anand
 *
 *  A pojo to contain information regarding workManager jobs
 * */
data class WorkManagerQueueEntry(val workRequest: WorkRequest, val name: String? = null, val workPolicy: ExistingWorkPolicy = KEEP, val shouldReplaceExistingWork: Boolean = false, val isOneTimeWorkRequest: Boolean = true) {
}