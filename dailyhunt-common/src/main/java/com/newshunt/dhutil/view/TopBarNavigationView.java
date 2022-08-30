/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.newshunt.dhutil.R;

/**
 * Class to set toolbar option view.
 *
 * @author sumedh.tambat
 */
public class TopBarNavigationView extends RelativeLayout {

  public TopBarNavigationView(Context context) {
    super(context);

    final RelativeLayout rootView = (RelativeLayout) LayoutInflater.from(getContext())
        .inflate(R.layout.actionbar_app_logo, this, true);
  }

}
