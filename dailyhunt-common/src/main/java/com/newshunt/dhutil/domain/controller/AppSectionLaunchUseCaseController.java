/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.domain.controller;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dhutil.domain.usecase.AppSectionLaunchUseCase;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchConfigResponse;
import com.newshunt.dhutil.model.internal.service.AppLaunchConfigServiceImpl;
import com.newshunt.dhutil.model.service.AppLaunchConfigService;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import io.reactivex.Observable;

/**
 * @author santhosh.kc
 */
public class AppSectionLaunchUseCaseController implements AppSectionLaunchUseCase {

  private final int uniqueRequestId;
  private AppLaunchConfigService appLaunchConfigService;
  private final Bus uiBus;
  private boolean registered = false;

  public AppSectionLaunchUseCaseController(int uniqueRequestId, Bus uiBus) {
    this.uniqueRequestId = uniqueRequestId;
    appLaunchConfigService = new AppLaunchConfigServiceImpl();
    this.uiBus = uiBus;
  }

  @Override
  public void execute() {

  }

  @Subscribe
  public void onAppLaunchConfigResponseReceived(AppLaunchConfigResponse appLaunchConfigResponse) {
    if (appLaunchConfigResponse == null ||
        uniqueRequestId != appLaunchConfigResponse.getUniqueRequestId()) {
      return;
    }
    unRegister();
    sendResponseToPresenter(appLaunchConfigResponse);
  }

  @Override
  public Observable<AppLaunchConfigResponse> getAppLaunchConfig() {
    register();
    return appLaunchConfigService.getAppLaunchConfig();
  }

  @Override
  public void sendResponseToPresenter(final AppLaunchConfigResponse eventsResponse) {
    AndroidUtils.getMainThreadHandler().post(new UIPostRunnable(uiBus, eventsResponse));
  }

  private static class UIPostRunnable implements Runnable {

    private final AppLaunchConfigResponse appLaunchConfigResponse;
    private final Bus uiBus;

    public UIPostRunnable(Bus uiBus, AppLaunchConfigResponse appLaunchConfigResponse) {
      this.uiBus = uiBus;
      this.appLaunchConfigResponse = appLaunchConfigResponse;
    }

    @Override
    public void run() {
      uiBus.post(appLaunchConfigResponse);
    }
  }

  public void destroy() {
    unRegister();
  }

  private void register() {
    if (registered) {
      return;
    }
    BusProvider.getRestBusInstance().register(this);
    registered = true;
  }

  private void unRegister() {
    if (!registered) {
      return;
    }
    BusProvider.getRestBusInstance().unregister(this);
    registered = false;
  }
}
