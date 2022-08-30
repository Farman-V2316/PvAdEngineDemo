/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.dailyhunt.tv.players.analytics;

import android.net.Uri;

import com.dailyhunt.tv.exolibrary.util.PA;
import com.dailyhunt.tv.players.analytics.constants.PlayerAnalyticsEventParams;
import com.dailyhunt.tv.players.analytics.enums.AnalyticsEvent;
import com.dailyhunt.tv.players.analytics.enums.ExoExceptionType;
import com.dailyhunt.tv.players.constants.PlayerContants;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistTracker;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.analytics.entity.AnalyticsParam;
import com.newshunt.dataentity.dhutil.analytics.NhAnalyticsCommonEventParam;
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;


/**
 * Created by Jayanth on 09/05/18.
 */
public class PlayerAnalyticsHelper {
  public static void resetVideoStartSystemTime() {
    PreferenceManager.saveLong(PlayerContants.VIDEO_START_SYSTEM_TIME, 0L);
  }

  public static void logVideoMuteStatusEvent(String type, PageReferrer pageReferrer) {
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsAppEventParam.PAGE_VIEW_EVENT, false);
    map.put(NhAnalyticsAppEventParam.TYPE, type);
    AnalyticsClient.log(NhAnalyticsAppEvent.EXPLOREBUTTON_CLICK, NhAnalyticsEventSection.NEWS, map,
        pageReferrer);
  }

  public static void logVideoErrorAnalytics(ExoPlaybackException exception,
                                            ExoPlayerAsset playerAsset,
                                            NhAnalyticsEventSection appSection) {
    if (exception == null) {
      return;
    }
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    if (playerAsset != null) {
      eventParams.put(PlayerAnalyticsEventParams.DATA_URL, playerAsset.getVideoUrl());
      eventParams.put(AnalyticsParam.ITEM_ID.ITEM_ID, playerAsset.getId());
      eventParams.put(PlayerAnalyticsEventParams.IS_LIVE, playerAsset.isLiveStream());
      if (playerAsset.getSourceInfo() != null) {
        eventParams.put(
            AnalyticsParam.ITEM_PUBLISHER_ID, playerAsset.getSourceInfo().getSourceId());
      }
      if (!CommonUtils.isEmpty(playerAsset.getChannelKey())) {
        eventParams.put(PlayerAnalyticsEventParams.ITEM_CHANNEL_ID, playerAsset.getChannelKey());
      }
    }

    if (appSection != null) {
      eventParams.put(PlayerAnalyticsEventParams.APP_SECTION, appSection.name());
    }
    eventParams.put(PlayerAnalyticsEventParams.PLAYER_TYPE, "EXO_PLAYER");
    eventParams.put(NhAnalyticsCommonEventParam.ERROR_CODE, exception.type);
    try {
      if (exception.getMessage() != null) {
        eventParams.put(NhAnalyticsCommonEventParam.ERROR_MESSAGE,
            URLEncoder.encode(exception.getMessage(), "utf-8"));
      }
      if (exception.getCause() != null && exception.getCause().getMessage() != null) {
        eventParams.put(PlayerAnalyticsEventParams.ERROR_MESSAGE_CAUSE,
            URLEncoder.encode(exception.getCause().getMessage(), "utf-8"));
      }
    } catch (UnsupportedEncodingException e) {
      Logger.caughtException(e);
    }

    if (exception.getCause() instanceof HttpDataSource.HttpDataSourceException) {
      eventParams.put(NhAnalyticsAppEventParam.TYPE, ExoExceptionType.HTTP_DATA_SOURCE.getValue());
      HttpDataSource.HttpDataSourceException dataSourceException =
          (HttpDataSource.HttpDataSourceException) exception.getCause();
      if (dataSourceException.dataSpec != null) {
        Uri uri = dataSourceException.dataSpec.uri;
        if (uri != null) {
          eventParams.put(NhAnalyticsCommonEventParam.ERROR_URL, uri.toString());
        }
      }
    } else if (exception.getCause() instanceof HlsPlaylistTracker.PlaylistResetException) {
      eventParams.put(NhAnalyticsAppEventParam.TYPE, ExoExceptionType.PLAYLIST_RESET.getValue());
      HlsPlaylistTracker.PlaylistResetException playlistResetException =
          ((HlsPlaylistTracker.PlaylistResetException) exception.getCause());
      eventParams.put(NhAnalyticsCommonEventParam.ERROR_URL, playlistResetException.url);
    } else if (exception.getCause() instanceof HlsPlaylistTracker.PlaylistStuckException) {
      eventParams.put(NhAnalyticsAppEventParam.TYPE, ExoExceptionType.PLAYLIST_STUCK.getValue());
      HlsPlaylistTracker.PlaylistStuckException playlistStuckException =
          ((HlsPlaylistTracker.PlaylistStuckException) exception.getCause());
      eventParams.put(NhAnalyticsCommonEventParam.ERROR_URL, playlistStuckException.url);
    } else if (exception.getCause() instanceof BehindLiveWindowException) {
      eventParams.put(NhAnalyticsAppEventParam.TYPE,
          ExoExceptionType.BEHIND_LIVE_WINDOW.getValue());
    } else {
      eventParams.put(NhAnalyticsAppEventParam.TYPE, ExoExceptionType.GENERIC.getValue());
    }

    AnalyticsClient.log(AnalyticsEvent.VIDEO_PLAY_ERROR, NhAnalyticsEventSection.APP, eventParams,
        null);
  }

  public static void logPA(@NotNull PA pa) {
    Logger.d("PlayerAnalytics", pa.formattedStr());
    HashMap<NhAnalyticsEventParam, Object> params = new HashMap<NhAnalyticsEventParam, Object>();
    Object id = pa.getId();
    String idStr ;
    if (id instanceof ExoPlayerAsset) {
      idStr = ((ExoPlayerAsset) id).getId();
    } else {
      idStr = id.toString();
    }
    params.put(PAEventParam.id, idStr);
    params.put(PAEventParam.total_buffer_time, pa.getTotalBufferTime());
    params.put(PAEventParam.total_playback_time, pa.getTotalPlaybackTime());
    params.put(PAEventParam.states, pa.getAllStates().toString());
    params.put(PAEventParam.bitrates, pa.getBitratesAtStateChanges().toString());
    params.put(PAEventParam.bitrate_full_summary, pa.bitrateSummaryForEvent().toString());
    params.put(PAEventParam.load_events, pa.loadSummaryForEvent().toString());
    params.put(PAEventParam.time_for_first_format_change, pa.getTimeTakenForFirstFormatChange());
    params.put(PAEventParam.format_change_count, pa.getFormatChangeCount());
    AnalyticsClient.log(new PAEvent(), NhAnalyticsEventSection.APP, params);
  }


  static class PAEvent implements NhAnalyticsEvent {
    @NonNull
    @Override
    public String toString() {
      return "playback_info";
    }

    @Override
    public boolean isPageViewEvent() {
      return false;
    }
  }

  enum PAEventParam implements NhAnalyticsEventParam {
    id,
    total_buffer_time,
    total_playback_time,
    states,
    bitrates,
    bitrate_full_summary,
    load_events,
    time_for_first_format_change,
    format_change_count;
    @Override
    public String getName() {
      return name();
    }
  }


}
