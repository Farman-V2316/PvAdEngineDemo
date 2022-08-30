/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.preference

import com.newshunt.common.helper.preference.PreferenceType
import com.newshunt.common.helper.preference.SavedPreference
/**
 * @author aman.roy
 * Preference class for follow and block suggestion for implicit and explicit signals.
 */
enum class FollowBlockPreference(val preferenceName:String,val type:PreferenceType):SavedPreference {
    SOFT_FOLLOW_SIGNAL_THIS_SESSION("soft_follow_signal_this_session",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    COLD_FOLLOW_SIGNAL_THIS_SESSION("cold_follow_signal_this_session",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    MAX_FOLLOW_RECOMMENDATIONS_IN_LIFETIME("max_follow_recommendations_in_lifetime",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    MAX_COLD_FOLLOW_RECOMMENDATIONS_IN_LIFETIME("max_cold_follow_recommendations_in_lifetime",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    MAX_EXPLICIT_SIGNAL_FOLLOW_RECOMMENDATIONS_IN_LIFETIME("max_explicit_follow_recommendations_in_lifetime",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    MAX_EXPLICIT_SIGNAL_BLOCK_RECOMMENDATIONS_IN_LIFETIME("max_explicit_block_recommendations_in_lifetime",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    SOFT_BLOCK_SIGNAL_THIS_SESSION("soft_block_signal_this_session",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    MAX_BLOCK_RECOMMENDATIONS_IN_LIFETIME("max_block_recommendations_in_lifetime",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    IMPLICIT_FOLLOW_ABSOLUTE_TIMESTAMP("implicit_follow_absolute_timestamp",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    IMPLICIT_BLOCK_ABSOLUTE_TIMESTAMP("implicit_block_absolute_timestamp",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    IMPLICIT_FOLLOW_ACTIVITY_COUNT("implicit_follow_activity_count",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    IMPLICIT_BLOCK_ACTIVITY_COUNT("implicit_block_activity_count",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    EXPLICIT_FOLLOW_COOLOFF_TIMESTAMP("explicit_block_cooloff_timestamp",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    EXPLICIT_FOLLOW_SHOW_TIMESTAMP("explicit_follow_lastShown_timestamp",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    EXPLICIT_BLOCK_SHOW_TIMESTAMP("explicit_block_lastShown_timestamp",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    LAST_IMPLICIT_FOLLOW_ACTIVITY_TIMESTAMP("last_implicit_follow_activity_timestamp",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    LAST_COLD_FOLLOW_ACTIVITY_TIMESTAMP("last_cold_signal follow_activity_timestamp",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    LAST_IMPLICIT_BLOCK_ACTIVITY_TIMESTAMP("last_implicit_block_activity_timestamp",PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    IS_IMPLICIT_FOLLOW_BLOCK_TRIGGER("is_implicit_follow_block_trigger", PreferenceType.FOLLOW_BLOCK_PREFERENCE),
    NUMBER_OF_SESSIONS_COLD_SIGNAL("number_of_session_coldsignal",PreferenceType.FOLLOW_BLOCK_PREFERENCE);
    override fun getPreferenceType(): PreferenceType {
        return type
    }

    override fun getName(): String {
        return preferenceName
    }
}