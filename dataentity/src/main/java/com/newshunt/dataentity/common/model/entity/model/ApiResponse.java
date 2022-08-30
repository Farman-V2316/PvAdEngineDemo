/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.model;

import androidx.annotation.Nullable;

import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiData;
import com.newshunt.dataentity.common.model.entity.cachedapi.CachedApiResponseSource;

import java.util.HashMap;
import java.util.Map;


/**
 * Generic api response holder for REST api calls
 *
 * @param <T>
 * @author amarjit
 */
public class ApiResponse<T> implements java.io.Serializable, CachedApiData,
        BaseErrorReportingResponse {
  private static final long serialVersionUID = -3521522892464348387L;
  private int code;
  private Status status;
  private T data;
  private Track track;
  private String url;
  private CachedApiResponseSource cachedApiResponseSource;
  @Nullable
  private Integer clientMemoryTtlSec = 10;

  /**
   * Key value pair parameters from server to be included in events
   */
  private HashMap<String, String> experimentGlobal;

  public ApiResponse() {
  }

  public ApiResponse(T data) {
    this.data = data;
  }

  public ApiResponse(T data, int code) {
    this.data = data;
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public void setStatus(int code, String message) {
    this.code = code;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public Map<String, String> getExperimentGlobal() {
    return experimentGlobal;
  }

  @Override
  public CachedApiResponseSource getCachedApiResponseSource() {
    return cachedApiResponseSource;
  }

  @Override
  public void setCachedApiResponseSource(CachedApiResponseSource cachedApiResponseSource) {
    this.cachedApiResponseSource = cachedApiResponseSource;
  }

  public String toString() {
    return "code : " + code + ", status : " + status + ", data : " + data;
  }

  public Track getTrack() {
    return track;
  }

  public void setTrack(Track track) {
    this.track = track;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  @Nullable
  public Integer getClientMemoryTtlSec() {
    return clientMemoryTtlSec;
  }

  public void setClientMemoryTtlSec(@Nullable Integer clientMemoryTtlSec) {
    this.clientMemoryTtlSec = clientMemoryTtlSec;
  }
}
