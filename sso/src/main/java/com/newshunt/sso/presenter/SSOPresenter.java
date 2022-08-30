/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.presenter;

import android.os.Handler;
import android.os.Looper;

import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.presenter.BasePresenter;
import com.newshunt.dataentity.dhutil.model.entity.appsflyer.AppsFlyerEvents;
import com.newshunt.dataentity.model.entity.LoginType;
import com.newshunt.dataentity.sso.model.entity.AccountLinkingResult;
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse;
import com.newshunt.dhutil.helper.appsflyer.AppsFlyerHelper;
import com.newshunt.sso.SSO;
import com.newshunt.sso.model.entity.LoginResponse;
import com.newshunt.sso.model.entity.SSOResult;
import com.squareup.otto.Subscribe;

/**
 * Presenter for logout, verify session
 *
 * @author arun.babu
 */
public class SSOPresenter extends BasePresenter {

  private final SSO.Publisher publisher;
  private LoginType loginType;
  private boolean isBackground = false;
  private String userId;
  private static final String LOG_TAG = "SSOPresenter";

  public SSOPresenter(SSO.Publisher publisher) {
    this.publisher = publisher;
  }

  public void registerBus() {
    // Need to register on bus from UI thread only
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        BusProvider.getUIBusInstance().register(SSOPresenter.this);
      }
    });
  }


  @Override
  public void start() {
    //Do nothing
  }

  @Override
  public void stop() {
    //Do nothing
  }

  @Subscribe
  public void onLogin(LoginResponse loginResponse) {
    if (LoginType.NONE == loginResponse.getLoginType()) {
      publisher.postLoginResult(loginResponse.getSsoResult(), loginResponse.getUserLoginResponse());
    } else if (SSOResult.SUCCESS == loginResponse.getSsoResult()) {
      if (loginResponse.isAccountLinkingInProgress()) {
        Logger.d(LOG_TAG, "Account linking is in progress, save the login response");
        saveLoginResponse(loginResponse.getUserLoginResponse());
      } else {
        publishLoginResult(loginResponse.getSsoResult(), loginResponse.getUserLoginResponse());
      }
      if(loginResponse.getLoginType() == LoginType.GOOGLE) {
        AppsFlyerHelper.INSTANCE.trackEvent(AppsFlyerEvents.EVENT_USER_LOGIN_GOOGLE, null);
      } else if(loginResponse.getLoginType() == LoginType.FACEBOOK) {
        AppsFlyerHelper.INSTANCE.trackEvent(AppsFlyerEvents.EVENT_USER_LOGIN_FACEBOOK, null);
      } else if(loginResponse.getLoginType() == LoginType.MOBILE) {
        AppsFlyerHelper.INSTANCE.trackEvent(AppsFlyerEvents.EVENT_USER_LOGIN_TRUECALLER, null);
      }
    } else {
      publisher.postLoginResult(loginResponse.getSsoResult());
    }
  }

  @Subscribe
  public void onAccountLinkingDone(AccountLinkingResult result) {
    //If a different account is linked, login result would be posted and taken care of!
    if (result == AccountLinkingResult.DIFFERENT_ACC_LINKED) {
      Logger.d(LOG_TAG, "Ignored linking result,  " + result);
      return;
    }
    UserLoginResponse tempResponse = readPendingLoginResponse();
    if (tempResponse != null) {
      Logger.d(LOG_TAG, "Publishing a pending session for " + result);
      publishLoginResult(SSOResult.SUCCESS, tempResponse);
    }
  }

  /**
   * Check for any pending session, punlish it and clear the session
   * @return true if there was a pending session. Else false
   */
  public boolean checkAndInitPendingSession() {
    UserLoginResponse pendingResponse = readPendingLoginResponse();
    if (pendingResponse == null) {
      Logger.d(LOG_TAG, "No pending session to publish");
      return false;
    }
    Logger.d(LOG_TAG, "Publishing a pending session");
    publishLoginResult(SSOResult.SUCCESS, pendingResponse);
    return true;
  }

  /**
   * Read the temp login response
   *
   * @return user login response saved temporarily
   */
  private UserLoginResponse readPendingLoginResponse() {
    String pendingResponse =
        PreferenceManager.getPreference(GenericAppStatePreference.PENDING_USER_LOGIN_RESPONSE,
            Constants.EMPTY_STRING);
    return JsonUtils.fromJson(pendingResponse, UserLoginResponse.class);
  }

  /**
   * Publishes the login result to SSO singleton
   *
   * @param result        SSO Result
   * @param loginResponse Server login response
   */
  private void publishLoginResult(SSOResult result, UserLoginResponse loginResponse) {
    Logger.d(LOG_TAG, "publish the login result and clear pending response");
    publisher.postLoginResult(result, loginResponse);
    clearPendingLoginResponse();
  }

  /**
   * If account linking is in progress, save the temp response to be used incase process gets
   * killed before committing the session post account linking. Next session can use this preference
   *
   * @param userLoginResponse Login API response
   */
  private void saveLoginResponse(UserLoginResponse userLoginResponse) {
    if (userLoginResponse == null) {
      return;
    }
    String pendingLoginResponse = JsonUtils.toJson(userLoginResponse);
    PreferenceManager.savePreference(GenericAppStatePreference.PENDING_USER_LOGIN_RESPONSE,
        pendingLoginResponse);
  }

  /**
   * Clear the temp login response
   */
  private void clearPendingLoginResponse() {
    PreferenceManager.remove(GenericAppStatePreference.PENDING_USER_LOGIN_RESPONSE);
  }
}