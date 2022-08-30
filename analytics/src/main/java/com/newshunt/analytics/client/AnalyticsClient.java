/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.analytics.client;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Pair;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;

import com.dailyhunt.huntlytics.sdk.NHAnalyticsAgent;
import com.dailyhunt.huntlytics.sdk.NHAnalyticsAgentInitParams;
import com.newshunt.analytics.entity.ClientType;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.common.helper.UserConnectionHolder;
import com.newshunt.common.helper.common.CommonBaseUrlsContainer;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.HandshakeHelperKt;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.info.DeviceInfoHelper;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.analytics.NhAnalyticsReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.status.ClientInfo;
import com.newshunt.dhutil.helper.retrofit.AnalyticsInitDone;
import com.newshunt.news.analytics.NhAnalyticsAppState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for analytics client for android.
 *
 * @author shreyas.desai
 */
public class AnalyticsClient {
  private static final int DEFAULT_MULTI_POST_WAIT_MILLIS = 20000;
  private static final String DUMMY_CLIENT_ID_EMPTY_INIT = "-1";
  private static final String DUMMY_CLIENT_ID_EMPTY_ACTIVATE = "-2";
  private static final String DUMMY_CLIENT_ID_EMPTY_SETCLIENTID = "-3";
  private static final long FIFTEEN_MINUTES_MILLIS = 15 * 60 * 1000;
  private static final int MAX_SIZE_FOR_RETROACTIVE_UPDATE = 15;
  private static final int MAX_POSSIBLE_QUEUE_SIZE = 200;
  private static final Handler workerHandler;
  private static final HandlerThread handlerThread;
  private static List<Map<String, Object>> paramsMapList = new ArrayList<>();
  private static boolean didPostOrganically;
  private static long lastLogtime;
  private static NhAnalyticsEventSection lastAppSection;
  private static NhAnalyticsEvent lastAppEvent;
  public static MutableLiveData<AnalyticsInitDone> statusEvents = new MutableLiveData<>();

  static {
    handlerThread = new HandlerThread("AnalyticsClient");
    handlerThread.start();
    workerHandler = new Handler(handlerThread.getLooper());
  }

  public static void init(final Context ctx, final ClientInfo clientInfo) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _init(ctx, clientInfo);
        statusEvents.postValue(new AnalyticsInitDone());
      }
    });
  }

  public static void logError(final NhAnalyticsEvent event, final NhAnalyticsEventSection section,
                              final Map<NhAnalyticsEventParam, Object> paramsMap) {
    boolean disableErrorEvent =
        PreferenceManager.getPreference(GenericAppStatePreference.DISABLE_ERROR_EVENT, false);

    boolean logCollectionInProgress = PreferenceManager.getBoolean(Constants
        .LOG_COLLECTION_IN_PROGRESS, false);

    // we are supposed to fire logs in 2 cases
    // server flag disableErrorEvent is false
    // log collection is active
    if (!logCollectionInProgress && disableErrorEvent) {
      return;
    }

    log(event, section, paramsMap);
  }

  public static void log(final NhAnalyticsEvent event, final NhAnalyticsEventSection section,
                         final Map<NhAnalyticsEventParam, Object> paramsMap) {

    log(event, section, paramsMap, null, null);
  }

  public static void log(NhAnalyticsEvent event, NhAnalyticsEventSection section,
                         Map<NhAnalyticsEventParam, Object> paramsMap, PageReferrer pageReferrer) {
    log(event, section, paramsMap, pageReferrer, null);
  }

  private static void log(final NhAnalyticsEvent event, final NhAnalyticsEventSection section,
                          final Map<NhAnalyticsEventParam, Object> paramsMap,
                          final PageReferrer referrerObj,
                          final ClientType client) {

    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _log(event, section, paramsMap, referrerObj, client);
      }
    });
  }

  public static void _log(NhAnalyticsEvent event, NhAnalyticsEventSection section,
                           Map<NhAnalyticsEventParam, Object> paramsMap, PageReferrer referrerObj,
                           ClientType client) {
    if (paramsMap == null) {
      paramsMap = new HashMap<>();
    }

    if (null == section) {
      section = NhAnalyticsEventSection.UNKNOWN;
    }

    paramsMap.put(NhAnalyticsAppEventParam.PAGE_VIEW_EVENT,
        String.valueOf(event.isPageViewEvent()));
    addStateParamsAndPermanentParams(paramsMap);

    //Overriding the referrer id and referrer
    NhAnalyticsAppState.addReferrerParams(referrerObj, paramsMap);

    Map<String, Object> stringParams = AttributeFilter.filterForNH(paramsMap);
    stringParams.putAll(NhAnalyticsAppState.getInstance().getGlobalExperimentParams());
    addUserSegParams(stringParams);
    postLocally(event.toString(), section.name(), stringParams);

    if (client == null || client == ClientType.NEWSHUNT) {
      NHAnalyticsAgent.logEvent(event.toString(), section.name(), stringParams);
    }

    // no need of checking expired session when we are starting a new one or expiring a old one
    if (event != NhAnalyticsAppEvent.SESSION_START && event != NhAnalyticsAppEvent.SESSION_END &&
        event != NhAnalyticsAppEvent.APP_START && event != NhAnalyticsAppEvent.APP_EXIT &&
        event != NhAnalyticsAppEvent.NOTIFICATION_DELIVERED &&
        event != NhAnalyticsAppEvent.DEVICE_GOOGLE_IDS &&
        event != NhAnalyticsAppEvent.APP_INSTALL) {
      checkIfSessionExpired();
    }
    lastLogtime = System.currentTimeMillis();
  }

  public static void logMulti(final NhAnalyticsEvent event, final NhAnalyticsEventSection section,
                              final Map<NhAnalyticsEventParam, Object> paramsMap) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _logMulti(event, section, paramsMap);
      }
    });
  }

  public static void logMultiDynamic(final NhAnalyticsEvent event,
                                     final NhAnalyticsEventSection section,
                                     final Map<NhAnalyticsEventParam, Object> eventParamMap,
                                     final Map<String, String> dynamicParamMap) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _logMultiDynamic(event, section, eventParamMap, dynamicParamMap);
      }
    });
  }

  public static void logMultiDynamic(final NhAnalyticsEvent event,
                                     final NhAnalyticsEventSection section,
                                     final Map<NhAnalyticsEventParam, Object> eventParamMap,
                                     final Map<String, String> dynamicParamMap,
                                     final PageReferrer referrer) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _logMultiDynamic(event, section, eventParamMap, dynamicParamMap, referrer);
      }
    });
  }

  public static void logEventNow(final NhAnalyticsEvent event,
                                 final NhAnalyticsEventSection section,
                                 final Map<NhAnalyticsEventParam, Object> paramsMap) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _logEventNow(event, section, paramsMap);
      }
    });
  }

  // Method to Flush Events  [Use Case : a) App Exit /Close ]
  public static void flushPendingEvents() {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        NHAnalyticsAgent.flushEvents();
      }
    });
  }

  // Method to be called once , Device is registered with BE.
  public static void activateHttpSDKCalls(final String clientId) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _activateHttpSDKCalls(clientId);
      }
    });
  }

  public static void logDynamic(final NhAnalyticsEvent event, final NhAnalyticsEventSection section,
                                final Map<NhAnalyticsEventParam, Object> paramsMap,
                                final Map<String, String> dynamicParamsMap, boolean forceAsPvEvent) {
    logDynamic(event, section, paramsMap, dynamicParamsMap, null, forceAsPvEvent);
  }


  public static void logDynamic(NhAnalyticsEvent event, NhAnalyticsEventSection section
      , Map<NhAnalyticsEventParam, Object> paramsMap, Map<String, String> dynamicMap,
                                final PageReferrer referrer, boolean forceAsPvEvent) {
    Map<NhAnalyticsEventParam, Object> map = CommonUtils.isEmpty(paramsMap) ? paramsMap : new HashMap<>(paramsMap);
    logDynamic(event, section, map, dynamicMap, null, referrer, forceAsPvEvent);
  }

  public static Map<String, Object> logProcessedDynamic(final String event,
                                                        final String section,
                                                        final Map<String, Object> params) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _logProcessedDynamic(event, section, params);
      }
    });

    return params;
  }

  private static void _logProcessedDynamic(String eventName, String sectionName,
                                           Map<String, Object> stringParam) {
    addUserSegParams(stringParam);
    postLocally(eventName, sectionName, stringParam);
    NHAnalyticsAgent.logEvent(eventName, sectionName, stringParam);
    lastLogtime = System.currentTimeMillis();
  }


  public static Map<String, Object> logStringParamsBasedEvents(final String event,
                                                               final NhAnalyticsEventSection section,
                                                               final Map<String, Object> params) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _logStringParamsBasedEvents(event, section, params);
      }
    });

    return params;
  }

  private static void _logStringParamsBasedEvents(String eventName,
                                                  NhAnalyticsEventSection sectionName,
                                                  Map<String, Object> stringParam) {
    if (sectionName == null) {
      sectionName = NhAnalyticsEventSection.UNKNOWN;
    }
    Map<String, Object> newParams = new HashMap<>();
    newParams = AttributeFilter.filterForNH(NhAnalyticsAppState.getInstance().getStateParams(false));
    for (Map.Entry<String, Object> mapEntry : newParams.entrySet()) {
      if (stringParam.get(mapEntry.getKey()) == null) {
        stringParam.put(mapEntry.getKey(), mapEntry.getValue());
      }
    }
    addUserSegParams(stringParam);
    postLocally(eventName, sectionName.getEventSection(), stringParam);
    NHAnalyticsAgent.logEvent(eventName, sectionName.getEventSection(), stringParam);
  }

  public static void logDynamic(final NhAnalyticsEvent event, final NhAnalyticsEventSection section,
                                final Map<NhAnalyticsEventParam, Object> paramsMap,
                                final Map<String, String> dynamicParamsMap,
                                final ClientType clientType, final PageReferrer referrer, boolean forceAsPvEvent) {
    workerHandler.post(new Runnable() {
      @Override
      public void run() {
        _logDynamic(event, section, paramsMap, dynamicParamsMap, clientType, referrer, forceAsPvEvent);
      }
    });
  }

  private static void _init(Context ctx, ClientInfo clientInfo) {
    String clientId = Constants.EMPTY_STRING;
    String userId = Constants.EMPTY_STRING;
    if (clientInfo != null) {
      clientId = clientInfo.getClientId();
      userId = clientInfo.getUserId();
    }

    // just to be double sure
    clientId = DataUtil.isEmpty(clientId) ? DUMMY_CLIENT_ID_EMPTY_INIT : clientId;

    NHAnalyticsAgent.init(ctx, clientId, userId,
        new NHAnalyticsAgentInitParams(CommonBaseUrlsContainer.getInstance().getAnalyticsUrl(),
            MAX_POSSIBLE_QUEUE_SIZE,
            MAX_SIZE_FOR_RETROACTIVE_UPDATE, true));
  }

  private static void _logMulti(NhAnalyticsEvent event, NhAnalyticsEventSection section,
                                Map<NhAnalyticsEventParam, Object> paramsMap) {
    if (paramsMap == null) {
      paramsMap = new HashMap<>();
    }

    if(section == null){
      section = NhAnalyticsEventSection.UNKNOWN;
    }

    NhAnalyticsReferrer referrer = NhAnalyticsAppState.getInstance().getReferrer();
    paramsMap.put(NhAnalyticsAppEventParam.REFERRER, referrer);
    String referrerId = NhAnalyticsAppState.getInstance().getReferrerId();
    paramsMap.put(NhAnalyticsAppEventParam.REFERRER_ID, referrerId);
    String subReferrerId = NhAnalyticsAppState.getInstance().getSubReferrerId();
    paramsMap.put(NhAnalyticsAppEventParam.SUB_REFERRER_ID, subReferrerId);

    NhAnalyticsUserAction referrerAction = NhAnalyticsAppState.getInstance().getAction();
    if (referrerAction != null) {
      paramsMap.put(NhAnalyticsAppEventParam.REFERRER_ACTION, referrerAction.name());
    }

    Map<String, Object> eventParamMap = AttributeFilter.filterForNH(paramsMap);
    paramsMapList.add(eventParamMap);
    if (paramsMapList.size() == 1) {
      startCounter(event, section);
    }

    if (paramsMapList.size() >= 10) {
      logToServer(event, section);
      didPostOrganically = true;
    }
    postLocally(event.toString(), section.name(), AttributeFilter.filterForNH(paramsMap));
  }

  private static void _logMultiDynamic(NhAnalyticsEvent event, NhAnalyticsEventSection section,
                                       Map<NhAnalyticsEventParam, Object> eventParamMap,
                                       Map<String, String> dynamicParamMap) {
    _logMultiDynamic(event, section, eventParamMap, dynamicParamMap, null);
  }

  public static void _logMultiDynamic(NhAnalyticsEvent event, NhAnalyticsEventSection section,
                                       Map<NhAnalyticsEventParam, Object> eventParamMap,
                                       Map<String, String> dynamicParamMap,
                                       PageReferrer pageReferrer) {
    if (eventParamMap == null) {
      eventParamMap = new HashMap<>();
    }
    if (dynamicParamMap == null) {
      dynamicParamMap = new HashMap<>();
    }

    if(section == null){
      section = NhAnalyticsEventSection.UNKNOWN;
    }

    NhAnalyticsReferrer referrer = NhAnalyticsAppState.getInstance().getReferrer();
    eventParamMap.put(NhAnalyticsAppEventParam.REFERRER, referrer);
    String referrerId = NhAnalyticsAppState.getInstance().getReferrerId();
    eventParamMap.put(NhAnalyticsAppEventParam.REFERRER_ID, referrerId);
    String subReferrerId = NhAnalyticsAppState.getInstance().getSubReferrerId();
    eventParamMap.put(NhAnalyticsAppEventParam.SUB_REFERRER_ID, subReferrerId);

    NhAnalyticsUserAction referrerAction = NhAnalyticsAppState.getInstance().getAction();
    if (referrerAction != null) {
      eventParamMap.put(NhAnalyticsAppEventParam.REFERRER_ACTION, referrerAction.name());
    }

    if (pageReferrer != null) {
      NhAnalyticsAppState.addReferrerParams(pageReferrer, eventParamMap);
    }

    Map<String, Object> stringParams = AttributeFilter.filterForNH(eventParamMap);
    stringParams.putAll(dynamicParamMap);
    addUserSegParams(stringParams);


    //checking for app section
    if (lastAppSection != null && lastAppSection != section) {
      logToServer(event, lastAppSection);
    }
    lastAppSection = section;

    // checking for app event
    if (lastAppEvent != null && lastAppEvent != event) {
      logToServer(lastAppEvent, lastAppSection);
    }
    lastAppEvent = event;

    paramsMapList.add(stringParams);
    if (paramsMapList.size() == 1) {
      startCounter(event, section);
    }

    if (paramsMapList.size() >= 10) {
      logToServer(event, section);
      didPostOrganically = true;
    }

    postLocally(event.toString(), section.name(), stringParams);
  }

  private static void logToServer(NhAnalyticsEvent event, NhAnalyticsEventSection section) {
    if (CommonUtils.isEmpty(paramsMapList)) {
      return;
    }
    String clientId = ClientInfoHelper.getClientId();
    Map<NhAnalyticsEventParam, Object> standardParams =
        NhAnalyticsEventHelper.getBaseParams(clientId);
    Map<NhAnalyticsEventParam, Object> appStateParams =
        NhAnalyticsAppState.getInstance().getStateParams(true);
    standardParams.putAll(appStateParams);

    standardParams.remove(NhAnalyticsAppEventParam.REFERRER);
    standardParams.remove(NhAnalyticsAppEventParam.REFERRER_ID);
    standardParams.remove(NhAnalyticsAppEventParam.REFERRER_ACTION);

    Map<String, Object> stringParams = AttributeFilter.filterForNH
        (standardParams);
    stringParams.putAll(NhAnalyticsAppState.getInstance().getGlobalExperimentParams());
    addUserSegParams(stringParams);

    NHAnalyticsAgent.logEvent(event.toString(), section.name(), stringParams, paramsMapList);
    didPostOrganically = true;
    paramsMapList = new ArrayList<>();
  }

  private static void startCounter(final NhAnalyticsEvent event,
                                   final NhAnalyticsEventSection section) {
    final Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        // Post to server if user looked at few cards but didn't finish page
        // in last few seconds.
        if (paramsMapList.size() > 0 && !didPostOrganically) {
          logToServer(event, section);
          didPostOrganically = false;
        }
      }
    }, DEFAULT_MULTI_POST_WAIT_MILLIS);

  }

  private static void _logEventNow(NhAnalyticsEvent event, NhAnalyticsEventSection section,
                                   Map<NhAnalyticsEventParam, Object> paramsMap) {

    if (paramsMap == null) {
      paramsMap = new HashMap<>();
    }

    if(section == null){
      section = NhAnalyticsEventSection.UNKNOWN;
    }

    addStateParamsAndPermanentParams(paramsMap);

    Map<String, Object> nhParams = AttributeFilter.filterForNH(paramsMap);
    nhParams.putAll(NhAnalyticsAppState.getInstance().getGlobalExperimentParams());
    addUserSegParams(nhParams);
    NHAnalyticsAgent.logEventNow(event.toString(), section.name(), nhParams);

    // no need of checking expired session when we are starting a new one or expiring a old one
    if (event != NhAnalyticsAppEvent.SESSION_START && event != NhAnalyticsAppEvent.SESSION_END &&
        event != NhAnalyticsAppEvent.APP_START && event != NhAnalyticsAppEvent.APP_EXIT &&
        event != NhAnalyticsAppEvent.NOTIFICATION_DELIVERED &&
        event != NhAnalyticsAppEvent.APP_INSTALL) {
      checkIfSessionExpired();
    }
  }

  private static void checkIfSessionExpired() {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastLogtime > FIFTEEN_MINUTES_MILLIS) {
      renewSession();
    }
  }

  private static void renewSession() {
    // if last log time is zero that means there was no previous session
    if (lastLogtime != 0) {
      logPrevSessionEnd();
    }
    startNewSession();
  }

  private static void startNewSession() {
    PreferenceManager.savePreference(GenericAppStatePreference.APP_START_TIME,
        System.currentTimeMillis());

    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsAppEventParam.START_STATE, NhAnalyticsAppEvent.APP_START.name());
    eventParams.put(NhAnalyticsAppEventParam.REFERRER, NhAnalyticsUserAction.IDLE.name());

    Pair<Long, Long> dataConsumed = DeviceInfoHelper.getDataConsumed();
    PreferenceManager.saveLong(GenericAppStatePreference.DEVICE_DATA_CONSUMED, dataConsumed.first);
    PreferenceManager.saveLong(GenericAppStatePreference.APP_DATA_CONSUMED, dataConsumed.second);
    eventParams.put(NhAnalyticsAppEventParam.USER_BOOT_DATACONSUMED, dataConsumed.first);
    eventParams.put(NhAnalyticsAppEventParam.DH_BOOT_DATACONSUMED, dataConsumed.second);

    log(NhAnalyticsAppEvent.SESSION_START, NhAnalyticsEventSection.APP, eventParams);
  }

  private static void logPrevSessionEnd() {
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsAppEventParam.END_STATE,
        NhAnalyticsUserAction.IDLE.name());

    long startTime = PreferenceManager.getPreference(GenericAppStatePreference.APP_START_TIME, 0L);
    if (startTime > 0 && lastLogtime - startTime > 0) {
      long sessionLengthMillis = lastLogtime - startTime;
      eventParams.put(NhAnalyticsAppEventParam.SESSION_LENGTH, sessionLengthMillis);
    }

    long deviceDataConsumedAtSessionStart =
        PreferenceManager.getPreference(GenericAppStatePreference.DEVICE_DATA_CONSUMED, 0L);
    long appDataConsumedAtSessionStart =
        PreferenceManager.getPreference(GenericAppStatePreference.APP_DATA_CONSUMED, 0L);
    PreferenceManager.remove(GenericAppStatePreference.DEVICE_DATA_CONSUMED);
    PreferenceManager.remove(GenericAppStatePreference.APP_DATA_CONSUMED);
    Pair<Long, Long> dataConsumed = DeviceInfoHelper.getDataConsumed();
    if (dataConsumed.first - deviceDataConsumedAtSessionStart > 0 &&
        dataConsumed.second - appDataConsumedAtSessionStart > 0) {
      eventParams.put(NhAnalyticsAppEventParam.USER_SESSION_DATACONSUMED, dataConsumed.first -
          deviceDataConsumedAtSessionStart);
      eventParams.put(NhAnalyticsAppEventParam.DH_SESSION_DATACONSUMED, dataConsumed.second -
          appDataConsumedAtSessionStart);
    }

    log(NhAnalyticsAppEvent.SESSION_END, NhAnalyticsEventSection.APP, eventParams);
  }

  public static void addStateParamsAndPermanentParams(Map<NhAnalyticsEventParam, Object> paramsMap) {
    String clientId = ClientInfoHelper.getClientId();
    Map<NhAnalyticsEventParam, Object> standardParams =
        NhAnalyticsEventHelper.getBaseParams(clientId);
    paramsMap.putAll(standardParams);

    Map<NhAnalyticsEventParam, Object> appStateParams = NhAnalyticsAppState.getInstance()
        .getStateParams(true);

    // remove the fg_session_id from the app state params if present in the paramsMap
    if (paramsMap.containsKey(NhAnalyticsAppEventParam.FG_SESSION_ID)) {
      appStateParams.remove(NhAnalyticsAppEventParam.FG_SESSION_ID);
    }
    paramsMap.putAll(appStateParams);

    paramsMap.put(NhAnalyticsAppEventParam.APP_ID, Constants.APP_ID);
    addConnectionParams(paramsMap);
    paramsMap.put(NhAnalyticsAppEventParam.IS_IN_FG, CommonUtils.isInFg);
  }

  private static void postLocally(String eventName, String section, Map<String, Object> paramsMap) {

    if (!Logger.loggerEnabled()) {
      return;
    }
    StringBuilder stringBuilder = new StringBuilder();
    for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
      if (stringBuilder.length() > 0) {
        stringBuilder.append("&");
      }

      stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
    }
    Logger.d("LOG_EVENT", eventName + " --- " + section + " <<<< ++++++++++++++++++ >>>> " +
        stringBuilder);
  }

  // Method to be called once , Device is registered with BE.
  private static void _activateHttpSDKCalls(String clientId) {

    // just to be double sure
    clientId = DataUtil.isEmpty(clientId) ? DUMMY_CLIENT_ID_EMPTY_ACTIVATE : clientId;

    NHAnalyticsAgent.activateSend(clientId);
  }

  @WorkerThread
  public static void _logDynamic(NhAnalyticsEvent event, NhAnalyticsEventSection section
      , Map<NhAnalyticsEventParam, Object> paramsMap, Map<String, String> dynamicMap,
                                 ClientType client, PageReferrer referrer, boolean forceAsPvEvent) {
    if (paramsMap == null) {
      paramsMap = new HashMap<>();
    }

    if(section == null){
      section = NhAnalyticsEventSection.UNKNOWN;
    }


    paramsMap.put(NhAnalyticsAppEventParam.PAGE_VIEW_EVENT,
        String.valueOf(event.isPageViewEvent()));
    if(forceAsPvEvent){
      paramsMap.put(NhAnalyticsAppEventParam.PAGE_VIEW_EVENT,
          String.valueOf(true));
    }else{
      paramsMap.put(NhAnalyticsAppEventParam.PAGE_VIEW_EVENT,
          String.valueOf(event.isPageViewEvent()));
    }

    addStateParamsAndPermanentParams(paramsMap);

    if (referrer != null) {
      NhAnalyticsAppState.addReferrerParams(referrer, paramsMap);
    }

    Map<String, Object> stringParam = AttributeFilter.filterForNH(paramsMap);
    stringParam.putAll(NhAnalyticsAppState.getInstance().getGlobalExperimentParams());
    if (dynamicMap != null) {
      stringParam.putAll(dynamicMap);
    }
    addUserSegParams(stringParam);
    postLocally(event.toString(), section.name(), stringParam);

    if (client == null || client == ClientType.NEWSHUNT) {
      NHAnalyticsAgent.logEvent(event.toString(), section.name(), stringParam);
    }

    // no need of checking expired session when we are starting a new one or expiring a old one
    if (event != NhAnalyticsAppEvent.SESSION_START && event != NhAnalyticsAppEvent.SESSION_END &&
        event != NhAnalyticsAppEvent.APP_START && event != NhAnalyticsAppEvent.APP_EXIT &&
        event != NhAnalyticsAppEvent.NOTIFICATION_DELIVERED &&
        event != NhAnalyticsAppEvent.APP_INSTALL) {
      checkIfSessionExpired();
    }
    lastLogtime = System.currentTimeMillis();
  }

  /**
   * The "userSeg" key-values pairs are received in multiple API and stored locally.
   * To be sent in all analytics events.
   * @param stringParam They will be added to this map
   */
  private static void addUserSegParams(Map<String, Object> stringParam) {
    Map<String, String> userSegMap = HandshakeHelperKt.readUserSegFromPref();
    if (!CommonUtils.isEmpty(userSegMap)) {
      stringParam.putAll(userSegMap);
    }
  }

  public static void addConnectionParams(Map<NhAnalyticsEventParam, Object> paramsMap){
    if(paramsMap == null){
      return;
    }

    if(!CommonUtils.isEmpty(UserConnectionHolder.INSTANCE.getUserConnectionType())) {
      paramsMap.put(NhAnalyticsAppEventParam.USER_CONNECTION,
              UserConnectionHolder.INSTANCE.getUserConnectionType());
    }
    if(!CommonUtils.isEmpty(UserConnectionHolder.INSTANCE.getUserConnectionQuality())){
      paramsMap.put(NhAnalyticsAppEventParam.USER_CONNECTION_QUALITY,
              UserConnectionHolder.INSTANCE.getUserConnectionQuality());
    }
    if(0.0 != UserConnectionHolder.INSTANCE.getExoEstimatedSpeed()){
      paramsMap.put(NhAnalyticsAppEventParam.EXOESTIMATION_CONNECTION_SPEEDINKBPS,
              UserConnectionHolder.INSTANCE.getExoEstimatedSpeed());
    }
    if(0.0 != UserConnectionHolder.INSTANCE.getNetworkEstimatedSpeed()){
      paramsMap.put(NhAnalyticsAppEventParam.FBESTIMATION_CONNECTION_SPEEDINKBPS,
              UserConnectionHolder.INSTANCE.getNetworkEstimatedSpeed());
    }
    if(0.0 != UserConnectionHolder.INSTANCE.getEstimatedSpeed()){
      paramsMap.put(NhAnalyticsAppEventParam.ESTIMATED_CONNECTION_SPEEDINKBPS,
              UserConnectionHolder.INSTANCE.getEstimatedSpeed());
    }
  }
}
