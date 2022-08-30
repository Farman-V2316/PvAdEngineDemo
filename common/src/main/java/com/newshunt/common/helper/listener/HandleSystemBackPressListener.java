/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.listener;

/**
 * @author shashikiran.nr on 7/11/2017.
 *
 * Listener to handle system back press when buzz video
 * playing in fullscreen mode form news section.
 */
public interface HandleSystemBackPressListener {
  void handleSystemBackPress();

  boolean handleAutoplayBackPress();
}