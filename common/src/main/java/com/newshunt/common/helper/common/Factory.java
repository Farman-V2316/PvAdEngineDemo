/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common;

/**
 * Generic interface for constructing objects of given type
 * @author satosh.dhanyamraju
 */
public interface Factory<T> {
  public T create(Object... params) throws IllegalArgumentException, ClassCastException;
}
