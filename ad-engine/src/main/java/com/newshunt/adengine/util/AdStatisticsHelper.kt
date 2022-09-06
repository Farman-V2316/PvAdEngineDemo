/*
* Copyright (c) 2019 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.util

import com.newshunt.adengine.model.entity.AdStatistics
import com.newshunt.adengine.model.entity.version.AdPosition
import com.newshunt.news.analytics.NhAnalyticsAppState
import com.newshunt.common.helper.common.JsonUtils
import com.newshunt.common.helper.common.Logger
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.preference.AdsPreference

/**
 * Fetches, checks and updates ad statistics sent in requests.
 * Stats are kept for a day.
 *
 * @author raunak.yadav
 */
object AdStatisticsHelper {

    private const val TAG = "AdStatisticsHelper"
    val adStatistics: AdStatistics

    init {
        val currentTS = System.currentTimeMillis()
        val firstTs: Long = if (PreferenceManager.containsPreference(AdsPreference.ADS_STATS_FIRST_TS)) {
            PreferenceManager.getPreference(AdsPreference.ADS_STATS_FIRST_TS, currentTS)
        } else {
            PreferenceManager.savePreference(AdsPreference.ADS_STATS_FIRST_TS, currentTS)
            currentTS
        }

        val totalSessions = PreferenceManager.getPreference(AdsPreference.ADS_STATS_TOTAL_SESSIONS, 0)
        val totalAdSessions = PreferenceManager.getPreference(AdsPreference.ADS_STATS_TOTAL_ADS_SESSIONS, 0)
        val totalSeenAds = PreferenceManager.getPreference(AdsPreference.ADS_STATS_TOTAL_ADS, 0)
        val totalSeenMastHeadAds = PreferenceManager.getPreference(AdsPreference.ADS_STATS_TOTAL_MASTHEAD_ADS, 0)

        //New session init
        adStatistics = AdStatistics(firstTs, totalSessions, totalAdSessions, totalSeenAds, totalSeenMastHeadAds)
        if (adStatistics.isExpired()) {
            resetStatsOnExpiry()
        }
        //TODO: PANDA removed
//        addNewSessionEntry(NHAnalyticsSession.getSessionId())
        Logger.d(TAG, "Initial stats : $adStatistics")
    }

    /**
     * Get stats as jsonString to pass with adRequest.
     * Refresh, if stale data.
     */
    fun getDataAsString(sessionId: String?): String? {
        if (adStatistics.isExpired()) {
            resetStatsOnExpiry()
        }
        if (isNewSession(sessionId)) {
            addNewSessionEntry(sessionId)
        }
        return JsonUtils.toJson(adStatistics)
    }

    private fun resetStatsOnExpiry() {
        Logger.d(TAG, "Reset ad stats for id : ${adStatistics.currentSessionId}")
        //Reset start time of today's ads data
        PreferenceManager.savePreference(AdsPreference.ADS_STATS_FIRST_TS, System.currentTimeMillis())

        //Remove other ad data
        PreferenceManager.remove(AdsPreference.ADS_STATS_TOTAL_SESSIONS)
        PreferenceManager.remove(AdsPreference.ADS_STATS_TOTAL_ADS_SESSIONS)
        PreferenceManager.remove(AdsPreference.ADS_STATS_TOTAL_ADS)
        PreferenceManager.remove(AdsPreference.ADS_STATS_TOTAL_MASTHEAD_ADS)
        adStatistics.reset()
    }

    private fun isNewSession(newSessionId: String?): Boolean {
        return adStatistics.currentSessionId == null || newSessionId != adStatistics.currentSessionId
    }

    private fun addNewSessionEntry(sessionId: String?) {
        Logger.d(TAG, "New session started with id: $sessionId")
        adStatistics.currentSessionId = sessionId
        adStatistics.sessionSource = NhAnalyticsAppState.getInstance().sessionSource?.referrerName
        PreferenceManager.savePreference(AdsPreference.ADS_STATS_TOTAL_SESSIONS, ++adStatistics.totalSessions)
    }

    /**
     * Update stats and save them.
     */
    fun onAdViewed(adPosition: AdPosition) {
        if (adPosition == AdPosition.MASTHEAD) {
            PreferenceManager.savePreference(
                AdsPreference.ADS_STATS_TOTAL_MASTHEAD_ADS, ++adStatistics.mastheadAdCount)
        }
        PreferenceManager.savePreference(AdsPreference.ADS_STATS_TOTAL_ADS, ++adStatistics.totalSeenAds)

        if (!adStatistics.adsSeenInCurrentSession) {
            adStatistics.adsSeenInCurrentSession = true
            PreferenceManager.savePreference(AdsPreference.ADS_STATS_TOTAL_ADS_SESSIONS,
                ++adStatistics.totalAdSessions)
        }
        Logger.d(TAG, "onAdViewed $adPosition : $adStatistics")
    }

}