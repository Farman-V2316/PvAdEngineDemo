/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.players.analytics.events;


import androidx.annotation.NonNull;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.common.helper.common.Logger;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public final class AnalyticsService {
  public static final String TAG_NAME = AnalyticsService.class.getName();
  private static AnalyticsService INSTANCE;
  private PublishSubject<Event> eventPublishSubject;

  private AnalyticsService() {
    eventPublishSubject = PublishSubject.create();
    eventPublishSubject
        .observeOn(Schedulers.newThread())
        .subscribe(new DisposableObserver<Event>() {
          @Override
          public void onNext(final Event value) {
            pushEvent(value);
          }

          @Override
          public void onError(final Throwable e) {
            Logger.d(TAG_NAME, e.getMessage());
          }

          @Override
          public void onComplete() {

          }
        });

  }

  public static AnalyticsService instance() {
    if (INSTANCE == null) {
      synchronized (AnalyticsService.class) {
        if (INSTANCE == null) {
          INSTANCE = new AnalyticsService();
        }
      }
    }
    return INSTANCE;
  }

  @NonNull
  Observable<Event> getEventStream() {
    return eventPublishSubject;
  }

  public void trackEvent(final Event event) {
    eventPublishSubject.onNext(event);
  }

  public void pushEvent(final Event event) {
    if (event.logDynamicEvent()) {
      AnalyticsClient.logDynamic(event.getEvent(), event.getEventSectionType(),
          event.getEventParam(), event.getDynamicMap(), event.pageReferrer(), false);
    } else {
      AnalyticsClient.log(event.getEvent(), event.getEventSectionType(), event.getEventParam(),
          event.pageReferrer());
    }
  }

}
