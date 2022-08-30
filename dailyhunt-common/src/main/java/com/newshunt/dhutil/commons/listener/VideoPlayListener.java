/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.commons.listener;

import androidx.annotation.NonNull;
import android.view.View;

public interface VideoPlayListener {

  void onFullScreen(boolean isFullScreen);

  void onExit(@NonNull View mediaView);


}
