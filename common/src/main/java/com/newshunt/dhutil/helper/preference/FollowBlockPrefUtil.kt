/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.preference

import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.follow.entity.*
import com.newshunt.dataentity.common.helper.common.CommonUtils
import java.util.concurrent.TimeUnit
/**
 * @author aman.roy
 * Preference Util class for follow and block suggestion for implicit and explicit signals.
 */
object FollowBlockPrefUtil {
    fun getConfigFromLangType(followBlockConfigWrapper: FollowBlockConfigWrapper, lang:String): FollowBlockLangConfig? {
        return followBlockConfigWrapper.followBlockConfig?.find { it.langFilter?.contains(lang) == true }
    }
    fun getSoftFollowSignalInSession():Int {
        return PreferenceManager.getPreference(FollowBlockPreference.SOFT_FOLLOW_SIGNAL_THIS_SESSION,0)
    }
    fun updateSoftFollowPref(count:Int) {
        PreferenceManager.savePreference(FollowBlockPreference.SOFT_FOLLOW_SIGNAL_THIS_SESSION,count)
    }
    fun updateColdFollowPref(count:Int) {
        PreferenceManager.savePreference(FollowBlockPreference.COLD_FOLLOW_SIGNAL_THIS_SESSION,count)
    }

    fun updateMaxSessionPref(count:Int) {
        PreferenceManager.savePreference(FollowBlockPreference.NUMBER_OF_SESSIONS_COLD_SIGNAL,count)
    }

    fun resetSoftFollowPref() {
        updateSoftFollowPref(0)
    }

    fun getSoftBlockSignalInSession():Int {
        return PreferenceManager.getPreference(FollowBlockPreference.SOFT_BLOCK_SIGNAL_THIS_SESSION,0)
    }
    fun updateSoftBlockPref(count:Int) {
        PreferenceManager.savePreference(FollowBlockPreference.SOFT_BLOCK_SIGNAL_THIS_SESSION,count)
    }
    fun resetSoftBlockPref() {
        updateSoftBlockPref(0)
    }
    fun resetColdFollowPref() {
        updateColdFollowPref(0)
    }
    fun resetColdMaxSessionPref() {
        updateMaxSessionPref(0)
    }
    fun getMaxSessions():Int {
        return PreferenceManager.getPreference(FollowBlockPreference.NUMBER_OF_SESSIONS_COLD_SIGNAL,0)
    }

    fun getFollowSignalInLifetime():Int {
        return PreferenceManager.getPreference(FollowBlockPreference.MAX_FOLLOW_RECOMMENDATIONS_IN_LIFETIME,0)
    }

    fun getBlockSignalInLifetime():Int {
        return PreferenceManager.getPreference(FollowBlockPreference.MAX_BLOCK_RECOMMENDATIONS_IN_LIFETIME,0)
    }

    fun getColdFollowSignalInLifetime():Int {
        return PreferenceManager.getPreference(FollowBlockPreference.MAX_COLD_FOLLOW_RECOMMENDATIONS_IN_LIFETIME,0)
    }

    fun getExplicitFollowSignalInLifetime():Int {
        return PreferenceManager.getPreference(FollowBlockPreference.MAX_EXPLICIT_SIGNAL_FOLLOW_RECOMMENDATIONS_IN_LIFETIME,0)
    }
    fun getExplicitBlockSignalInLifetime():Int {
        return PreferenceManager.getPreference(FollowBlockPreference.MAX_EXPLICIT_SIGNAL_BLOCK_RECOMMENDATIONS_IN_LIFETIME,0)
    }

    fun updateFollowSignalInLifetime(count:Int = 1) {
        return PreferenceManager.savePreference(FollowBlockPreference.MAX_FOLLOW_RECOMMENDATIONS_IN_LIFETIME,
            getFollowSignalInLifetime()+1)
    }

    fun updateBlockSignalInLifetime(count:Int = 1) {
        return PreferenceManager.savePreference(FollowBlockPreference.MAX_BLOCK_RECOMMENDATIONS_IN_LIFETIME,
            getBlockSignalInLifetime() +1)
    }

    fun updateColdFollowSignalInLifetime(count: Int = 1) {
        PreferenceManager.savePreference(
            FollowBlockPreference.MAX_COLD_FOLLOW_RECOMMENDATIONS_IN_LIFETIME,
            getColdFollowSignalInLifetime() + count
        )
    }

    fun getImplicitFollowAbsoluteTimestamp():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.IMPLICIT_FOLLOW_ABSOLUTE_TIMESTAMP,0)
    }

    fun updateImplicitFollowAbsoluteTimestamp(timeStamp:Long) {
        PreferenceManager.savePreference(FollowBlockPreference.IMPLICIT_FOLLOW_ABSOLUTE_TIMESTAMP,timeStamp)
    }

    fun getImplicitFollowAbsoluteDay():Int {
        return (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-getImplicitFollowAbsoluteTimestamp()) /Constants.ONE_DAY_IN_SECONDS).toInt()
    }
    fun getImplicitBlockAbsoluteTimestamp():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.IMPLICIT_BLOCK_ABSOLUTE_TIMESTAMP,0)
    }

    fun getColdCooloffTimestamp(): Long {
        return PreferenceManager.getPreference(
            FollowBlockPreference.LAST_COLD_FOLLOW_ACTIVITY_TIMESTAMP,
            0
        )
    }

    fun updateColdFollowlastShownTimestamp(timeStamp: Long) {
        PreferenceManager.savePreference(
            FollowBlockPreference.LAST_COLD_FOLLOW_ACTIVITY_TIMESTAMP,
            timeStamp
        )
    }

    fun getExplicitBlocklastShownTimestamp():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.EXPLICIT_BLOCK_SHOW_TIMESTAMP,0)

    }
    fun getExplicitFollowlastShownTimestamp():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.EXPLICIT_FOLLOW_SHOW_TIMESTAMP,0)

    }
    fun setExplicitFollowlastShownTimestamp(timeStamp: Long) {
         PreferenceManager.savePreference(FollowBlockPreference.EXPLICIT_FOLLOW_SHOW_TIMESTAMP,timeStamp)
    }

    fun setExplicitBlocklastShownTimestamp(timeStamp: Long) {
        PreferenceManager.savePreference(FollowBlockPreference.EXPLICIT_BLOCK_SHOW_TIMESTAMP,timeStamp)
    }

    fun getExplicitFollowCooloffTimestamp():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.EXPLICIT_FOLLOW_COOLOFF_TIMESTAMP,0)
    }

    fun updateImplicitBlockAbsoluteTimestamp(timeStamp:Long) {
        PreferenceManager.savePreference(FollowBlockPreference.IMPLICIT_BLOCK_ABSOLUTE_TIMESTAMP,timeStamp)
    }


    fun getImplicitFollowActivityCount():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.IMPLICIT_FOLLOW_ACTIVITY_COUNT,0)
    }

    fun updateImplicitFollowActivityCount(timeStamp:Long) {
        PreferenceManager.savePreference(FollowBlockPreference.IMPLICIT_FOLLOW_ACTIVITY_COUNT,timeStamp)
    }

    fun getImplicitBlockActivityCount():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.IMPLICIT_BLOCK_ACTIVITY_COUNT,0)
    }

    fun updateImplicitBlockActivityCount(timeStamp:Long) {
        PreferenceManager.savePreference(FollowBlockPreference.IMPLICIT_BLOCK_ACTIVITY_COUNT,timeStamp)
    }

    fun getLastImplicitFollowActivityTimeStamp():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.LAST_IMPLICIT_FOLLOW_ACTIVITY_TIMESTAMP,0)
    }
    fun updateLastImplicitFollowActivityTimeStamp(timeStamp: Long) {
        PreferenceManager.savePreference(FollowBlockPreference.LAST_IMPLICIT_FOLLOW_ACTIVITY_TIMESTAMP,timeStamp)
    }

    fun getLastImplicitBlockActivityTimeStamp():Long {
        return PreferenceManager.getPreference(FollowBlockPreference.LAST_IMPLICIT_BLOCK_ACTIVITY_TIMESTAMP,0)
    }
    fun updateLastImplicitBlockActivityTimeStamp(timeStamp: Long) {
        PreferenceManager.savePreference(FollowBlockPreference.LAST_IMPLICIT_BLOCK_ACTIVITY_TIMESTAMP,timeStamp)
    }

    @JvmStatic
    fun onNewLaunch() {
        val currentTime = System.currentTimeMillis()
        if(getLastImplicitFollowActivityTimeStamp() != 0L) {
            if(CommonUtils.isAtleastNextXCalDay(getLastImplicitFollowActivityTimeStamp(),currentTime,1)) {
                updateImplicitFollowActivityCount(getImplicitFollowActivityCount()+1)
                updateLastImplicitFollowActivityTimeStamp(currentTime)
            }
        }
        if(getLastImplicitBlockActivityTimeStamp()!=0L) {
            if(CommonUtils.isAtleastNextXCalDay(getLastImplicitBlockActivityTimeStamp(),currentTime,1)) {
                updateImplicitBlockActivityCount(getImplicitBlockActivityCount()+1)
                updateLastImplicitBlockActivityTimeStamp(currentTime)
            }
        }
        
        resetSoftBlockPref()
        resetSoftFollowPref()
        resetColdFollowPref()
        if(getMaxSessions()!=0) {
            updateMaxSessionPref(getMaxSessions() + 1)
        }
    }

    fun isInImplicitFollowCoolOffPeriod(followConfig:Follow?):Boolean {
        val activityCount = getImplicitFollowActivityCount()
        val absoluteTimeStamp = getImplicitFollowAbsoluteTimestamp()
        if(absoluteTimeStamp == 0L && activityCount==0L) {
            return false
        }
        val absoluteTimeStampCoolOffLimitExceeded = CommonUtils.isAtleastNextXCalDay(absoluteTimeStamp,System.currentTimeMillis(),
            (followConfig?.coolOffAbsoluteDays ?: Constants.DEFAULT_IMPLICIT_ABSOLUTE_DAYS))
        return !absoluteTimeStampCoolOffLimitExceeded && activityCount < (followConfig?.coolOffActiveDays ?: Constants.DEFAULT_IMPLICIT_ACTIVITY_COUNT)

    }

    fun isInImplicitBlockCoolOffPeriod(blockConfig: Block?):Boolean {
        val activityCount = getImplicitBlockActivityCount()
        val absoluteTimeStamp = getImplicitBlockAbsoluteTimestamp()
        if(absoluteTimeStamp == 0L && activityCount==0L) {
            return false
        }
        val absoluteTimeStampCoolOffLimitExceeded = CommonUtils.isAtleastNextXCalDay(absoluteTimeStamp,System.currentTimeMillis(),
            (blockConfig?.coolOffAbsoluteDays ?: Constants.DEFAULT_IMPLICIT_ABSOLUTE_DAYS))
        return !absoluteTimeStampCoolOffLimitExceeded && activityCount < (blockConfig?.coolOffActiveDays ?: Constants.DEFAULT_IMPLICIT_ACTIVITY_COUNT)

    }

    fun isInColdCoolOffPeriod(followConfig: Follow?): Boolean {
        val absoluteTimeStamp = getColdCooloffTimestamp()
        if (absoluteTimeStamp == 0L) {
            return false
        }
        val currentTime = System.currentTimeMillis()

        val endTime = absoluteTimeStamp + TimeUnit.SECONDS.toMillis(
            ((followConfig?.minTimeGapInSeconds ?: 0).toLong())
        )

        return currentTime < endTime
    }

    fun isInCoolOffPeriodExplicitFollowSignal(followConfig: Follow?): Boolean {
        val absoluteTimeStamp = getExplicitFollowlastShownTimestamp()
        if (absoluteTimeStamp == 0L) {
            return false
        }

        val currentTime = System.currentTimeMillis()
        val endTime = absoluteTimeStamp + TimeUnit.SECONDS.toMillis(
            ((followConfig?.coolOffPeriodInSecs ?: 0).toLong())
        )
        return currentTime < endTime

    }

    fun isInCoolOffPeriodExplicitBlockSignal(blockConfig: Block?): Boolean {
        val absoluteTimeStamp = getExplicitBlocklastShownTimestamp()
        if (absoluteTimeStamp == 0L) {
            return false
        }

        val currentTime = System.currentTimeMillis()
        val endTime = absoluteTimeStamp + TimeUnit.SECONDS.toMillis(
            ((blockConfig?.coolOffPeriodInSecs ?: 0).toLong())
        )
        return currentTime < endTime
    }

    fun updateImplicitFollowShow(timeStamp:Long = System.currentTimeMillis()) {
        updateImplicitFollowAbsoluteTimestamp(timeStamp)
        updateLastImplicitFollowActivityTimeStamp(timeStamp)
        updateFollowSignalInLifetime()
    }

    fun incrementSoftFollowSessionCount() {
        updateSoftFollowPref(getSoftFollowSignalInSession() +1)
    }

    fun incrementSoftBlockSessionCount() {
        updateSoftBlockPref(getSoftBlockSignalInSession()+1)
    }

    fun updateImplicitBlockShow(timeStamp:Long = System.currentTimeMillis()) {
        updateImplicitBlockAbsoluteTimestamp(timeStamp)
        updateLastImplicitBlockActivityTimeStamp(timeStamp)
        updateBlockSignalInLifetime()
    }

    fun updateColdSignalFollowCarouselShow(timeStamp:Long = System.currentTimeMillis()) {
          updateColdFollowSignalInLifetime()
          updateColdFollowlastShownTimestamp(timeStamp)
          updateColdFollowPref(1)
          updateMaxSessionPref(1)
    }

    fun isFollowBlockUpdateFromImplicitSignal():Boolean {
        return PreferenceManager.getPreference(FollowBlockPreference.IS_IMPLICIT_FOLLOW_BLOCK_TRIGGER,false)
    }

    fun updateFollowBlockUpdateFromImplicitSignal(flag:Boolean) {
        return PreferenceManager.savePreference(FollowBlockPreference.IS_IMPLICIT_FOLLOW_BLOCK_TRIGGER,flag)
    }
}