/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.listeners;

/**
 * Used to update players time left for auto play cards
 */
public interface VideoTimeListener {
  void onTimeUpdate(String time, long position);

  void showTimeLeft(boolean isShowRemaingTime);

  void onRenderedFirstFrame();
}
