/*
 * Copyright (c) 2020 Newshunt. All rights reserved.
 */
package com.newshunt.notification.helper

import android.annotation.SuppressLint
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import com.newshunt.common.helper.common.Logger
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.notification.model.entity.WorkManagerQueueEntry
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Mukesh Yadav
 *
 *  A clas for scheduling the pull notifications work request and cancelling them.
 * */
object DHWorkManager {
    private const val TAG: String = "DHWorkManager"
    private var workManager: WorkManager? = null
    var donNotSchedule: AtomicBoolean = AtomicBoolean(true);
    var jobsQueue : MutableList<WorkManagerQueueEntry>? = null
    var idIndexMapping: MutableMap<String, Int>? = null

    @JvmStatic
    fun init() {
        Logger.d(TAG, "init: ")
        jobsQueue = mutableListOf()
        idIndexMapping = mutableMapOf()
    }

    @JvmStatic
    private fun initWorkManagerInstance(){
        if(workManager == null){
            Logger.d(TAG, "Initializing workmanager")
            workManager = WorkManager.getInstance(CommonUtils.getApplication())
        }
    }

    @SuppressLint("RestrictedApi")
    private fun addToQueue(queueEntry: WorkManagerQueueEntry){
        CommonUtils.runInBackGroundSerially {
            if(queueEntry.shouldReplaceExistingWork || !queueEntry.name.isNullOrEmpty()){
                var id = queueEntry.workRequest.tags.elementAt(0)
                if(!queueEntry.name.isNullOrEmpty()){
                    id = queueEntry.name
                }
                removeEntryFromQueue(id)
            }
            jobsQueue?.add(queueEntry)
            val index = (jobsQueue?.size?:0) - 1
            if(queueEntry.name.isNullOrEmpty()){
                Logger.d(TAG, "work added tag is ${queueEntry.workRequest.tags.elementAt(0)} index is $index")
                idIndexMapping?.put(queueEntry.workRequest.tags.elementAt(0), index)
            }else{
                Logger.d(TAG, "work added tag is ${queueEntry.name} index is $index")
                idIndexMapping?.put(queueEntry.name, index)
            }
        }

    }

    private fun removeEntryFromQueue(id: String?) {
        id?:return
        var index = -1
        index = idIndexMapping?.get(id)?:-1
        Logger.d(TAG, "removing job $id from queue index is $index")
        if(index < jobsQueue?.size?:-1 && index >= 0){
            jobsQueue?.removeAt(index)
            idIndexMapping?.remove(id)
        }
    }

    @SuppressLint("RestrictedApi")
    @JvmStatic
    fun scheduleAllQueuedEntries(){
        if(workManager == null){
            initWorkManagerInstance()
        }
        CommonUtils.runInBackGroundSerially {
            donNotSchedule.set(false)
            jobsQueue?.let{
                for(entry in it){
                    if(entry.name.isNullOrEmpty() && entry.isOneTimeWorkRequest){
                        Logger.d(TAG, "begining from scheduled queue ${entry.workRequest.tags.elementAt(0)}")
                        beginWork((entry.workRequest as OneTimeWorkRequest), entry.shouldReplaceExistingWork)
                    }else if(!entry.name.isNullOrEmpty() && entry.isOneTimeWorkRequest){
                        Logger.d(TAG, "begining from scheduled queue ${entry.name}")
                        beginUniqueWork((entry.workRequest as OneTimeWorkRequest), entry.name, entry.workPolicy)
                    }else{

                    }
                }
                it.clear()
                idIndexMapping?.clear()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @JvmStatic
    fun beginWork(workRequest: OneTimeWorkRequest, shouldReplaceExistingWork: Boolean = false) {
        if(donNotSchedule.get()){
            addToQueue(WorkManagerQueueEntry(workRequest = workRequest, shouldReplaceExistingWork = shouldReplaceExistingWork))
            Logger.d(TAG, "Adding to jobsQueue will be scheduled later")
            return
        }
        CommonUtils.runInBackGroundSerially {
            initWorkManagerInstance()
            if(CommonUtils.workManagerInitFailed) return@runInBackGroundSerially
            Logger.d(TAG, "beginWork: #${workManager.hashCode()}")
            if (shouldReplaceExistingWork) {
                cancelWork(workRequest.tags.elementAt(0))
            }
            workManager?.enqueue(workRequest)
        }
    }

    @JvmStatic
    fun beginUniqueWork(workRequest: OneTimeWorkRequest, name: String, workPolicy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP) {
        if(donNotSchedule.get()){
            addToQueue(WorkManagerQueueEntry(workRequest = workRequest, name = name, workPolicy = workPolicy))
            Logger.d(TAG, "Adding uniqueWork to jobsQueue will be scheduled later name is $name")
            return
        }
        CommonUtils.runInBackGroundSerially {
            initWorkManagerInstance()
            if(CommonUtils.workManagerInitFailed) return@runInBackGroundSerially
            Logger.d(TAG, "beginUniqueWork: name is $name")
            workManager?.enqueueUniqueWork(name, workPolicy, workRequest)
        }

    }

    @JvmStatic
    fun cancelWork(tag: String) {
        if(donNotSchedule.get()){
            CommonUtils.runInBackGroundSerially {
                removeEntryFromQueue(tag)
            }
            return
        }
        initWorkManagerInstance()
        if(CommonUtils.workManagerInitFailed) return
        Logger.d(TAG, "cancelWork: tag is:- ${tag}")
        workManager?.cancelAllWorkByTag(tag)

    }

    @JvmStatic
    fun cancelUniqueWork(uniqueName: String) {
        if(donNotSchedule.get()){
            CommonUtils.runInBackGroundSerially {
                removeEntryFromQueue(uniqueName)
            }
            return
        }
        initWorkManagerInstance()
        if(CommonUtils.workManagerInitFailed) return
        Logger.d(TAG, "cancelUniqueWork: name is: ${uniqueName}")
        workManager?.cancelUniqueWork(uniqueName)
    }

    @JvmStatic
    fun beginWith(workRequestList: List<OneTimeWorkRequest>): WorkContinuation? {
        if(workManager == null){
            initWorkManagerInstance()
            scheduleAllQueuedEntries()
        }
        if (CommonUtils.workManagerInitFailed) return null
        return workManager?.beginWith(workRequestList)
    }
}
