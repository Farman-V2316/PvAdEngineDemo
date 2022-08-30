/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics;

import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams;
import com.dailyhunt.tv.players.analytics.enums.PlayerVideoEndAction;
import com.dailyhunt.tv.players.analytics.events.PlayerVideoEvent;
import com.dailyhunt.tv.players.interfaces.PlayerAnalyticCallbacks;
import com.newshunt.adengine.analytics.NhAnalyticsAdEventParam;
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.analytics.helper.ReferrerProvider;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.DeviceInfoHelper;
import com.newshunt.dataentity.analytics.entity.AnalyticsParam;
import com.newshunt.news.helper.VideoPlayBackTimer;
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jayanth on 09/05/18.
 */

public class ImaAnalyticsHelper extends VideoAnalyticsBaseHelper {

  private BaseDisplayAdEntity baseDisplayAdEntity;
  private PageReferrer currentPageReferrer;
  private ReferrerProvider referrerProvider;
  private PlayerVideoEndAction videoEndAction = PlayerVideoEndAction.PAUSE;
  private ExoPlayerAsset item;
  private String adId;
  private long startTime;

  private VideoPlayBackTimer videoPlayBackTimer;
  private PlayerAnalyticCallbacks playerAnalyticCallbacks;
  private PageReferrer referrerFlow;
  private PageReferrer referrerLead;

  public ImaAnalyticsHelper(ExoPlayerAsset item, PageReferrer currentPageReferrer,
                            ReferrerProvider referrerProvider,
                            PlayerAnalyticCallbacks playerAnalyticCallbacks,
                            PageReferrer referrerFlow,
                            PageReferrer referrerLead) {
    this.currentPageReferrer = currentPageReferrer;
    this.referrerProvider = referrerProvider;
    this.playerAnalyticCallbacks = playerAnalyticCallbacks;
    videoPlayBackTimer = new VideoPlayBackTimer();
    this.item = item;
    this.referrerFlow = referrerFlow;
    this.referrerLead = referrerLead;
  }

  public void setBaseDisplayAdEntity(BaseDisplayAdEntity baseDisplayAdEntity){
    this.baseDisplayAdEntity = baseDisplayAdEntity;
  }

  public void setPageReferrer(PageReferrer pageReferrer) {
    this.currentPageReferrer = pageReferrer;
  }

  public void setAnalyticCallbacks(ReferrerProvider referrerProvider,
                                   PlayerAnalyticCallbacks playerAnalyticCallbacks){
    this.referrerProvider = referrerProvider;
    this.playerAnalyticCallbacks = playerAnalyticCallbacks;
  }

  public void adStarted(String adId) {
    if (!CommonUtils.equals(this.adId, adId)) {
      this.adId = adId;
    }
    startVideoplayBackTimer();
  }

  public void adPaused() {
    setVideoEndAction(PlayerVideoEndAction.PAUSE);
  }

  public void adResumed() {
    startVideoplayBackTimer();
  }

  public void adCompleted() {
    setVideoEndAction(PlayerVideoEndAction.COMPLETE);
  }

  private void logVideoAdPlayed() {
    try {
      stopVideoPlayBackTimer();
      if (CommonUtils.isEmpty(adId)) {
        return;
      }

      Map<NhAnalyticsEventParam, Object> map = new HashMap<>();

      long playbackDuration = playbackDuration();
      videoPlayBackTimer.reset();
      if (playbackDuration <= 1000L) {
        //resetting values to default
        videoEndAction = PlayerVideoEndAction.PAUSE;
        return;
      }
      map.put(PlayerAnalyticsEventParams.PLAYBACK_DURATION, playbackDuration);
      map.put(PlayerAnalyticsEventParams.CAMPAIGN_ID, adId);

      map.put(PlayerAnalyticsEventParams.PLAYER_TYPE, "EXO_PLAYER");

      //Parent view will update additional params
      playerAnalyticCallbacks.updateAdditionalEventParams(map);

      map.put(PlayerAnalyticsEventParams.TIME_OFFSET, startTime);
      if(item != null) {
        map.put(PlayerAnalyticsEventParams.VIDEO_CONTENT_LENGTH, item.getDurationLong());
      }

      if (baseDisplayAdEntity != null) {
        map.put(PlayerAnalyticsEventParams.BANNER_ID, baseDisplayAdEntity.getCampaignId());
        map.put(NhAnalyticsAdEventParam.AD_POSITION, baseDisplayAdEntity.getAdPosition());
      }

      if (videoEndAction != null) {
        map.put(PlayerAnalyticsEventParams.END_ACTION, videoEndAction.name());
      }

      if(referrerFlow != null && referrerLead != null) {
        addReferrerParams(map, referrerFlow, referrerLead);
      } else if (referrerProvider != null) {
        addReferrerParams(map, referrerProvider.getReferrerFlow(),
            referrerProvider.getReferrerLead());
      }

      String operatorName = DeviceInfoHelper.getOperatorName(CommonUtils.getApplication());
      if (!DataUtil.isEmpty(operatorName)) {
        map.put(AnalyticsParam.NETWORK_SERVICE_PROVIDER, operatorName);
      }

      //Log event on Kibana
      new PlayerVideoEvent(map, playerAnalyticCallbacks.getExperiment(), currentPageReferrer,
          NhAnalyticsEventSection.ADS);

      videoEndAction = PlayerVideoEndAction.PAUSE;
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public void stopVideoPlayBackTimer() {
    videoPlayBackTimer.stop();
  }

  public void startVideoplayBackTimer() {
    videoPlayBackTimer.start();
  }

  @Override
  public void setVideoEndAction(PlayerVideoEndAction endAction) {
    if (videoEndAction == PlayerVideoEndAction.PAUSE) {
      videoEndAction = endAction;
      logVideoAdPlayed();
      Logger.d("+++++++", "Video end action - " + videoEndAction);
    } else {
      Logger.d("+++++++", "Cannot override video end action -> " + endAction);
    }
  }

  /**
   * Use this method to reset end action, as multiple ads can play for the same video content
   * preroll, midroll, postroll
   **/
  public void setVideoEndActionDirectlty(final PlayerVideoEndAction endAction) {
    videoEndAction = endAction;
  }

  public void setStartTime(final long startTime) {
    this.startTime = startTime;
  }


  @Override
  public void onPlay(final long currentPos) {
  }

  @Override
  public void onPause(final long currentPos) {
    setVideoEndAction(PlayerVideoEndAction.PAUSE);
  }

  @Override
  public void onError(final long currentPos) {

  }

  @Override
  public void seekTo() {

  }

  @Override
  public void bufferingStart() {

  }

  @Override
  public void setFullScreenMode(final boolean state) {

  }

  @Override
  public void bufferingStop() {

  }

  @Override
  public void end(final long duration) {

  }

  @Override
  public void currentTime(final long time) {

  }

  @Override
  public long playbackDuration() {
    return videoPlayBackTimer.getTotalTime();
  }

  @Override
  public void onResume() {
    startVideoplayBackTimer();
  }
}
