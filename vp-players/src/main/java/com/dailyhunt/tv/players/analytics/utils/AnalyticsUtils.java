package com.dailyhunt.tv.players.analytics.utils;

import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction;
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoStartAction;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;

/**
 * Created by Jayanth on 09/05/18.
 */

public class AnalyticsUtils {

  /**
   * Get end action from start action
   */
  public static PlayerVideoEndAction getVideoEndAction(PlayerVideoStartAction startAction) {
    switch (startAction) {
      case SWIPE:
        return PlayerVideoEndAction.SWIPE;
      case CLICK:
        return PlayerVideoEndAction.NEXT_CARD;
      default:
        return PlayerVideoEndAction.PAUSE;
    }
  }

  /**
   * Get referral action from start action
   * start actionand referral action are same for Video played event
   */
  public static NhAnalyticsUserAction getVideoReferralAction(PlayerVideoStartAction startAction) {
    switch (startAction) {
      case SWIPE:
        return NhAnalyticsUserAction.SWIPE;
      case CLICK:
      case NOTIFICATION:
        return NhAnalyticsUserAction.CLICK;
      default:
        return NhAnalyticsUserAction.CLICK;
    }
  }

}
