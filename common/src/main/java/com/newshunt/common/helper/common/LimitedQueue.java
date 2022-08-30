/*
 * Copyright (c) 2016 Dailyhunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import java.util.LinkedList;

/**
 * Queue which will evict items at head position when size is more than limit
 *
 * @author arun.babu
 */
public class LimitedQueue<T> extends LinkedList<T> {

  private final int limit;

  public LimitedQueue(int limit) {
    super();
    this.limit = limit;
  }

  @Override
  public boolean add(T object) {
    boolean added = super.add(object);
    while (added && size() > limit) {
      remove();
    }
    return added;
  }
}
