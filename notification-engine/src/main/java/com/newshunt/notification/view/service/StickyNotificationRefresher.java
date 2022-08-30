/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.notification.view.service;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.newshunt.common.helper.preference.SavedPreference;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.APIException;
import com.newshunt.dataentity.common.model.entity.BaseError;
import com.newshunt.dataentity.common.model.entity.ListNoContentException;
import com.newshunt.dataentity.notification.asset.DataStreamResponse;
import com.newshunt.dataentity.notification.asset.GenericDataStreamAsset;
import com.newshunt.dataentity.notification.asset.NewsStickyDataStreamAsset;
import com.newshunt.dataentity.notification.util.NotificationConstants;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.notification.helper.StickyNotificationUtilsKt;
import com.newshunt.dataentity.notification.StickyNavModel;
import com.newshunt.notification.model.service.DataStreamService;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dataentity.notification.asset.BaseNotificationAsset;
import com.newshunt.dataentity.notification.asset.CricketDataStreamAsset;
import com.newshunt.notification.helper.StickyNotificationLogger;
import com.newshunt.sdk.network.Priority;
import com.newshunt.common.track.AsyncTrackHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * This class is responsible for scheduling auto refresh of stickynotification based on
 * autoRefreshInterval.
 * <p>
 * Created by srikanth.ramaswamy on 09/01/17.
 */

public class StickyNotificationRefresher {
    // default auto refresh interval of 30 secs
    private static final String TAG = "StickyNotificationRefre";
    private static final int DEFAULT_AUTO_INTERVAL = 30000;
    private final int MIN_INTERVAL_BTW_REQUESTS = 5_000; // 5 seconds
    private final int MIN_INTERVAL_FOR_UPDATING = 1_000;

    private final BaseNotificationAsset baseNotificationAsset;
    private final DataStreamService dataStreamService;
    private final StickyNavModel stickyNavModel;
    private long autoRefreshIntervalMs;
    private long previousAutoRefreshIntervalMs;
    private long lastRefreshTime;
    private long expiryTime;
    private long lastRequestTime;
    private String currentVersion;

    private static final long NOW = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static Integer refreshCount; //Need to maintain static so that it can be persisted
    // across screen off/on and network on/off
    private static Long lastTrackUrlHitTimeStamp;
    private long timeWindowForStreamTrackUrlInMillis;

    public StickyNotificationRefresher(final @NonNull BaseNotificationAsset baseNotificationAsset,
                                       final @NonNull DataStreamService streamService,
                                       final @NonNull StickyNavModel stickyNavModel) {
        dataStreamService = streamService;
        this.baseNotificationAsset = baseNotificationAsset;
        autoRefreshIntervalMs = baseNotificationAsset.getAutoRefreshInterval() > 0 ?
                baseNotificationAsset.getAutoRefreshInterval() * 1000 : DEFAULT_AUTO_INTERVAL;
        this.stickyNavModel = stickyNavModel;
        if (stickyNavModel.getBaseNotificationAsset() != null) {
            this.timeWindowForStreamTrackUrlInMillis = stickyNavModel
                    .getBaseNotificationAsset().getTimeWindowForStreamTrackUrl() * 1000l;
        }
        if (stickyNavModel.getBaseInfo() != null) {
            this.expiryTime = stickyNavModel.getBaseInfo().getExpiryTime();
        }
        Logger.d("StickNotification", "refreshInterval is "+ autoRefreshIntervalMs);
    }

    public boolean isManualRequestValid() {
        long diffBtwRequests = System.currentTimeMillis() - lastRequestTime;
        if (diffBtwRequests < MIN_INTERVAL_BTW_REQUESTS && diffBtwRequests >= 0) {
            return false;
        }
        return true;
    }

    /**
     * Schedules refresh requests with the Service Impl
     *
     * @param userRequest boolean indicating whether or not its a user requested refresh.
     * @param initialDelay
     * @return Observable to subscribe and unsubscribe
     */
    public Observable<DataStreamResponse> refresh(final boolean userRequest, long initialDelay) {
        if (baseNotificationAsset == null || CommonUtils.isEmpty(baseNotificationAsset.getStreamUrl())) {
            return Observable.empty();
        }
        final long delay = (userRequest ? NOW : getTimeToNextRefresh()) + (initialDelay * 1000);

        Logger.d("StickNotification", "refresh interval is "+ delay + "   is userRequest " + userRequest);

        return Observable.just(1).
                delay(delay, TimeUnit.MILLISECONDS).flatMap(
                integer -> Observable.defer(() -> {
                    lastRequestTime = System.currentTimeMillis();
                    SavedPreference savedPrefToSend = null;
                    if(stickyNavModel.getStickyType().equals(NotificationConstants.STICKY_NEWS_TYPE)){
                        savedPrefToSend = AppStatePreference.NEWS_STICKY_QUERY_PARAMS;
                    }
                    return dataStreamService.getStreamData(baseNotificationAsset
                                    .getStreamUrl(),
                            userRequest ? Priority.PRIORITY_HIGHEST : Priority.PRIORITY_LOW, currentVersion, stickyNavModel.getStickyType(), savedPrefToSend)
                            .doOnSubscribe(disposable -> {
                                lastRequestTime = System.currentTimeMillis();
                            })
                            .delay(getTimeDiffForShowingUpdateString(userRequest), TimeUnit.MILLISECONDS)
                            .doOnNext(processNotfnResp)
                            .onErrorReturn(error -> {
                                Logger.e(TAG, "error ", error);
                                DataStreamResponse dataStreamResponse = new DataStreamResponse();
                                if(error instanceof BaseError) {
                                    dataStreamResponse.setError((BaseError) error);
                                }else if(error instanceof ListNoContentException){
                                    dataStreamResponse.setError(((ListNoContentException)error).getError());
                                }else if(error instanceof APIException){
                                    dataStreamResponse.setError(((APIException)error).getError());
                                }else{
                                    dataStreamResponse.setError(new BaseError(error, error.getMessage()));
                                }

                              return dataStreamResponse;
                            });
                })
                        .repeatWhen(obs -> obs.flatMap(o -> Observable.just(1)
                                .delay(autoRefreshIntervalMs, TimeUnit.MILLISECONDS))));
    }

    /**
     * Returns the latest expiry time obtained from latest response
     *
     * @return the latest expiry time obtained from latest response
     */
    public long getExpiryTime() {
        return expiryTime;
    }

    private long getTimeToNextRefresh() {

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastRefreshTime) >= autoRefreshIntervalMs) {
            return NOW;
        } else {
            long remainingTime = autoRefreshIntervalMs - (currentTime - lastRefreshTime);
            // just a fail safe condition. ideally expecting it be not true
            if (remainingTime < 0) {
                remainingTime = 0;
            }
            return remainingTime;
        }
    }

    private long getTimeDiffForShowingUpdateString(boolean isUserRequest) {

        if (!isUserRequest) {
            return 0;
        }
        long diff = System.currentTimeMillis() - lastRequestTime;
        if (diff > MIN_INTERVAL_FOR_UPDATING || diff < 0) {
            return 0;
        }

        StickyNotificationLogger.stickyNotificationResponseDelay(MIN_INTERVAL_FOR_UPDATING - diff);
        return MIN_INTERVAL_FOR_UPDATING - diff;
    }

    private final Consumer<DataStreamResponse> processNotfnResp = new Consumer<DataStreamResponse>() {
        @Override
        public void accept(DataStreamResponse dataStreamResponse) throws Exception {
            lastRefreshTime = System.currentTimeMillis();

            fireTrackUrlIfApplicable();

            if (dataStreamResponse == null || dataStreamResponse.getError() != null) {
                return;
            }
            if (dataStreamResponse.getBaseStreamAsset() instanceof CricketDataStreamAsset) {
                CricketDataStreamAsset cricketDataStreamAsset =
                        (CricketDataStreamAsset) dataStreamResponse.getBaseStreamAsset();

                //Update the expiryTime value which we get from the server.
                if (cricketDataStreamAsset.getExpiryTime() > 0) {
                    if (expiryTime != cricketDataStreamAsset.getExpiryTime()) {
                        StickyNotificationUtilsKt
                            .fireExpiryTimeChangedIntent(stickyNavModel, cricketDataStreamAsset.getExpiryTime());
                    }
                    expiryTime = cricketDataStreamAsset.getExpiryTime();
                    stickyNavModel.getBaseInfo().setExpiryTime(expiryTime);
                    cricketDataStreamAsset.setExpiryTime(expiryTime);
                }

                Integer refreshTime = cricketDataStreamAsset.getAutoRefreshInterval();

                previousAutoRefreshIntervalMs = autoRefreshIntervalMs;
                if (refreshTime != null && refreshTime != 0) {
                    autoRefreshIntervalMs = refreshTime * 1000;
                }

                Long version = cricketDataStreamAsset.getVersion();
                if (version != null) {
                    currentVersion = version.toString();
                }
            } else if (dataStreamResponse.getBaseStreamAsset() instanceof GenericDataStreamAsset) {

                GenericDataStreamAsset genericDataStreamAsset =
                        (GenericDataStreamAsset) dataStreamResponse
                                .getBaseStreamAsset();

                //Update the expiryTime value which we get from the server.
                if (genericDataStreamAsset.getExpiryTime() > 0) {
                    if (expiryTime != genericDataStreamAsset.getExpiryTime()) {
                        StickyNotificationUtilsKt.fireExpiryTimeChangedIntent(stickyNavModel,
                            genericDataStreamAsset.getExpiryTime());
                    }
                    expiryTime = genericDataStreamAsset.getExpiryTime();
                    stickyNavModel.getBaseInfo().setExpiryTime(expiryTime);
                }
                genericDataStreamAsset.setExpiryTime(expiryTime);


                Integer refreshTime = genericDataStreamAsset.getAutoRefreshInterval();

                previousAutoRefreshIntervalMs = autoRefreshIntervalMs;
                if (refreshTime != null && refreshTime != 0) {
                    autoRefreshIntervalMs = refreshTime * 1000;
                }

                Long version = genericDataStreamAsset.getVersion();
                if (version != null) {
                    currentVersion = version.toString();
                }

            }else if(dataStreamResponse.getBaseStreamAsset() instanceof NewsStickyDataStreamAsset){
                NewsStickyDataStreamAsset newsStickyDataStreamAsset = (NewsStickyDataStreamAsset) dataStreamResponse.getBaseStreamAsset();
                currentVersion = null;
                if(newsStickyDataStreamAsset.getRefreshInterval() > 0){
                    previousAutoRefreshIntervalMs = autoRefreshIntervalMs;
                    autoRefreshIntervalMs = newsStickyDataStreamAsset.getRefreshInterval();
                }
                if(!CommonUtils.isEmpty(newsStickyDataStreamAsset.getUrl())){
                    baseNotificationAsset.setStreamUrl(newsStickyDataStreamAsset.getUrl());
                }
            }
        }
    };

    /**
     * This method is used to fire track url, only if the last track url was fired more than 'x'
     * seconds ago
     */
    private void fireTrackUrlIfApplicable() {

        if (lastTrackUrlHitTimeStamp == null || refreshCount == null || CommonUtils.isEmpty
                (baseNotificationAsset.getStreamTrackUrl())) {
            return;
        }

        refreshCount++;

        if (System.currentTimeMillis() - lastTrackUrlHitTimeStamp >
                timeWindowForStreamTrackUrlInMillis) {

            //Form the track url with count
            String streamTrackUrl = baseNotificationAsset.getStreamTrackUrl();
            String queryUrl = UrlUtil.getQueryUrl(streamTrackUrl);
            String baseUrl = UrlUtil.getBaseUrl(streamTrackUrl);

            Map map = UrlUtil.urlRequestParamToMap(queryUrl);
            map.put(DailyhuntConstants.TRACK_COUNT, Integer.toString(refreshCount));
            final String trackurl = UrlUtil.getUrlWithQueryParamns(baseUrl, map);
            handler.post(
                    () -> AsyncTrackHandler.getInstance().sendTrack(trackurl));
            StickyNotificationLogger.logTrackUrlHit(trackurl);

            lastTrackUrlHitTimeStamp = System.currentTimeMillis();
            refreshCount = 0;
        }
    }

    public static void initStateVariables() {
        lastTrackUrlHitTimeStamp = 0l;
        refreshCount = 0;
    }

    public static void freeStateVariables() {
        lastTrackUrlHitTimeStamp = null;
        refreshCount = null;
    }


    public long getPreviousAutoRefreshIntervalMs() {
        return previousAutoRefreshIntervalMs;
    }
}
