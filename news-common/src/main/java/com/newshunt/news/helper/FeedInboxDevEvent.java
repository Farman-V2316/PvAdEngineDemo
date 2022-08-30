/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import androidx.annotation.NonNull;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable pojo for feedinbox dev events
 *
 * @author satosh.dhanyamraju
 */

public class FeedInboxDevEvent implements NhAnalyticsEvent {

  public enum EvtType {
    DEV_VIEW_START,
    DEV_VIEW_STOP,
    DEV_VIEW_REQUESTED,
    DEV_VIEW_NOT_REQUESTED,
    DEV_VIEW_SHOW_DATA,
    DEV_VIEW_SHOW_DATA_EMPTY,
    DEV_VIEW_ERROR,
    DEV_VIEW_DESTROY,
    DEV_SERVICE_REQUEST,
    DEV_SERVICE_RESP_SUCCESS,
    DEV_SERVICE_RESP_SUCCESS_NO_DATA,
    DEV_SERVICE_ERROR_NW_FAILURE,
    DEV_SERVICE_ERROR_HTTP_FAILURE,
    DEV_NOTF_2NDCHUNK,
    DEV_NOTF_CONTENT_IMAGE,
    DEV_NOTF_SWIPEURL,
    AUTO_PLAY_OFF
  }

  public enum EvtParam implements NhAnalyticsEventParam {
    UNIQUE_ID,
    STORY_COUNT,
    URL,
    RESP_CODE,
    ITEM_COUNT,
    NEXT_PAGE_URL,
    MESSAGE,
    SHOWN_TO_USER,
    MESSAGE_FOR_USER,
    TRIGGER,
    REASON,
    RESULT;

    @Override
    public String getName() {
      return name().toLowerCase();
    }
  }

  public static final String TRIGGER_AUTO = "auto";
  public static final String TRIGGER_MANUAL = "manual";
  public static final String REASON_REQ_IN_PRG = "request-in-progress";
  public static final String REASON_CONTENT_AVAILABLE = "fetched-content-available";
  public static final String RESULT_SUCCESS = "success";
  public static final String RESULT_FAILURE = "failure";


  private final EvtType evtType;
  private final Map<NhAnalyticsEventParam, Object> paramsMap;
  private final long creationTime;

  private FeedInboxDevEvent(EvtType evtType, Map<NhAnalyticsEventParam, Object> paramsMap,
                            long creationTime) {
    this.evtType = evtType;
    this.paramsMap = paramsMap;
    this.creationTime = creationTime;
  }

  /**
   * Constructs FeedInboxDevEvent
   *
   * @param evtType         - evtType of event
   * @param uniqueRequestId - fragment_id which generated the event
   * @param params          - key/value pair of params, where keys are instance of NhAnalyticsEventParam
   * @return FeedInboxDevEvent instance
   */
  public static FeedInboxDevEvent create(@NonNull EvtType evtType, int uniqueRequestId,
                                         Object... params) {
    CommonUtils.checkNotNull(evtType);
    HashMap<NhAnalyticsEventParam, Object> paramMap = new HashMap<>();
    paramMap.put(EvtParam.UNIQUE_ID, uniqueRequestId);// required for all events
    int len = params.length;
    if (len > 0) {
      CommonUtils.check(len % 2 == 0, "params length should be even");
      for (int i = 0; i < params.length; i = i + 2) {
        Object key = params[i];
        CommonUtils.check(key instanceof NhAnalyticsEventParam,
            key + " not instance of NhAnalyticsEventParam");
        paramMap.put((NhAnalyticsEventParam) key, params[i + 1]);
      }
    }
    return new FeedInboxDevEvent(evtType, paramMap, System.currentTimeMillis());
  }

  public static FeedInboxDevEvent create(@NonNull EvtType evtType, HashMap<NhAnalyticsEventParam, Object> params) {
    return new FeedInboxDevEvent(evtType, params, System.currentTimeMillis());
  }

  @Override
  public boolean isPageViewEvent() {
    return false;
  }

  private String eventType() {
    return evtType.name().toLowerCase();
  }

  public Map<NhAnalyticsEventParam, Object> getParamsMap() {
    return paramsMap;
  }

  public long getCreationTime() {
    return creationTime;
  }

  @Override
  public String toString() {
    return eventType();
  }

  String toDebugString() {
    return String.format("type=%s, params=%s", eventType(), paramsMap.toString());
  }
}