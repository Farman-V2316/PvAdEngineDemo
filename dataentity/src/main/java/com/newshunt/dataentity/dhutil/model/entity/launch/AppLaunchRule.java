/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.entity.launch;

import java.io.Serializable;
import java.util.List;

/**
 * @author santhosh.kc
 */
public class AppLaunchRule implements Serializable {

  private static final long serialVersionUID = -3284703117839787438L;

  private List<String> previousSections;

  private long startTime;

  private long endTime;

  private String nextSection;

  private String nextSectionEntity;

  private long expiryTime;

  private String nextSectionAfterExpiry;

  private List<TimeWindow> timeWindows;

  public List<String> getPreviousSections() {
    return previousSections;
  }

  public void setPreviousSections(List<String> previousSections) {
    this.previousSections = previousSections;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

  public String getNextSection() {
    return nextSection;
  }

  public void setNextSection(String nextSection) {
    this.nextSection = nextSection;
  }

  public String getNextSectionEntity() {
    return nextSectionEntity;
  }

  public void setNextSectionEntity(String nextSectionEntity) {
    this.nextSectionEntity = nextSectionEntity;
  }

  public long getExpiryTime() {
    return expiryTime;
  }

  public void setExpiryTime(long expiryTime) {
    this.expiryTime = expiryTime;
  }

  public String getNextSectionAfterExpiry() {
    return nextSectionAfterExpiry;
  }

  public void setNextSectionAfterExpiry(String nextSectionAfterExpiry) {
    this.nextSectionAfterExpiry = nextSectionAfterExpiry;
  }

  public List<TimeWindow> getTimeWindows() {
    return timeWindows;
  }

  public void setTimeWindows(List<TimeWindow> windows) {
    this.timeWindows = windows;
  }
}
