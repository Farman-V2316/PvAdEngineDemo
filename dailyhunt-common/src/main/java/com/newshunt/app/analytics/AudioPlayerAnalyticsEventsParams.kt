package com.newshunt.app.analytics

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam


/**
 * Created by vinod on 23/04/18.
 */

enum class AudioPlayerAnalyticsEventParams (val value: String) : NhAnalyticsEventParam {

  //Audio Playback measurement
  START_TIME("start_time"),
  END_TIME("end_time"),
  PLAYER_TYPE("player_type"),
  PLAYBACK_MODE("playback_mode"),
  END_ACTION("end_action"),
  PLAYBACK_DURATION("playback_duration"),
  START_ACTION("start_action"),
  ITEM_CATEGORY_ID("item_category_id");

  override fun getName(): String {
    return value
  }

}


