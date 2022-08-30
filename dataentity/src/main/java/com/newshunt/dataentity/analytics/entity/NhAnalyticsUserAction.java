/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.analytics.entity;

/**
 * Actions user may perform with the app.
 *
 * @author shreyas.desai
 */
public enum NhAnalyticsUserAction {
  LAUNCH, CLICK, SWIPE, BACK, FAVORITE, UN_FAVORITE, IDLE, FORCE_CLOSE, NORMAL_EXIT, VIEW,
  DELETE, CLIENT_FILTER, SCROLL, AUTOSCROLL, NOTIFICATION, NONE, CROSS_DISMISS, REFRESH,
  DELETE_VIDEO, AUTO_PLAY,AUTO_SWIPE,
  SERVER_REMOVED, IB_CLICK, MINIMIZE
}
