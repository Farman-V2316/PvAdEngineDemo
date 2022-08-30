/*
 *
 *  * Copyright (c) 2017 Newshunt. All rights reserved.
 *
 */

package com.newshunt.news.view.entity;

/**
 * Helper event to kill the activities, mainly used in sticky notification landing page activities
 *
 * Created by srikanth.ramaswamy on 09/06/17.
 */

public class ActivityKillerEvent {
  private final int newActivityId;

  public ActivityKillerEvent( final int newActivityId ){
    this.newActivityId = newActivityId;
  }

  /**
   * Return the newly created activity's ID
   * @return newly created activity's ID
   */
  public int getNewActivityId(){
    return newActivityId;
  }
}
