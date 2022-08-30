/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.view;

import android.app.Activity;

import com.newshunt.adengine.model.entity.BaseAdEntity;

/**
 * Represents view that is updatable with ad content.
 *
 * @author shreyas.desai
 */
public interface UpdateableAdView {
  void updateView(Activity activity, BaseAdEntity baseDisplayAdEntity);

  void onCardView(BaseAdEntity baseAdEntity);

  BaseAdEntity getAdEntity();

  void onDestroy();
}
