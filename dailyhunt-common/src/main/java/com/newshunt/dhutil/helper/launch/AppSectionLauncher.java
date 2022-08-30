/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.launch;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.common.presenter.BasePresenter;
import com.newshunt.deeplink.navigator.CommonNavigator;
import com.newshunt.dhutil.domain.controller.AppSectionLaunchUseCaseController;
import com.newshunt.dhutil.domain.usecase.AppSectionLaunchUseCase;
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionInfo;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionsResponse;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppLaunchConfigResponse;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppSectionLaunchParameters;
import com.newshunt.dataentity.dhutil.model.entity.launch.AppSectionLaunchResult;
import com.newshunt.dhutil.view.view.AppSectionLauncherView;
import com.squareup.otto.Bus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * @author santhosh.kc
 */
public class AppSectionLauncher extends BasePresenter {

  private final AppSectionLauncherView appSectionLauncherView;
  private AppSectionLaunchParameters launchParameters;
  private boolean isLaunching;
  private boolean isRegistered;
  private final int uniqueRequestId;
  private final Bus uiBus;
  private final PageReferrer pageReferrer;
  private AppSectionLaunchUseCase appSectionLaunchUseCase;
  private List<AppSectionInfo> appSectionInfos;
  private boolean launchImmediate;
  private UserAppSection targetSection;
  private boolean sectionResponseReceived;

  public AppSectionLauncher(AppSectionLauncherView appSectionLauncherView, int
      uniqueRequestId, Bus uiBus, PageReferrer pageReferrer) {
    this(appSectionLauncherView, uniqueRequestId, uiBus, pageReferrer, true);
  }

  public AppSectionLauncher(AppSectionLauncherView appSectionLauncherView, int
      uniqueRequestId, Bus uiBus, PageReferrer pageReferrer, boolean launchImmediate) {
    this.appSectionLauncherView = appSectionLauncherView;
    this.uniqueRequestId = uniqueRequestId;
    this.uiBus = uiBus;
    appSectionLaunchUseCase = new AppSectionLaunchUseCaseController(uniqueRequestId, uiBus);
    this.pageReferrer = pageReferrer;
    this.launchImmediate = launchImmediate;
  }

  @Override
  public void start() {
    if (isRegistered) {
      return;
    }

    uiBus.register(this);
    isRegistered = true;
  }

  @Override
  public void stop() {
    if (!isRegistered) {
      return;
    }

    uiBus.unregister(this);
    isRegistered = false;
  }

  public void launch() {
    AppSectionsProvider.INSTANCE.getAppSectionsObserver()
        .observe(appSectionLauncherView.getLifeCycleOwner(),this::onServerConfiguredAppSectionsReceived);
  }

  public void launchToAppSectionHome(AppSectionLaunchParameters launchParameters) {
    if (isLaunching) {
      return;
    }
    this.launchParameters = launchParameters;
    appSectionLaunchUseCase.getAppLaunchConfig();
    isLaunching = true;
  }

  public void onServerConfiguredAppSectionsReceived(AppSectionsResponse appSectionsResponse) {
    if (appSectionsResponse == null) {
      return;
    }
    appSectionInfos = appSectionsResponse.getSections();
    launchToServerConfiguredSection();
  }

  private void launchToServerConfiguredSection() {
    if (isLaunching) {
      return;
    }

    DisposableObserver<AppLaunchConfigResponse> observer = new DisposableObserver<AppLaunchConfigResponse>() {
      @Override
      public void onNext(AppLaunchConfigResponse appLaunchConfigResponse) {
        onAppConfigResponseReceived(appLaunchConfigResponse);
      }

      @Override
      public void onError(Throwable e) {
        launchToLastUserSection();

      }

      @Override
      public void onComplete() {

      }
    };
    appSectionLaunchUseCase.getAppLaunchConfig()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(observer);
    addDisposable(observer);
    isLaunching = true;
  }

  private void onAppConfigResponseReceived(AppLaunchConfigResponse appLaunchConfigResponse) {
    if (appLaunchConfigResponse == null) {
      return;
    }
    launchToServerConfiguredSection(appLaunchConfigResponse);
  }

  private void launchToServerConfiguredSection(AppLaunchConfigResponse appLaunchConfigResponse) {
    sectionResponseReceived = true;
    if (appLaunchConfigResponse == null) {
      return;
    }

    UserAppSection userAppSection = AppSectionLauncherHelper.getNextLaunchSection
        (appLaunchConfigResponse, appSectionInfos);
    if (userAppSection != null) {
      targetSection = userAppSection;
    }
    appSectionLauncherView.launchSectionResolved(userAppSection);
    if (launchImmediate) {
      launchTargetSection();
    }
  }

  /**
   * Launch the AppSection.
   * If section response not received yet, set flag to launch immediately when it does.
   */
  public void launchTargetSection() {
    if (!sectionResponseReceived) {
      launchImmediate = true;
      return;
    }
    if (targetSection == null) {
      launchToLastUserSection();
    } else {
      AppSectionLaunchParameters launchParameters = new AppSectionLaunchParameters.Builder()
          .setUserAppSection(targetSection).setPageReferrer(pageReferrer).build();
      sendAppLaunchResult(CommonNavigator.launchDefinedSectionHome(launchParameters));
    }
  }

  private void launchToLastUserSection() {
    if (appSectionLauncherView == null) {
      return;
    }
    AppSectionLaunchResult appSectionLaunched =
        CommonNavigator.defaultRuleLaunchSection(CommonUtils.getApplication(), pageReferrer);
    sendAppLaunchResult(appSectionLaunched);
  }

  private void sendAppLaunchResult(AppSectionLaunchResult result) {
    if (appSectionLauncherView == null) {
      return;
    }
    if (result != null && result.isLaunched()) {
      appSectionLauncherView.onLaunchSuccess(result.getAppSection());
    } else {
      appSectionLauncherView.onLaunchFailure(result != null ? result.getAppSection() : null);
    }
  }
}
