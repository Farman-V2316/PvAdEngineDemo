/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.model.service;


import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchConfigResponse;

import io.reactivex.Observable;

/**
 * @author santhosh.kc
 */
public interface AppLaunchConfigService {

  Observable<AppLaunchConfigResponse> getAppLaunchConfig();

  Observable<AppLaunchConfigResponse> updateDBFromServer();

  void resetApiVersion();
}
