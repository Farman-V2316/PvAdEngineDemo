/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.view.view;

import androidx.lifecycle.LifecycleOwner;

import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.common.view.view.BaseMVPView;

/**
 * @author santhosh.kc
 */
public interface AppSectionLauncherView extends BaseMVPView {

  void onLaunchSuccess(UserAppSection appSection);

  void onLaunchFailure(UserAppSection appSection);

  void launchSectionResolved(UserAppSection appSection);

  LifecycleOwner getLifeCycleOwner();

}
