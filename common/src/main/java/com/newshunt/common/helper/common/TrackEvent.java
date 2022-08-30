/**
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common;

/**
 * Wrapper model to post events on Bus
 * @author: bedprakash.rout on 20/07/17.
 */

public class TrackEvent {
  public final Object data;
  public boolean fromCache;
  public TrackEvent(Object data,boolean fromCache) {
    this.data = data;
    this.fromCache = fromCache;
  }
}
