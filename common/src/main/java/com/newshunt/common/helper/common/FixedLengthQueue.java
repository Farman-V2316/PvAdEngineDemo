package com.newshunt.common.helper.common;

import java.util.ArrayList;

/**
 * @author: bedprakash.rout on 7/7/2016.
 */
public class FixedLengthQueue<K> extends ArrayList<K> {

  private int maxSize;

  public FixedLengthQueue(int size) {
    this.maxSize = size;
  }

  public boolean add(K k) {
    boolean r = super.add(k);
    if (size() > maxSize) {
      removeRange(0, size() - maxSize);
    }
    return r;
  }

  public K getYongest() {
    int index = size() - 1;
    if (index < 0) {
      return null;
    }
    return get(index);
  }

  public K getOldest() {
    if (size() == 0) {
      return null;
    }
    return get(0);
  }
}
