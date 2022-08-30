/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.listener;

/**
 * Listener to notify when More/Less buttons are clicked in ExpandableTextView
 * Created by srikanth.ramaswamy on 03/13/17.
 */

public interface TextDescriptionSizeChangeListener {
  void onDescriptionExpanded(boolean expanded, String photoId);

  boolean isStoryExpanded(String photoId);
}
