package com.dailyhunt.tv.players.analytics;

import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams;
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoPlayBackMode;
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction;
import com.dailyhunt.tv.players.analytics.interfaces.VideoPlayerCallBacks;
import com.dailyhunt.tv.players.analytics.interfaces.VideoPlayerProperties;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;

import java.util.Map;

/**
 * Created by Jayanth on 09/05/18.
 */

public abstract class VideoAnalyticsBaseHelper
    implements VideoPlayerProperties, VideoPlayerCallBacks {

  public PlayerVideoPlayBackMode getPlayBackMode(PlayerVideoStartAction videoStartAction) {
    switch (videoStartAction) {
      case SWIPE:
        return PlayerVideoPlayBackMode.SWIPETOPLAY;
      case AUTOPLAY:
        return PlayerVideoPlayBackMode.AUTOPLAY;
      case CLICK:
        return PlayerVideoPlayBackMode.CLICKTOPLAY;
      default:
        return PlayerVideoPlayBackMode.CLICKTOPLAY;
    }
  }

  protected void addReferrerParams(Map<NhAnalyticsEventParam, Object> map,
                                 PageReferrer referrerFlow, PageReferrer referrerLead) {
    if (referrerFlow != null && referrerFlow.getReferrer() != null) {
      map.put(NhAnalyticsAppEventParam.REFERRER_FLOW,
          referrerFlow.getReferrer().getReferrerName());
      map.put(NhAnalyticsAppEventParam.REFERRER_FLOW_ID, referrerFlow.getId());
      map.put(NhAnalyticsAppEventParam.SUB_REFERRER_FLOW_ID, referrerFlow.getSubId());
    }
    if (referrerLead != null && referrerLead.getReferrer() != null) {
      map.put(PlayerAnalyticsEventParams.REFERRER_LEAD,
          referrerLead.getReferrer().getReferrerName());
      map.put(PlayerAnalyticsEventParams.REFERRER_LEAD_ID,
          referrerLead.getId());
    }
  }
}
