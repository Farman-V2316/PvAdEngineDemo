/*
* Copyright (c) 2021 Newshunt. All rights reserved.
*/
package com.newshunt.app.helper

import android.os.Handler
import android.os.Looper
import com.newshunt.app.lptimer.AdsGenericBeaconServiceImpl
import com.newshunt.app.lptimer.TimeSpentOnLPExitReason
import com.newshunt.common.helper.common.Constants
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dataentity.common.helper.common.CommonUtils
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider
import com.newshunt.news.helper.VideoPlayBackTimer
import java.util.concurrent.TimeUnit

/**
 * @author Mukesh Yadav
 */
private const val TAG = "AdsTimeSpentOnLPHelper"
class AdsTimeSpentOnLPHelper {
    var isAdsOnLPTimerStarted = false
    private var exitReason: TimeSpentOnLPExitReason? = null
    private var handler = Handler(Looper.getMainLooper())
    private var playBackTimer = VideoPlayBackTimer()

    fun startAdsTimeSpentOnLPTimer(beaconUrl: String?) {
        if (!isAdsOnLPTimerStarted) {
            Logger.d(TAG, "start timer")
            isAdsOnLPTimerStarted = true
            PreferenceManager.saveBoolean(Constants.TIME_SPENT_ON_LP_TIMER_STARTED, true)
            val adsUpgadeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
            PreferenceManager.saveString(Constants.ADS_TS_ON_LP_URL, beaconUrl)
            PreferenceManager.saveString(Constants.ADS_TS_ON_LP_TIMESTAMP, System.currentTimeMillis().toString())
            handler.removeCallbacksAndMessages(null)
            startTimer()
            adsUpgadeInfo?.adLpTimespentTimeoutMS?.let {
                handler.postDelayed({
                    if (isAdsOnLPTimerStarted) {
                        Logger.d(TAG, "exit reason THRESHOLD_TIME")
                        exitReason = TimeSpentOnLPExitReason.THRESHOLD_TIME
                        stopAdsTimeSpentOnLPTimerAndTriggerEvent()
                    }
                }, it)
            }
        }
    }

    fun stopAdsTimeSpentOnLPTimerAndTriggerEvent() {
        if (isAdsOnLPTimerStarted) {
            Logger.d(TAG, "Stop timer")
            PreferenceManager.saveBoolean(Constants.TIME_SPENT_ON_LP_TIMER_STARTED, false)
            val beaconUrl = PreferenceManager.getString(Constants.ADS_TS_ON_LP_URL)
            PreferenceManager.saveString(Constants.ADS_TS_ON_LP_URL, Constants.EMPTY_STRING)
            PreferenceManager.saveString(Constants.ADS_TS_ON_LP_TIMESTAMP, Constants.EMPTY_STRING)

            handler.removeCallbacksAndMessages(null)

            if (exitReason == null) {
                exitReason = TimeSpentOnLPExitReason.BACK_NAVIGATION
            }

            pauseTimer()
            val adsTimespentValue = totalTime()
            if (adsTimespentValue > 0) {
                Logger.d(TAG, "Beacon Url for LP and Time: $beaconUrl :  $adsTimespentValue, exit reason : $exitReason")
                AdsGenericBeaconServiceImpl.triggerAdLPTimeSpentBeaconEvent(adsTimespentValue.toString(), beaconUrl, exitReason)
                resetTimer()
                isAdsOnLPTimerStarted = false
                exitReason = null
            }
        }
    }
    private fun startTimer() {
        Logger.d(TAG, ": ADS_TIMESPENT_TIMER_START ")
        playBackTimer.start()
    }

    private fun pauseTimer() {
        Logger.d(TAG, ": ADS_TIMESPENT_TIMER_PAUSE ")
        playBackTimer.stop()
    }

    private fun resetTimer() {
        Logger.d(TAG, ": ADS_TIMESPENT_TIMER_RESET ")
        playBackTimer.reset()
    }

    private fun totalTime(): Long {
        playBackTimer.stop()
        val total = playBackTimer.getTotalTime(TimeUnit.SECONDS)
        playBackTimer.reset()
        return total
    }

    companion object {

        //Triggering time spent on landing page beacon in next app launch if app is killed in the
        // previous session
        @JvmStatic
        fun triggerTimeSpentOnLP(exitReason: TimeSpentOnLPExitReason) {
            val adsUpgadeInfo = AdsUpgradeInfoProvider.getInstance().adsUpgradeInfo
            if (adsUpgadeInfo != null) {
                val adLpTimespentTimeoutMS = adsUpgadeInfo.adLpTimespentTimeoutMS
                adLpTimespentTimeoutMS ?: return
                val isLPTimerStarted = PreferenceManager.getBoolean(Constants.TIME_SPENT_ON_LP_TIMER_STARTED, false)
                val requestUrl = PreferenceManager.getString(
                    Constants.ADS_TS_ON_LP_URL,
                    Constants.EMPTY_STRING
                )
                var timeSpent: Long? = 0L

                val startTime = PreferenceManager.getString(
                    Constants.ADS_TS_ON_LP_TIMESTAMP,
                    Constants.EMPTY_STRING
                )
                val endTime = System.currentTimeMillis()

                timeSpent = if (startTime.isNotEmpty()) {
                    val diff = endTime - startTime.toLong()
                    if (diff < adLpTimespentTimeoutMS) {
                        diff
                    } else {
                        adLpTimespentTimeoutMS
                    }
                } else {
                    adLpTimespentTimeoutMS
                }
                timeSpent = if (timeSpent != null) {
                    TimeUnit.MILLISECONDS.toSeconds(timeSpent)
                } else {
                    0L
                }

                val requestPayload: MutableMap<String, String> = HashMap()
                requestPayload["timeSpent"] = timeSpent.toString()
                requestPayload["exitReason"] = exitReason.toString()

                if (isLPTimerStarted && !CommonUtils.isEmpty(requestUrl)) {
                    Logger.d(TAG, "Beacon Url for LP and Time: $requestUrl :  $timeSpent, exit reason : $exitReason")
                    AdsGenericBeaconServiceImpl.updateAdsGenericBeacon(requestUrl, requestPayload)
                    PreferenceManager.saveBoolean(Constants.TIME_SPENT_ON_LP_TIMER_STARTED, false)
                    PreferenceManager.saveString(Constants.ADS_TS_ON_LP_URL, Constants.EMPTY_STRING)
                    PreferenceManager.saveString(Constants.ADS_TS_ON_LP_TIMESTAMP, Constants.EMPTY_STRING)
                }
            }
        }

        @JvmStatic
        fun triggerTimeSpentOnLpForContentBoostedAd(beaconUrl: String?, timeSpent: Long, exitReason: String) {
            val requestPayload: MutableMap<String, String> = HashMap()
            val timeInSec = (timeSpent / 1000).toString()
            requestPayload["timeSpent"] = timeInSec
            requestPayload["exitReason"] = exitReason
            beaconUrl?.let {
                Logger.d(TAG, "Beacon Url for LP and Time: $beaconUrl :  $timeInSec, exit reason : $exitReason")
                AdsGenericBeaconServiceImpl.updateAdsGenericBeacon(beaconUrl, requestPayload)
            }
        }
    }
}
