/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.listener;

import android.view.View;

import com.newshunt.dhutil.view.ErrorMessageBuilder;

/**
 * To show custom Ui to open customize topic screen
 *
 * @author satosh.dhanyamraju
 */
public interface TopicsCustomErrorBuilder {
  boolean showCustomErrorForCustomizableTab(ErrorMessageBuilder errorMessageBuilder,
                                            String title, View.OnClickListener listener);
}