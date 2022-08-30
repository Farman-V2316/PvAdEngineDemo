/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Singleton class to get the UI/Rest Bus Instances
 *
 * @author maruti.borker
 */
public class BusProvider{

  private static final Bus REST_BUS = new DHBus (ThreadEnforcer.ANY);
  private static final Bus UI_BUS = new DHBus();
  private static final Bus AD_BUS = new DHBus(ThreadEnforcer.ANY);

  public BusProvider() {
  }

  public static Bus getRestBusInstance() {
    return REST_BUS;
  }

  public static Bus getUIBusInstance() {
    return UI_BUS;
  }

  public static Bus getAdBusInstance() {
    return AD_BUS;
  }

  public static <T> void postOnUIBus(final T event) {
    if (event == null) {
      return;
    }
    AndroidUtils.getMainThreadHandler().post(() -> UI_BUS.post(event));
  }

}