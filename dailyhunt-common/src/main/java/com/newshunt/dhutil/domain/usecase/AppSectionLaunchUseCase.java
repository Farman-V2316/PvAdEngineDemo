/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.domain.usecase;

import com.newshunt.common.domain.Usecase;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchConfigResponse;

import io.reactivex.Observable;

/**
 * @author santhosh.kc
 */
public interface AppSectionLaunchUseCase extends Usecase {

  Observable<AppLaunchConfigResponse> getAppLaunchConfig();

  void sendResponseToPresenter(AppLaunchConfigResponse eventsResponse);
}
