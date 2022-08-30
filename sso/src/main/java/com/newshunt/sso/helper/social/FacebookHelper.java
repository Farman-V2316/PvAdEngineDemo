/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.helper.social;

import android.content.Intent;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider;
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo;
import com.newshunt.sso.R;

import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.Fragment;

/**
 * This class is designed to perform the common operations using the Facebook SDK
 *
 * @author amit.winjit
 */
public class FacebookHelper {

  private static final String FACEBOOK_PACKAGE = "com.facebook.katana";
  private static final CallbackManager CALLBACK_MANAGER = CallbackManager.Factory.create();
  private Callback callback;
  private static final String LOG_TAG = "FacebookHelper";


  public FacebookHelper(final Fragment fragment) {
    try {
      FacebookSdk.sdkInitialize(CommonUtils.getApplication());
      callback = (Callback) fragment;
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  /**
   * This method logs into Facebook via Facebook app or WebView
   */
  public void login(final Fragment fragment) {
    /* Register the Callback to receive the result of Facebook Login */
      LoginManager.getInstance().registerCallback(CALLBACK_MANAGER,
        new FacebookCallback<LoginResult>() {
          @Override
          public void onSuccess(LoginResult loginResult) {
            // success callback - here we can redirect to the required fragment (meaning: successful login)
            if (loginResult.getAccessToken() != null) {
              Logger.d(LOG_TAG, "Login success, token obtained");
              // Facebook SDK stores the current access token in SharedPreferences. So we don't
              // need to store it explicitly
              callback.onFacebookLogin(loginResult.getAccessToken().getToken(),
                  loginResult.getAccessToken().getUserId());
              LoginManager.getInstance().registerCallback(CALLBACK_MANAGER, null);
            } else {
              Logger.e(LOG_TAG, "Login success but token is null!!");
              FontHelper.showCustomFontToast(fragment.getContext(),
                  fragment.getString(R.string.unexpected_error_message), Toast.LENGTH_SHORT);
              callback.onFacebookLoginError();
              LoginManager.getInstance().registerCallback(CALLBACK_MANAGER, null);
            }
          }

          @Override
          public void onCancel() {
            callback.onFacebookLoginCancelled();
            Logger.e(LOG_TAG, "login cancelled");
          }

          @Override
          public void onError(FacebookException e) {
            //handles FB account switching issue
            Logger.e(LOG_TAG, "Login onError: "+e.getMessage());
            if (e instanceof FacebookAuthorizationException) {
              if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
                login(fragment);
              } else {
                callback.onFacebookLoginFailed(CommonUtils.getString(R.string.error_generic));
                LoginManager.getInstance().registerCallback(CALLBACK_MANAGER, null);
              }
            } else {
              callback.onFacebookLoginFailed(e.getMessage());
              LoginManager.getInstance().registerCallback(CALLBACK_MANAGER, null);
            }
          }
        });

    // Permission request for reading "public_profile" and other permissions from Facebook user
    List<String> accessPermissions;
    AdsUpgradeInfo adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().getAdsUpgradeInfo();
    if (null != adsUpgradeInfo && !CommonUtils.isEmpty(adsUpgradeInfo.getFacebookPermissions())) {
      accessPermissions = adsUpgradeInfo.getFacebookPermissions();
    } else {
      accessPermissions = Arrays.asList(CommonUtils.getStringArray(R.array.fb_permissions));
    }
    try {
      /* Call the Facebook login with the required permissions */
      Logger.d(LOG_TAG,"beginning login, first logout of an existing session if any");
      LoginManager.getInstance().logOut();
      LoginManager.getInstance().logInWithReadPermissions(fragment, accessPermissions);
    } catch (Exception e) {
      Logger.caughtException(e);
      if (callback != null) {
        callback.onFacebookLoginFailed(CommonUtils.getString(R.string.error_generic));
        LoginManager.getInstance().registerCallback(CALLBACK_MANAGER, null);
      }
    }
  }

  /**
   * This method takes the callback from Activity, post Facebook operations and
   * calls the respective onSuccess(), onCancel(), onError() methods implemented in this class
   *
   * @param requestCode : request code
   * @param resultCode  : result code
   * @param data        : intent data
   */
  public boolean callbackFromActivity(int requestCode, int resultCode, Intent data) {
    return CALLBACK_MANAGER.onActivityResult(requestCode, resultCode, data);
  }

  public interface Callback {
    void onFacebookLogin(String token, String userId);

    void onFacebookLoginError();

    void onFacebookLoginCancelled();

    void onFacebookLoginFailed(String errorMessage);
  }
}