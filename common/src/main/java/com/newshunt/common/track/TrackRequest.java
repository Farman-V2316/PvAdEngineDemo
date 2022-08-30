/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.track;

import androidx.annotation.NonNull;

import com.newshunt.common.track.AsyncTrackHandler.RequestType;
/**
 * @author: bedprakash.rout on 03/08/17.
 */

public class TrackRequest {
  private static final int INVALID_ID = -1;
  public long id = INVALID_ID;
  public String url;
  private String requestType;
  public int failureCount;
  private boolean shouldPersist;

  TrackRequest(@NonNull String url, @RequestType String type) {
    this(url, type, false);
  }

  TrackRequest(@NonNull String url, @RequestType String type, boolean shouldPersist) {
    this(INVALID_ID, url, type, 0, shouldPersist);
  }

  TrackRequest(long id, @NonNull String url, @RequestType String type) {
    this(id, url, type, 0, false);
  }

  TrackRequest(long id, @NonNull String url, @RequestType String type, int failureCount,
               boolean shouldPersist) {
    this.id = id;
    this.url = url;
    this.requestType = type;
    this.failureCount = failureCount;
    this.shouldPersist = shouldPersist;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  String getRequestType() {
    return requestType;
  }

  public int getFailureCount() {
    return failureCount;
  }

  public void setFailureCount(int failureCount) {
    this.failureCount = failureCount;
  }

  @Override
  public String toString() {
    return "TrackEvent{" +
        "id='" + id + '\'' +
        "url='" + url + '\'' +
        ", requestType=" + requestType +
        '}';
  }

  public boolean isShouldPersist() {
    return shouldPersist;
  }

  public void setShouldPersist(boolean shouldPersist) {
    this.shouldPersist = shouldPersist;
  }
}
