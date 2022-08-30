/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.presenter;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.common.helper.info.MigrationStatusProvider;
import com.newshunt.common.presenter.BasePresenter;
import com.newshunt.dataentity.model.entity.AuthType;
import com.newshunt.dataentity.model.entity.LoginType;
import com.newshunt.dataentity.sso.model.entity.LoginPayload;
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse;
import com.newshunt.dhutil.helper.retrofit.NewsBaseUrlContainer;
import com.newshunt.sso.SSO;
import com.newshunt.sso.helper.CustomHashGenerator;
import com.newshunt.sso.model.entity.SSOResult;
import com.newshunt.sso.model.entity.UserExplicit;
import com.newshunt.sso.model.internal.service.LoginService;
import com.newshunt.sso.model.internal.service.LoginServiceImpl;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Presenter for guest login
 *
 * @author arun.babu
 */
public class SSOGuestPresenter extends BasePresenter {

  private final SSO.Publisher publisher;
  private final LoginService loginService;

  public SSOGuestPresenter(SSO.Publisher publisher) {
    loginService = new LoginServiceImpl();
    this.publisher = publisher;
  }

  @Override
  public void start() {
    //Do nothing
  }

  @Override
  public void stop() {
    //Do nothing
  }

  public void loginAsGuest() {
    String credential = ClientInfoHelper.getClientId();
    LoginPayload loginPayload = new LoginPayload(CustomHashGenerator.getHash(credential),
        AuthType.GUEST.name(), null,
        UserExplicit.NO.getValue(), null, null, null);
    Disposable disposable =
        this.loginService.login(loginPayload, NewsBaseUrlContainer.getUserServiceSecuredBaseUrl())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(new Consumer<UserLoginResponse>() {
              @Override
              public void accept(UserLoginResponse userLoginResponse) throws Exception {
                publisher.postGuestLoginResult(SSOResult.SUCCESS, userLoginResponse);
                if (userLoginResponse.getUserMigrationCompleted() != null &&
                    userLoginResponse.getUserMigrationCompleted()) {
                  MigrationStatusProvider.INSTANCE.updateMigrationStatus(null);
                }
              }
            }, new Consumer<Throwable>() {
              @Override
              public void accept(Throwable throwable) throws Exception {
                publisher.postGuestLoginResult(SSOResult.UNEXPECTED_ERROR,
                    new UserLoginResponse(null, null, LoginType.NONE, null, true, null));
                Logger.caughtException(throwable);
              }
            });
    addDisposable(disposable);
  }
}
