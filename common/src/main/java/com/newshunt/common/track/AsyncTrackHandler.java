/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.track;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.newshunt.common.helper.DHWebCookieJar;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.UrlUtil;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.model.NoConnectivityException;
import com.newshunt.dataentity.common.model.entity.model.Track;
import com.newshunt.common.model.interceptor.UserAgentInterceptor;
import com.newshunt.common.model.retrofit.RestAdapterContainer;
import com.newshunt.common.model.retrofit.RestAdapters;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.sdk.network.Priority;
import com.newshunt.sdk.network.connection.ConnectionSpeed;
import com.newshunt.sdk.network.connection.ConnectionSpeedEvent;
import com.squareup.otto.Subscribe;

import java.lang.annotation.Retention;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Completable;
import okhttp3.OkHttpClient;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Hits the track trackRequest
 *
 * @author shrikant.agrawal
 */
public class AsyncTrackHandler {

  private static final Priority PRIORITY = Priority.PRIORITY_LOW;
  private static final int MAX_RETRY_ATTEMPT = 3;
  private static final Object LOCK = new Object();

  public static boolean FIRE_TRACK_FROM_CACHE;
  public static boolean FIRE_COMSCORE_TRACKS_FROM_CACHE;

  @Retention(SOURCE)
  @StringDef({RequestType.COMSCORE, RequestType.TRACK_URL})
  public @interface RequestType {
    String COMSCORE = "comscore";
    String TRACK_URL = "track";
  }

  public static final String TAG = "AsyncTrackHandler";
  private static final int MAX_THREADS_SIZE = 1;

  private static AsyncTrackHandler sInstance;
  private static AtomicInteger runningComscoreRequests = new AtomicInteger(0);
  private static AtomicBoolean pickedFromDB = new AtomicBoolean(false);

  private final ConcurrentLinkedQueue<TrackRequest> requestQueue;
  private final ScheduledThreadPoolExecutor executor;
  private final TrackRequestDao trackRequestDao;
  private boolean internetConnectionActive = true;

  private AsyncTrackHandler() {
    executor = new ScheduledThreadPoolExecutor(MAX_THREADS_SIZE);
    trackRequestDao = TrackRequestDao.getInstance();
    requestQueue = new ConcurrentLinkedQueue<>();
    BusProvider.getRestBusInstance().register(this);
    FIRE_TRACK_FROM_CACHE =
        PreferenceManager.getPreference(AppStatePreference.FIRE_TRACK_FROM_CACHE, false);
    FIRE_COMSCORE_TRACKS_FROM_CACHE =
        PreferenceManager.getPreference(AppStatePreference.FIRE_COMSCORE_TRACK_FROM_CACHE, false);
  }

  public static AsyncTrackHandler getInstance() {
    if (sInstance == null) {
      sInstance = new AsyncTrackHandler();
    }
    return sInstance;
  }

  /**
   * @param successTrackers recived in permissionsModel
   */
  public void fireSuccessTrackers(List<String> successTrackers) {
    for (String url : successTrackers) {
      sendTrack(url);
    }
  }
  public void sendTrack(final String url) {
    sendTrack(url, false);
  }

  public void sendTrack(final String url, boolean shouldPersist) {
    if (CommonUtils.isEmpty(url)) {
      return;
    }
    TrackRequest request = new TrackRequest(url, RequestType.TRACK_URL, shouldPersist);
    TrackRunnable trackRunnable = new TrackRunnable(request);
    executor.submit(trackRunnable);
  }

  public void sendTrackForComscore(String url) {
    if (CommonUtils.isEmpty(url)) {
      return;
    }
    persistRequest(new TrackRequest(url, RequestType.COMSCORE, true));
    processRequestFromQueue();
  }

  public boolean track(Track track, boolean shouldPersist) {
    if(track == null) return false;
    sendTrack(track.getUrl(), shouldPersist);
    if (track.getComscoreUrls() != null) {
      for (String aUrl : track.getComscoreUrls()) {
        sendTrackForComscore(aUrl);
      }
    }
    return true;
  }

  private void persistRequest(TrackRequest request) {

    synchronized (LOCK) {
    trackRequestDao.addRequest(request);
      // first adding to db is important as it fills up the request id for this
      requestQueue.add(request);
    }
  }

  private void removeFromStorage(TrackRequest trackRequest) {
    trackRequestDao.deleteRequest(trackRequest);
  }

  private boolean isIdle() {
    Logger.d(TAG, "IsIdle " + runningComscoreRequests);
    return runningComscoreRequests.get() <= 0;
  }

  private void processRequestFromQueue() {

    // if internet connection is off don't start
    if (!internetConnectionActive) {
      Logger.d(TAG, "Internet connection false  Waiting for internet");
      return;
    }

    if (!isIdle()) {
      return;
    }

    TrackRequest trackRequest = requestQueue.poll();
    if (trackRequest == null) {
      return;
    }
    long delay = 0;
    if (RequestType.COMSCORE.equals(trackRequest.getRequestType())) {
      runningComscoreRequests.incrementAndGet();
      delay = ComscoreDelayInterceptor.calculateDelayNeeded();
    }
    TrackRunnable trackRunnable = new TrackRunnable(trackRequest);
    executor.schedule(trackRunnable, delay, TimeUnit.MILLISECONDS);
  }


  void setInternetConnectionActive(boolean internetConnectionActive) {
    this.internetConnectionActive = internetConnectionActive;
  }

  @Subscribe
  public void onInternetConnectivityChange(@NonNull ConnectionSpeedEvent event) {
    if (ConnectionSpeed.NO_CONNECTION == event.getConnectionSpeed()) {
      return;
    }
    setInternetConnectionActive(true);
    processRequestFromQueue();
  }

  @NonNull
  private static Completable getRequestObservableForComscore(@NonNull TrackRequest trackRequest) {
    OkHttpClient.Builder clientBuilder = RestAdapterContainer.getInstance()
        .getOkHttpClientBuilder(false, PRIORITY, trackRequest.getUrl());
    clientBuilder.cookieJar(new DHWebCookieJar());
    clientBuilder.addInterceptor(new UserAgentInterceptor());
    clientBuilder.addInterceptor(new ComscoreDelayInterceptor());
    return RestAdapters.getBuilder(UrlUtil.getBaseUrl(trackRequest.getUrl()), clientBuilder.build())
        .build()
        .create(TrackAPI.class)
        .sendTrack(trackRequest.getUrl());
  }

  @NonNull
  private static Completable getRequestObservableForTrackRequest(@NonNull TrackRequest trackRequest) {
    return RestAdapters.getBuilder(
        UrlUtil.getBaseUrl(trackRequest.getUrl()), false, PRIORITY, trackRequest.getUrl())
        .build()
        .create(TrackAPI.class)
        .sendTrack(trackRequest.getUrl());
  }

  public static void setFireTrackFromCache(boolean fire) {
    FIRE_TRACK_FROM_CACHE = fire;
  }

  public static void setFireComscoreTrackFromCache(boolean fire) {
    FIRE_COMSCORE_TRACKS_FROM_CACHE = fire;
  }

  public static class TrackRunnable implements Runnable {

    private final TrackRequest trackRequest;

    TrackRunnable(@NonNull TrackRequest url) {
      this.trackRequest = url;
    }

    @Override
    public void run() {


      /*
        This is done to pick up all the entries from the DB which were not sent during the last
        session from the app. It will be done for the very first request that comes in. (That
        request can be comscore or track request). First we clear the queue. Then re fill the
        queue from teh DB. Any items present in the in memory queue are also present in the DB.
        So while clearing the queue we are not losing the items in the in memory queue.

        To keep the execution order intact if the request was to be persisted we return from thr
        runnable from here. And start processing from the queue so it picks up the requests left
        out in the last session.

        If the request was of type COMSCORE we also mark the request as finished as it is in the
        queue and will be picked up later. This is important because comscore requests we fire
        only one after another.


        if the request was not of type to persist then we simply continue the execution of that
        request after filling the queue.
       */

      if (!pickedFromDB.get()) {

        AsyncTrackHandler.getInstance().fillUpQueue();
        pickedFromDB.set(true);

        if (trackRequest.isShouldPersist()) {
          if (RequestType.COMSCORE.equals(trackRequest.getRequestType())) {
            runningComscoreRequests.decrementAndGet();
          }
          AsyncTrackHandler.getInstance().processRequestFromQueue();
          return;
        }

      }

      Completable requestObservable;

      switch (trackRequest.getRequestType()) {
        case RequestType.COMSCORE: {
          requestObservable = getRequestObservableForComscore(trackRequest);
          break;
        }
        default:
        case RequestType.TRACK_URL: {
          requestObservable = getRequestObservableForTrackRequest(trackRequest);
          break;
        }
      }

      requestObservable.subscribe(
          this::handleSuccess,
          this::handleError);
    }

    private void handleError(Throwable error) {
      if (RequestType.COMSCORE.equals(trackRequest.getRequestType())) {
        runningComscoreRequests.decrementAndGet();
      }
      //In case of no internet stop the executor and wait till internet comes back
      if (error instanceof NoConnectivityException) {
        if (trackRequest.isShouldPersist()) {
          AsyncTrackHandler.getInstance().persistRequest(trackRequest);
        }
        AsyncTrackHandler.getInstance().setInternetConnectionActive(false);
        return;
      }

      trackRequest.setFailureCount(trackRequest.getFailureCount() + 1);
      AsyncTrackHandler.getInstance().removeFromStorage(trackRequest);
      if (trackRequest.getFailureCount() < MAX_RETRY_ATTEMPT) {
        if (trackRequest.isShouldPersist()) {
          //add a new record with updated failure count
          AsyncTrackHandler.getInstance().persistRequest(trackRequest);
        }
      }
      AsyncTrackHandler.getInstance().processRequestFromQueue();
    }

    private void handleSuccess() {
      if (RequestType.COMSCORE.equals(trackRequest.getRequestType())) {
        runningComscoreRequests.decrementAndGet();
      }
      AsyncTrackHandler.getInstance().removeFromStorage(trackRequest);
      AsyncTrackHandler.getInstance().processRequestFromQueue();
    }
  }

  private void fillUpQueue() {
    synchronized (LOCK) {
      requestQueue.clear();
      requestQueue.addAll(trackRequestDao.getALLRequests());
    }
  }
}