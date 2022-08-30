package com.newshunt.news.helper;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * For measuring time diffs and logging
 * Class is tolerant to wrong invocation order. Returns false for failures/errors
 *
 * @author satosh.dhanyamraju
 */
public class VideoPlayBackTimer {

  String name;
  LinkedList<Long> strt, stp;
  private boolean isStarted;

  public VideoPlayBackTimer(String name) {
    this.name = name;
    reset();
  }

  public VideoPlayBackTimer() {
    this("Entry");
  }

  public boolean start() {
    if (isStarted) return true;
    int r = strt.size();
    int p = stp.size();
    if (r < p || Math.abs(r - p) > 1) {
      return false;
    } else if (r == p + 1) {
      //replace last start entry
      strt.removeLast();
      strt.addLast(time());
      isStarted = true;
      return true;
    }
    //add another start entry, r==p
    strt.addLast(time());
    isStarted = true;
    return true;
  }

  public boolean stop() {
    int r = strt.size();
    int p = stp.size();
    if (r != (p + 1)) {
      return false;//ERROR
    }
    stp.addLast(time());
    isStarted = false;
    return true;
  }

  public boolean reset() {
    strt = new LinkedList<>();
    stp = new LinkedList<>();
    return true;
  }

  public long getTotalTime(TimeUnit unit) {
    int r = strt.size();
    int p = stp.size();
    if (p == 0) {
      return 0;
    } else if (r < p || Math.abs(r - p) > 1) {
      return -1; //ERROR
    }
    // p == r or r-1
    long sum = 0;
    try {
      for (int i = 0; i < p; i++) {
        long diff = stp.get(i) - strt.get(i);
        if (diff < 0) {
          //return  -1?
          continue;
        }
        sum = sum + diff;
      }
    } catch (Exception ex) {
      // Race condition between read and adding array causing exception
    }
    return unit.convert(sum, TimeUnit.NANOSECONDS);
  }

  public long getTotalTime() {
    return getTotalTime(TimeUnit.MILLISECONDS);
  }

  private long time() {
    return System.nanoTime();
  }

}