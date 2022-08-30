/*
* Copyright (c) 2017 Newshunt. All rights reserved.
*/
package com.dailyhunt.tv.players.helpers;

import android.app.Activity;

import com.dailyhunt.tv.players.presenters.PlayerInlineVideoAdBeaconPresenter;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.news.model.entity.PageType;

/**
 * Created by Jayanth on 09/05/18.
 */
public class PlayerInlineVideoAdBeaconHandler {

  private static PlayerInlineVideoAdBeaconHandler instance;
  private PlayerInlineVideoAdBeaconPresenter adBeaconPresenter;
  private PageType pageType;

  private PlayerInlineVideoAdBeaconHandler() {
  }

  public static PlayerInlineVideoAdBeaconHandler getInstance() {
    if (instance == null) {
      synchronized (PlayerInlineVideoAdBeaconHandler.class) {
        if (instance == null) {
          instance = new PlayerInlineVideoAdBeaconHandler();
        }
      }
    }
    return instance;
  }


  /**
   * Start Presenter on Ad start
   *
   * @param categoryKey
   * @param uniqueRequestId
   */
  public void onAdStart(String categoryKey, String tvVideoSource,
                        int uniqueRequestId) {
    adBeaconPresenter = new PlayerInlineVideoAdBeaconPresenter(BusProvider.getUIBusInstance(),
        uniqueRequestId, categoryKey, tvVideoSource);
    adBeaconPresenter.start();
    adBeaconPresenter.requestAd();
  }

  /**
   * Send impression Beacon on Ad End
   */
  public void onAdEnd() {
    if (adBeaconPresenter != null) {
      adBeaconPresenter.sendImpressionBeacon();
    }
  }

  /**
   * Release presenter
   */
  public void destroy() {
    if (adBeaconPresenter != null) {
      adBeaconPresenter.stop();
    }
  }

  public void setPageType(PageType pageType) {
    this.pageType = pageType;
  }

  public PageType getPageType() {
    return pageType;
  }
}
