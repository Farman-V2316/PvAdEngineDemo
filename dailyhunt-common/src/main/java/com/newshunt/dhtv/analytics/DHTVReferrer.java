/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhtv.analytics;


import com.newshunt.dataentity.common.helper.analytics.NHReferrerSource;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * @Author Rahul Ravindra.
 */

public enum DHTVReferrer implements NhAnalyticsReferrer {
  DHTV_FOR_YOU("DHTV_FOR_YOU", DHTVReferrerSource.DHTV_HOME_VIEW),
  DHTV_PLAYLIST("DHTV_PLAYLIST", DHTVReferrerSource.DHTV_HOME_VIEW),
  DHTV_HOME("DHTV_HOME", DHTVReferrerSource.DHTV_HOME_VIEW),
  VIDEO_DETAIL("VIDEO_DETAIL", DHTVReferrerSource.DHTV_HOME_VIEW),
  DHTV_RELATED("DHTV_RELATED", DHTVReferrerSource.DHTV_HOME_VIEW),
  DAILY_TV("DAILY_TV", DHTVReferrerSource.DHTV_HOME_VIEW),
  DHTV_POLL("DHTV_POLL", DHTVReferrerSource.DHTV_HOME_VIEW),
  ADD_CHANNEL("ADD_CHANNEL", DHTVReferrerSource.DHTV_HOME_VIEW),
  CHANGE_CHANNEL("CHANGE_CHANNEL", DHTVReferrerSource.DHTV_HOME_VIEW),
  GRID("GRID", DHTVReferrerSource.ADD_CHANNEL_VIEW),
  LIST("LIST", DHTVReferrerSource.ADD_CHANNEL_VIEW),
  CAROUSEL("CAROUSEL", DHTVReferrerSource.ADD_CHANNEL_VIEW),
  DHTV_TAG("DHTV_TAG", DHTVReferrerSource.DHTV_HOME_VIEW),
  EXPLORE_CHANNEL("DHTV_TAG", DHTVReferrerSource.DHTV_HOME_VIEW);

  String name;
  DHTVReferrerSource referrerSource;


  DHTVReferrer(String name) {
    this.name = name;
  }

  DHTVReferrer(String name,
               DHTVReferrerSource referrerSource) {
    this.name = name;
    this.referrerSource = referrerSource;
  }

  public static DHTVReferrer getNewsReferrer(String referrerStr) {

    if (CommonUtils.isEmpty(referrerStr)) {
      return null;
    }

    for (DHTVReferrer coolfieReferrer : DHTVReferrer.values()) {
      if (coolfieReferrer.getReferrerName().equals(referrerStr)) {
        return coolfieReferrer;
      }
    }
    return null;
  }

  @Override
  public String getReferrerName() {
    return name;
  }

  @Override
  public NHReferrerSource getReferrerSource() {
    return referrerSource;
  }
}
