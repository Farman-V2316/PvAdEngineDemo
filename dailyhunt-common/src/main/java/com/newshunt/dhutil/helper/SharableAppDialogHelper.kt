/*
 * Copyright (c) 2022 NewsHunt. All rights reserved.
 */
package com.newshunt.dhutil.helper

import com.newshunt.common.helper.preference.AppUserPreferenceUtils
import com.newshunt.common.helper.preference.GenericAppStatePreference
import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.preference.AppStatePreference
import java.util.concurrent.TimeUnit

/**
 * Created by kajal.kumari on 24/05/22.
 */
class SharableAppDialogHelper {

    companion object {
        private const val MAX_LINKEDIN_SHARE_SHOW_COUNT = 5
        private const val MAX_WAIT_DAYS_FOR_NEW_USERS = 30
        private const val MAX_WAIT_DAYS_FOR_UPGRADED_USERS = 7
        private const val MIN_LAUNCHES_FOR_UPGRADED_USERS = 14
        private const val MIN_DAYS_AFTER_LAST_SEEN = 30

        fun canShowLinkedInShareDialog(): Boolean {
            return !isLinkedinShareShowMaxCountReached() && (isFirstTimeToShowLinkedInShare() || canShowAfterAppUsage())
        }


        private fun isFirstTimeToShowLinkedInShare(): Boolean {
            if (getLinkedInShareShowCount() > 0) {
                return false
            }
            val currentDays = TimeUnit.MILLISECONDS.toDays(
                System.currentTimeMillis() - getFirstLaunchOrLatestUpgradeTime()).toInt()

            if(PreferenceManager.getPreference(AppStatePreference.IS_NEW_INSTALL,false) && currentDays >= getMinDaysToShowLinkedInForNewUsers()) {
                return true
            } else if(!PreferenceManager.getPreference(AppStatePreference.IS_NEW_INSTALL,false) && AppUserPreferenceUtils.getLastAppVersion() >= 2822 && (currentDays >= getMaxDaysToShowLinkedInForUpgradedUsers() || getAppLaunchCountForLinkedPrompt() >= getMinLaunchesToShowLinkedInForUpgradedUsers())) {
                return true
            }
            return false
        }

        private fun getLinkedInShareShownTime(): Long {
            return PreferenceManager.getPreference(AppStatePreference.LINKEDIN_SHOWN_TIME, 0L)
        }

        fun setLinkedInShareShownTime(linkedInShareShownTime: Long) {
            PreferenceManager.savePreference(AppStatePreference.LINKEDIN_SHOWN_TIME, linkedInShareShownTime)
        }

        private fun canShowAfterAppUsage(): Boolean {
            if(getLinkedInShareShownTime() == 0L) {
                return false
            }
            val currentDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - getLinkedInShareShownTime()).toInt()
            return getLinkedInShareShownTime() != 0L && (currentDays >= getMinDaysAfterLinkedInLastSeen())
        }

        private fun getFirstLaunchOrLatestUpgradeTime(): Long{
            return PreferenceManager.getPreference(AppStatePreference.LINKEDIN_FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME,0L)
        }

        fun linkedInFirstLaunchOrLatestUpgradeTime() {
            val firstOrUpgradeTime = PreferenceManager.getPreference(AppStatePreference.LINKEDIN_FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME, 0L)
            if (firstOrUpgradeTime == 0L) {
                PreferenceManager.savePreference(AppStatePreference.LINKEDIN_FIRST_LAUNCH_OR_LATEST_UPGRADE_TIME, System.currentTimeMillis())
            }
        }

        private fun getMinDaysToShowLinkedInForNewUsers(): Int {
            return PreferenceManager.getPreference(AppStatePreference.LINKEDIN_MIN_WAIT_DAYS_NEW_USERS, MAX_WAIT_DAYS_FOR_NEW_USERS)
        }

        private fun getMaxDaysToShowLinkedInForUpgradedUsers(): Int {
            return PreferenceManager.getPreference(AppStatePreference.LINKEDIN_MIN_WAIT_DAYS_UPGRADED_USERS, MAX_WAIT_DAYS_FOR_UPGRADED_USERS)
        }

        private fun getMinLaunchesToShowLinkedInForUpgradedUsers(): Int {
            return PreferenceManager.getPreference(AppStatePreference.LINKEDIN_MIN_LAUNCHES_UPGRADED_USERS, MIN_LAUNCHES_FOR_UPGRADED_USERS)
        }


        private fun getMinDaysAfterLinkedInLastSeen(): Int {
            return PreferenceManager.getPreference(AppStatePreference.LINKEDIN_MIN_DAYS_AFTER_LAST_SEEN, MIN_DAYS_AFTER_LAST_SEEN)
        }

        private fun isLinkedinShareShowMaxCountReached(): Boolean {
            if(getLinkedInShareShowCount() >= getMaxLinkedInDialogShowCount()) {
                return true
            }
            return false
        }

        fun getLinkedInShareShowCount(): Int {
            return PreferenceManager.getPreference(AppStatePreference.LINKEDIN_SHARE_SHOWN_COUNT, 0)
        }

        fun setLinkedInShareShowCount(count: Int) {
            PreferenceManager.savePreference(AppStatePreference.LINKEDIN_SHARE_SHOWN_COUNT, count)
        }

        private fun getMaxLinkedInDialogShowCount(): Int {
            return PreferenceManager.getPreference(AppStatePreference.LINKEDIN_SHARE_MAX_TIMES_SHOW, MAX_LINKEDIN_SHARE_SHOW_COUNT)
        }

        fun incrementAppLaunchCountForLinkedIn() {
            var launchCount = getAppLaunchCountForLinkedPrompt()
            launchCount++
            setAppLaunchCountForLinkedPrompt(launchCount)
        }

        private fun getAppLaunchCountForLinkedPrompt(): Int {
            return PreferenceManager.getPreference(GenericAppStatePreference.APP_LAUNCH_COUNT_FOR_LINKEDIN_PROMPT, 0)
        }

        private fun setAppLaunchCountForLinkedPrompt(count: Int) {
            PreferenceManager.savePreference(GenericAppStatePreference.APP_LAUNCH_COUNT_FOR_LINKEDIN_PROMPT, count)
        }
    }
}