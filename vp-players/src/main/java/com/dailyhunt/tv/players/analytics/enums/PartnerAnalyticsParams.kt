/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics.enums

/**
 * Defines all the tracking params for partners
 */
enum class PartnerAnalyticsParams(val value: String) {
    UNIQUE_ID("UNIQUE_ID"), USER_BEHAVIOUR("USER_BEHAVIOUR"), EVENT_COUNTER("EVENT_COUNTER"),
    MEDIA_BEGIN_FLAG("MEDIA_BEGIN_FLAG"), MEDIA_END_FLAG("MEDIA_END_FLAG"),
    MEDIA_PLAY_COUNTER("MEDIA_PLAY_COUNTER"), PLAY_HEAD_POSITION("PLAY_HEAD_POSITION"),
    ELAPSED_PLAY_TIME("ELAPSED_PLAY_TIME"), MEDIA_PAUSE_COUNTER("MEDIA_PAUSE_COUNTER"),
    CLIP_PAUSE_COUNTER("CLIP_PAUSE_COUNTER"), TIME_STAMP("TIME_STAMP"), IS_MUTED("IS_MUTED"),
    IS_FULLSCREEN("IS_FULLSCREEN");

    companion object {
        fun fromName(type: String): PartnerAnalyticsParams? {
            for (param in PartnerAnalyticsParams.values()) {
                if (param.value.equals(type, ignoreCase = true)) {
                    return param
                }
            }
            return null
        }
    }

}
