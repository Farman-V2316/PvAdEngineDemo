/*
 * Copyright (c) 2021 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.preference

import com.newshunt.common.helper.preference.PreferenceType
import com.newshunt.common.helper.preference.SavedPreference

/**
 * Adjunct lang related preferences
 *
 * @author aman.roy
 */
enum class AdjunctLangPreference(val preferenceName:String, val type:PreferenceType):SavedPreference {

    PENDING_NOTIFICATION_RESPONSE_FLAG("pending_notification_response_flag", PreferenceType.ADJUNCT_LANG),
    PENDING_USER_WRITE_FLAG("pending_user_write_flag", PreferenceType.ADJUNCT_LANG),
    HANDSHAKE_REQUIRED("handshake_dropped",PreferenceType.ADJUNCT_LANG),
    PENDING_NOTIFICATION_OBJECT("pending_notification_object", PreferenceType.ADJUNCT_LANG),
    ADJUNCT_NOTIFICATION_TIMESTAMP("adjunct_notification_timestamp",PreferenceType.ADJUNCT_LANG),
    ADJUNCT_NOTIFICATION_UPDATE_TYPE("adjunct_notification_update_type", PreferenceType.ADJUNCT_LANG),
    ADJUNCT_NOTIFICATION_LAST_REQUEST_ID("adjunct_notification_last_request_id",PreferenceType.ADJUNCT_LANG),
    ADJUNCT_LANG_UPDATE_HANDSHAKE("adjunct_lang_update_handshake",PreferenceType.ADJUNCT_LANG),
    USER_ACTED_ON_ADJUNCT_FLOW("user_acted_adjunct_flow",PreferenceType.ADJUNCT_LANG),
    WEB_CARD_ADJUNCT_LANG("web_card_adjunct_lang",PreferenceType.ADJUNCT_LANG);

    override fun getName(): String {
        return preferenceName
    }

    override fun getPreferenceType(): PreferenceType {
        return type
    }
}