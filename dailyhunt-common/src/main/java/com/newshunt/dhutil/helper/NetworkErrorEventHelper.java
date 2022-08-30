/*
 *  Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsDevEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEvent;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DummyDisposable;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.model.interceptor.HeaderInterceptor;
import com.newshunt.dataentity.dhutil.analytics.NhAnalyticsCommonEventParam;
import com.newshunt.dataentity.dhutil.model.entity.ErrorEventModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.http.RealInterceptorChain;

/**
 * Helper class to batch and limit sending error events
 *
 * @author: bedprakash.rout on 27/09/17.
 */

public class NetworkErrorEventHelper {

  private static final String TAG = NetworkErrorEventHelper.class.getSimpleName();

  private static NetworkErrorEventHelper sInstance;
  private static long timerPeriodInSeconds;
  private static long maxErrorEventPerInterval;

  public static void init() {
    timerPeriodInSeconds =
        PreferenceManager.getPreference(GenericAppStatePreference.ERROR_LOGGING_TIMER_DELAY,
            Constants.DEFALUT_TIMER_PERIOD_INSECONDS);
    maxErrorEventPerInterval =
        PreferenceManager.getPreference(GenericAppStatePreference.MAX_ERROR_EVENT_PER_INTERVAL,
            Constants.MAX_ERROR_EVENT_PER_INTERVAL);
  }

  private PublishSubject<Pair<ErrorEventModel, Boolean>> timerStatus = PublishSubject.create();
  private PublishSubject<Pair<ErrorEventModel, Boolean>> dataCollectionSubject =
      PublishSubject.create();
  private boolean timerRunningStatus;

  public static NetworkErrorEventHelper getInstance() {
    if (sInstance == null) {
      synchronized (NetworkErrorEventHelper.class) {
        if (sInstance == null) {
          sInstance = new NetworkErrorEventHelper();
        }
      }
    }
    return sInstance;
  }

  @VisibleForTesting
  private NetworkErrorEventHelper() {
    Observable.merge(dataCollectionSubject, timerStatus)
        .observeOn(Schedulers.io())
        .toFlowable(BackpressureStrategy.BUFFER)
        .scan(new HashMap<Integer, ErrorEventModel>(), (storageMap, eventModelTimerPair) -> {

          ErrorEventModel event = eventModelTimerPair.first;
          Boolean fromTimer = eventModelTimerPair.second;

          // event from timer stop
          if (event == null && fromTimer != null && !fromTimer) {
            //fire all and clear map
            Observable.fromIterable(storageMap.values())
                .doOnNext(NetworkErrorEventHelper::fireErrorEvent)
                .subscribe(new DummyDisposable());
            storageMap.clear();
          }

          // event from new error
          if (event != null && fromTimer == null) {
            if (!timerRunningStatus) {
              // not running . fire and start
              NetworkErrorEventHelper.fireErrorEvent(event);
              startTimer();
            } else {
              // add and wait
              processNewEvent(storageMap, event);
            }
          }
          return storageMap;
        })
        .subscribe();

    timerStatus.onNext(Pair.create(null, false));
  }

  private void processNewEvent(HashMap<Integer, ErrorEventModel> storageMap,
                               ErrorEventModel event) {
    ErrorEventModel existingEntry = storageMap.get(event.hashValue);
    if (existingEntry != null) {
      event.setCount(existingEntry.getCount() + 1);
      storageMap.put(event.hashValue, event);
      return;
    }

    if (storageMap.size() < maxErrorEventPerInterval) {
      storageMap.put(event.hashValue, event);
    } else {
      ErrorEventModel others = storageMap.get(ErrorEventModel.OVERFLOW_HASH);
      if (others == null) {
        others = NetworkErrorEventHelper.getLimitExceededError();
      } else {
        others.setCount(others.getCount() + 1);
      }
      storageMap.put(others.hashValue, others);
    }
  }

  private void startTimer() {
    Observable.timer(timerPeriodInSeconds, TimeUnit.SECONDS)
        .observeOn(Schedulers.computation())
        .doOnSubscribe(__ -> {
          timerRunningStatus = true;
          timerStatus.onNext(Pair.create(null, true));

        })
        .doOnTerminate(() -> {
          timerRunningStatus = false;
          timerStatus.onNext(Pair.create(null, false));
        })
        .subscribe(aLong -> {}, throwable -> {
          Logger.caughtException(throwable);
        });
  }

  public void fireEvent(@NonNull ErrorEventModel event) {
    dataCollectionSubject.onNext(Pair.create(event, null));
  }

  public static ErrorEventModel getAPIErrorEvent(String url, @NonNull Response response,
                                                 @NonNull Interceptor.Chain chain) {
    Map<NhAnalyticsEventParam, Object> eventParams = getParams(url, response, chain, null);
    int hashValue = getHashValue(url, NhAnalyticsDevEvent.DEV_SERVER_REQUEST_ERROR, response
        .code());
    return new ErrorEventModel(hashValue, NhAnalyticsDevEvent.DEV_SERVER_REQUEST_ERROR,
        eventParams);
  }

  public static ErrorEventModel getSocketTimeoutErrorEvent(String url, long timeTaken,
                                                           Interceptor.Chain chain) {
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsCommonEventParam.ERROR_URL, url);
    eventParams.put(NhAnalyticsCommonEventParam.RESPONSE_TIME, timeTaken);
    String uniqueId = chain.request().header(HeaderInterceptor.UNIQUE_ID_HEADER);
    if (!CommonUtils.isEmpty(uniqueId)) {
      eventParams.put(NhAnalyticsCommonEventParam.UNIQUE_ID, uniqueId);
    }
    int hashValue = getHashValue(url, NhAnalyticsDevEvent.DEV_SOCKET_TIMEOUT_ERROR,
        Constants.HTTP_UNKNOWN);
    return new ErrorEventModel(hashValue, NhAnalyticsDevEvent.DEV_SOCKET_TIMEOUT_ERROR,
        eventParams);
  }


  public static ErrorEventModel logDelayedResponseError(String url,
                                                        @NonNull Response response,
                                                        long timeTaken,
                                                        @NonNull Interceptor.Chain chain) {
    String uniqueId = chain.request().header(HeaderInterceptor.UNIQUE_ID_HEADER);
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsCommonEventParam.RESPONSE_TIME, timeTaken);
    if (!CommonUtils.isEmpty(uniqueId)) {
      eventParams.put(NhAnalyticsCommonEventParam.UNIQUE_ID, uniqueId);
    }
    getParams(url, response, chain, eventParams);
    int hashValue = getHashValue(url, NhAnalyticsDevEvent.DEV_SERVER_REQUEST_DELAY_ERROR,
        Constants.HTTP_UNKNOWN);
    return new ErrorEventModel(hashValue, NhAnalyticsDevEvent.DEV_SERVER_REQUEST_DELAY_ERROR,
        eventParams);

  }


  private static int getHashValue(@NonNull String url,
                                  @NonNull NhAnalyticsEvent devServerRequestError, int code) {
    String hashString = url + devServerRequestError + code;
    return hashString.hashCode();
  }


  private static Map<NhAnalyticsEventParam, Object> getParams(String url,
                                                              @NonNull okhttp3.Response response,
                                                              @NonNull Interceptor.Chain chain,
                                                              @Nullable Map<NhAnalyticsEventParam, Object> eventParams) {
    if (eventParams == null) {
      eventParams = new HashMap<>();
    }
    String uniqueId = chain.request().header(HeaderInterceptor.UNIQUE_ID_HEADER);
    eventParams.put(NhAnalyticsCommonEventParam.ERROR_CODE, response.code());
    eventParams.put(NhAnalyticsCommonEventParam.ERROR_MESSAGE, response.message());
    eventParams.put(NhAnalyticsCommonEventParam.ERROR_URL, url);
    if (!CommonUtils.isEmpty(uniqueId)) {
      eventParams.put(NhAnalyticsCommonEventParam.UNIQUE_ID, uniqueId);
    }
    addServerIp(chain, eventParams);
    return eventParams;
  }

  private static void addServerIp(@NonNull Interceptor.Chain chain,
                                  @NonNull Map<NhAnalyticsEventParam, Object> eventParams) {
    try {
      eventParams.put(NhAnalyticsCommonEventParam.ERROR_ROUTE,
          ((RealInterceptorChain) chain).streamAllocation()
              .connection()
              .socket()
              .getRemoteSocketAddress()
              .toString());
    } catch (Exception e) {
      // added to avoid multiple null checks
      Logger.caughtException(e);
    }
  }

  private static void fireErrorEvent(ErrorEventModel event) {
    if (event == null) {
      return;
    }
    AnalyticsClient.logError(event.event, NhAnalyticsEventSection.APP, event.eventParams);
  }

  private static ErrorEventModel getLimitExceededError() {
    Map<NhAnalyticsEventParam, Object> eventParams = new HashMap<>();
    eventParams.put(NhAnalyticsCommonEventParam.COUNT, 0);
    return new ErrorEventModel(ErrorEventModel.OVERFLOW_HASH, NhAnalyticsDevEvent
        .DEV_NETWORK_ERROR_LIMIT_EXCEEDED, eventParams);
  }


  public static void setTimerPeriodInSeconds(long timerPeriodInSeconds) {
    NetworkErrorEventHelper.timerPeriodInSeconds = timerPeriodInSeconds;
  }

  public static void setMaxErrorEventPerInterval(long maxErrorEventPerInterval) {
    NetworkErrorEventHelper.maxErrorEventPerInterval = maxErrorEventPerInterval;
  }
}