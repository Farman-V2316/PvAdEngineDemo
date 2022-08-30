/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model

/**
 * Created by kajal.kumari on 03/05/22.
 */
enum class SettingsSection(val value: String) {
    NOTIFICATION("notification"),
    APP_LANGUAGE("appLanguage"),
    PREFERRED_LOCATION("location"),
    BLOCKED_SOURCES("blockedSources"),
    FEEDBACK("feedback"),
    NEWS_LANGUAGE("newsLanguage"),
    DISPLAY_THEME("displayTheme"),
    AUTOPLAY("autoPlay"),
    CARDS_LAYOUT("cardsLayout"),
    SELECT_APP_TO_SHARE("selectAppToShare"),
    CHECK_FOR_UPDATE("update"),
    CLIENT_ID("clientId"),
    ADVERTISING("advertising"),
    HELP("help"),
    ABOUT_US("aboutUs"),
    SIGN_OUT("signOut");

    fun getSection(): String {
        return value;
    }

    companion object {
        @JvmStatic
        fun fromName(name: String?): SettingsSection? {
            for (settingsSection in values()) {
                if (settingsSection.value.equals(name, ignoreCase = true)) {
                    return settingsSection
                }
            }
            return null
        }
    }
}