/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.helper.social;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.sso.R;
import com.newshunt.sso.model.entity.SSOResult;
import com.newshunt.sso.presenter.SignOutPresenter;
import com.newshunt.sso.view.fragment.PlayServicesUpdateDialog;

import javax.annotation.Nullable;

import androidx.fragment.app.Fragment;

import static com.newshunt.common.helper.common.Constants.REQ_CODE_GOOGLE;

/**
 * This class is designed to perform the sign up using Google Auth Sdk.
 *
 * @author anshul.jain
 */
public class GoogleSignInHelper implements GoogleApiClient.OnConnectionFailedListener,
    GoogleApiClient.ConnectionCallbacks {

  /* Activity reference used to pass the Login result to the calling Activity */
  private Fragment fragment;

  /* Client used to interact with Google APIs. */
  private GoogleApiClient googleApiClient;

  private LoginCallback loginCallback;
  @Nullable
  private LogoutCallback logoutCallback;
  private boolean isSignIn;
  private final String TAG = "GoogleSignInHelper";

  public GoogleSignInHelper(final Fragment fragment) {
    this.fragment = fragment;
    this.isSignIn = true;
    try {
      loginCallback = (LoginCallback) fragment;
    } catch (Exception e) {
      Logger.caughtException(e);
      return;
    }
    init(fragment.getContext());
  }

  public GoogleSignInHelper(@Nullable final SignOutPresenter signOutPresenter, Context context) {
    this.isSignIn = false;
    try {
      logoutCallback = signOutPresenter;
    } catch (Exception e) {
      Logger.caughtException(e);
      return;
    }
    init(context);
  }

  private void init(Context context) {
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        //TODO: check if requestEmail is required?
        .requestEmail()
        .requestIdToken(Constants.WEB_CLIENT_ID)
        .build();

    googleApiClient = new GoogleApiClient.Builder(context)
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();

  }

  /**
   * This method logs into Google account.
   */
  public void login() {
    isSignIn = true;
    if (!googleApiClient.isConnected()) {
      connectGoogleApiClient();
    } else {
      loginInGoogleAccount();
    }
  }

  public void logout() {
    isSignIn = false;
    if (!googleApiClient.isConnected()) {
      connectGoogleApiClient();
    } else {
      logoutFromGoogleAccount();
    }
  }

  private void loginInGoogleAccount() {
    if (fragment != null) {
      Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
      fragment.startActivityForResult(signInIntent, REQ_CODE_GOOGLE);
    } else if (loginCallback != null) {
      loginCallback.onGoogleLoginError(SSOResult.UNEXPECTED_ERROR);
    }
  }

  private void logoutFromGoogleAccount() {
    //Signs out the user from the account.
    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
        new ResultCallback<Status>() {
          @Override
          public void onResult(Status status) {

            if (logoutCallback == null) {
              return;
            }
            if (status == null) {
              logoutCallback.onGoogleLogoutFromClientFailure();
              return;
            }
            if (status.isSuccess()) {
              logoutCallback.onGoogleLogoutFromClientSuccess();
              Logger.d(TAG, "succesfully logged out of Google Account");
            } else {
              logoutCallback.onGoogleLogoutFromClientFailure();
              Logger.d(TAG,
                  "Google logout failed due to the following reason --> " + status.toString());
            }
            disconnectAndUnregisterApiClient();
          }
        });
  }

  public void handleSignInResult(Intent data) {

    if (loginCallback == null) {
      return;
    }

    if (data == null) {
      loginCallback.onGoogleLoginError(SSOResult.UNEXPECTED_ERROR);
      return;
    }
    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
    if (result == null) {
      loginCallback.onGoogleLoginError(SSOResult.UNEXPECTED_ERROR);
      return;
    }

    if (result.isSuccess()) {
      // Signed in successfully, get idToken and userID.
      GoogleSignInAccount acct = result.getSignInAccount();
      if (acct == null) {
        loginCallback.onGoogleLoginError(SSOResult.UNEXPECTED_ERROR);
        return;
      }
      String userID = acct.getId();
      String idToken = acct.getIdToken();
      loginCallback.onGoogleLoginSuccess(idToken, userID);
      Logger.d(TAG, "Succesfully logged in Google for the email --> " + acct.getEmail() + " " +
          " idToken --> " + idToken + " userID -->" + userID);

    } else {
      // Login not successful.
      Status status = result.getStatus();
      if (status == null) {
        loginCallback.onGoogleLoginError(SSOResult.UNEXPECTED_ERROR);
        return;
      }
      Logger.d(TAG, "Google login in failed with the following status  " + status.toString());

      if (!CommonUtils.isNetworkAvailable(CommonUtils.getApplication())) {
        loginCallback.onGoogleLoginError(SSOResult.NETWORK_ERROR);
      } else if (status.isCanceled()) {
        loginCallback.onGoogleLoginCancelled();
      } else if (status.getStatusCode() == CommonStatusCodes.INVALID_ACCOUNT) {
        loginCallback.onGoogleLoginFailed();
      } else {
        loginCallback.onGoogleLoginError(SSOResult.UNEXPECTED_ERROR);
      }
    }
    disconnectAndUnregisterApiClient();
  }


  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    SSOResult ssoResult = getErrorMessageForConnectionFailed(connectionResult);
    loginCallback.onGoogleLoginError(ssoResult);
  }

  @Override
  public void onConnected(Bundle bundle) {
    if (isSignIn) {
      loginInGoogleAccount();
    } else {
      logoutFromGoogleAccount();
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    if (googleApiClient != null) {
      connectGoogleApiClient();
    }
  }

  private void connectGoogleApiClient() {
    googleApiClient.registerConnectionCallbacks(this);
    googleApiClient.connect();
  }

  private void disconnectAndUnregisterApiClient() {
    googleApiClient.disconnect();
    googleApiClient.unregisterConnectionCallbacks(this);
  }

  private SSOResult getErrorMessageForConnectionFailed(ConnectionResult result) {
    SSOResult ssoResult = SSOResult.UNEXPECTED_ERROR;
    if (result == null) {
      return ssoResult;
    }
    switch (result.getErrorCode()) {

      case ConnectionResult.NETWORK_ERROR:
        ssoResult = SSOResult.NETWORK_ERROR;
        break;
      case ConnectionResult.INVALID_ACCOUNT:
        ssoResult = SSOResult.LOGIN_INVALID;
        break;

      case ConnectionResult.INTERNAL_ERROR:
      case ConnectionResult.SIGN_IN_FAILED:
      case ConnectionResult.TIMEOUT:
      default:
        ssoResult = SSOResult.UNEXPECTED_ERROR;
        break;
    }
    return ssoResult;
  }

  /**
   * Returns true if the version of the google play services installed on the client is
   * compatible with the library version, false otherwise.
   *
   * @param context
   * @return
   */
  public static boolean arePlayServicesAvailable(Context context) {
    GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
    if (googleApiAvailability == null) {
      return false;
    }
    int result = googleApiAvailability.isGooglePlayServicesAvailable(context);
    return (result == ConnectionResult.SUCCESS);
  }

  /**
   * This method will show a pop-up to update the Google Play services app if internet is available.
   *
   * @param context
   */
  public static void showPlayServiceUpdateDialog(Activity context) {
    if (context == null) {
      return;
    }
    if (!CommonUtils.isNetworkAvailable(context)) {
      FontHelper.showCustomFontToast(context, CommonUtils.getString(R.string.no_connection_error),
          Toast.LENGTH_LONG);
      return;
    }
    FragmentManager manager = context.getFragmentManager();
    PlayServicesUpdateDialog dialog = PlayServicesUpdateDialog.newInstance();
    dialog.show(manager, "GoogleSignInHelper");
  }

  public interface LoginCallback {
    void onGoogleLoginSuccess(String token, String userId);

    void onGoogleLoginError(SSOResult ssoResult);

    void onGoogleLoginFailed();

    void onGoogleLoginCancelled();
  }

  public interface LogoutCallback {
    void onGoogleLogoutFromClientSuccess();

    void onGoogleLogoutFromClientFailure();
  }

}