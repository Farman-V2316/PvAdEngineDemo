package com.newshunt.dhutil.helper

import com.newshunt.common.helper.preference.PreferenceManager
import com.newshunt.dhutil.helper.preference.AppStatePreference

class SpecialChannelHelper {
    companion object {
        @JvmStatic
        fun addSpecialChannel(channelId: String) {
            val specialChannel = PreferenceManager.getPreference<MutableSet<String>>(
                    AppStatePreference.EXEMPTED_NOTIFICATION_CHANNELS, mutableSetOf())
            specialChannel.add(channelId)
            PreferenceManager.savePreference(AppStatePreference.EXEMPTED_NOTIFICATION_CHANNELS,
                    specialChannel)
        }
    }
}