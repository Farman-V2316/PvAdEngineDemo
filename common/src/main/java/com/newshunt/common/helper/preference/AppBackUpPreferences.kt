package com.newshunt.common.helper.preference

/**
 * @author anshul.jain
 */
enum class AppBackUpPreferences(val value: String) : SavedPreference {

    BACKUP_USER_DATA("backupUserData");

    override fun getPreferenceType(): PreferenceType {
        return PreferenceType.BACK_UP;
    }

    override fun getName(): String {
        return value
    }

}