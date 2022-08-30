/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dailyhunt.huntlytics.sdk.NHAnalyticsAgent;
import com.google.gson.Gson;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.PasswordEncryption;
import com.newshunt.common.helper.preference.AppCredentialPreference;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.track.DailyhuntUtils;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.model.entity.LoginType;
import com.newshunt.dataentity.sso.model.entity.UserLoginResponse;
import com.newshunt.deeplink.navigator.SSONavigator;
import com.newshunt.dhutil.commons.listener.ReferrerProviderlistener;
import com.newshunt.sso.helper.SSOLoginSourceTracker;
import com.newshunt.sso.helper.preference.SSOPreference;
import com.newshunt.sso.model.entity.LoginMode;
import com.newshunt.sso.model.entity.LoginResult;
import com.newshunt.sso.model.entity.LogoutResult;
import com.newshunt.sso.model.entity.SSOLoginSourceType;
import com.newshunt.sso.model.entity.SSOResult;
import com.newshunt.sso.model.entity.SSOUserState;
import com.newshunt.sso.model.entity.SessionPayload;
import com.newshunt.sso.presenter.SSOGuestPresenter;
import com.newshunt.sso.presenter.SSOPresenter;
import com.newshunt.sso.presenter.SignOutPresenter;
import com.newshunt.sso.view.view.SignOutView;
import com.squareup.otto.Bus;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.backup.BackupManager;

/**
 * Utility class for Single Sign On
 *
 * @author arun.babu
 */
public class SSO {

  private static volatile SSO instance;
  private final String TAG = SSO.class.getSimpleName();
  private final Bus uiBus = BusProvider.getUIBusInstance();
  private final UserDetails userDetails = new UserDetails();
  private final Publisher publisher = new Publisher();
  private final SSOGuestPresenter ssoGuestPresenter;
  private final SSOPresenter ssoPresenter;
  private final SSOLoginSourceTracker ssoLoginSourceTracker;
  private WeakReference<Activity> activitySoftReference;
  private volatile SSOUserState ssoUserState;
  private MutableLiveData<UserDetails> userDetailsLiveData = new MutableLiveData<>();

  private SSO() {
    ssoGuestPresenter = new SSOGuestPresenter(publisher);
    ssoPresenter = new SSOPresenter(publisher);
    ssoPresenter.registerBus();
    ssoLoginSourceTracker = new SSOLoginSourceTracker();
    init();
  }

  public static SSO getInstance() {
    if (instance == null) {
      synchronized (SSO.class) {
        if (instance == null) {
          instance = new SSO();
        }
      }
    }
    return instance;
  }

  public static String getUserName() {
    return getInstance().userDetails.getUserName();
  }

  public static boolean isCreator() {
    return getInstance().userDetails.isCreator();
  }

  public static LoginType getLoginType() {
    return getInstance().userDetails.getLoginType();
  }

  @Nullable
  public static UserLoginResponse getLoginResponse() {
    return getInstance().userDetails.userLoginResponse;
  }

  private void init() {
    if (!ssoPresenter.checkAndInitPendingSession()) {
      userDetails.read();
      userDetailsLiveData.postValue(userDetails);
      setSSOUserState(userDetails.getLoginType() == LoginType.NONE ? SSOUserState.LOGGED_OUT :
          SSOUserState.LOGGED_IN);
    }
  }

  /**
   * Set the {@link SSOUserState}
   */
  private synchronized void setSSOUserState(SSOUserState ssoUserState) {
    this.ssoUserState = ssoUserState;
  }

  public UserDetails getUserDetails() {
    return userDetails;
  }

  public String getEncryptedSessionData() {
    //Construct Sessiondata based on userLogin information
    SessionPayload sessionPayload = new SessionPayload(userDetails);
    String sessionData = null;
    try {
      sessionData = new Gson().toJson(sessionPayload);
      sessionData = PasswordEncryption.encrypt(sessionData, false); //Encrypting user data
    } catch (Exception e) {
      //If anything fails during Encryption, lets no expose user data
      sessionData = null; //Reterofit will not set header if sessionData is null
      Logger.caughtException(e);
    }
    return sessionData;
  }

  public boolean isLoggedIn(boolean mayBeGuest) {
    return (userDetails.getLoginType() != LoginType.NONE) &&
        (mayBeGuest || userDetails.getLoginType() != LoginType.GUEST);
  }

  public void login(Activity activity, LoginMode loginMode, SSOLoginSourceType loginSourceType,
                    @Nullable ReferrerProviderlistener referrerProviderlistener) {
    if (!DailyhuntUtils.isRegisterOrFirstHandshakeDoneInThisVersion()) {
      //No login should happen before register/handshake.
      return;
    }
    this.activitySoftReference = new WeakReference<>(activity);

    //Hold the SSO login Requests for the Particular SESSION , and publish it back to Listeners
    ssoLoginSourceTracker.setSSOLoginSource(loginSourceType, loginMode);
    Logger.d(TAG, "Set SSO Login Source : " + loginSourceType.toString());
    if (LoginMode.USER_EXPLICIT == loginMode && isLoggedIn(false)) {
      //If user has already logged with login type other than guest, then try normal login
      loginMode = LoginMode.NORMAL;
    }

    /**
     * SSO Modifies the Login Type based on Login_Type and Login_Mode
     *
     * Case a) : NONE / GUEST  User Login_Type
     * i) Login Mode : BackGround/Normal
     *     --- Do a  Login in as Guest
     * ii) Login Mode : Explicit
     *     --  Show a Explicit UI , asking for User to input Credentials
     *
     * Case b) : NORMAL User Login_Type
     * i) Login Mode : Background
     *     -- Try for a Session Expiry , if fail Sit Quiet
     *
     * ii) Login Mode : Normal
     *     -- Try for Session Expiry(Background) --> Succeed --> OK
     *                                           --> Failed --> Do a AUTH Request --> OK
     *
     * SSO UserState on top of SSO , if any pending LOGIN Requests are in progress will allow
     * Higher Priority Requests only
     * (Login Type --  None /Guest User) , We allow EXPLICIT Login Mode
     * (Login Type -- Normal User) , We allow NORMAL Login Mode
     */

    if (LoginType.NONE == userDetails.getLoginType() ||
        LoginType.GUEST == userDetails.getLoginType()) {
      switch (loginMode) {
        case BACKGROUND_ONLY:
        case NORMAL:
          if (ssoUserState == SSOUserState.LOG_IN_PROGRESS ||
              ssoUserState == SSOUserState.LOG_OUT_PROGRESS) {
            Logger.d(TAG, "Guest user Login is already in Progress");
            return;
          }
          ssoGuestPresenter.loginAsGuest();
          break;
        case USER_EXPLICIT:
          PageReferrer pageReferrer = null;
          if (activity instanceof ReferrerProviderlistener) {
            pageReferrer = ((ReferrerProviderlistener) activity).getLatestPageReferrer();
          } else if (referrerProviderlistener != null) {
            pageReferrer = referrerProviderlistener.getLatestPageReferrer();
          }
          SSONavigator.launchSignInActivity(activity, LoginType.NONE, pageReferrer);
          break;
        default:
      }
      return;
    }

    if (LoginMode.BACKGROUND_ONLY == loginMode) {
      if (ssoUserState == SSOUserState.LOG_IN_PROGRESS ||
          ssoUserState == SSOUserState.LOG_OUT_PROGRESS) {
        Logger.d(TAG, "Normal user Login(Background) is already in Progress");
        return;
      }
      loginForType(userDetails.getLoginType(), true);
    } else {
      loginForType(userDetails.getLoginType(), false);
    }

    setSSOUserState(SSOUserState.LOG_IN_PROGRESS);
  }


  public void login(Activity activity, LoginMode loginMode, SSOLoginSourceType loginSourceType) {
    login(activity, loginMode, loginSourceType, null);
  }

  public void loginSocialMandatory() {
    SSONavigator.startSocialMandatory(null);
  }

  private void loginForType(LoginType loginType, boolean isBackgroundOnly) {

    if (loginType == null) {
      return;
    } else if (isBackgroundOnly && loginType == LoginType.MOBILE && PreferenceManager.getPreference(
        GenericAppStatePreference.DISABLE_TRUE_CALLER_LOGIN, false)) {
      return;
    }
    switch (loginType) {

      case FACEBOOK:
      case GOOGLE:
      case MOBILE:
        SSONavigator.startSocialMandatory(activitySoftReference.get());
        break;
    }
  }


  public void logout(LoginType loginType, SignOutPresenter signOutPresenter, SignOutView signOutView) {
    //If Logout call is initiated , Irrespective of the sso login state , Do LOGOUT
    setSSOUserState(SSOUserState.LOG_OUT_PROGRESS);
    signOutPresenter.logout(loginType, signOutView);
  }

  public void setSSOLoginSourceTracker(SSOLoginSourceType loginSourceType,
                                       LoginMode loginMode) {
    ssoLoginSourceTracker.setSSOLoginSource(loginSourceType, loginMode);
  }

  public static class UserDetails {

    public static final String GUEST = "GUEST";

    private UserLoginResponse userLoginResponse;

    private UserDetails() {
      //Private constructor to avoid external instantiation
    }

    private void setDetails(UserLoginResponse userLoginResponse) {
      if (userLoginResponse == null) {
        return;
      }
      this.userLoginResponse = userLoginResponse;
      NHAnalyticsAgent.setUserId(userLoginResponse.getUserId());
      String response = JsonUtils.toJson(userLoginResponse);
      PreferenceManager.savePreference(GenericAppStatePreference.USER_LOGIN_RESPONSE, response);
      AppUserPreferenceUtils.saveUserId(this.userLoginResponse.getUserId());
      PreferenceManager.savePreference(SSOPreference.USER_DATA, userLoginResponse.getUserData());
      try {
        BackupManager.dataChanged(CommonUtils.getApplication().getPackageName());
      } catch (Exception e) {
        Logger.caughtException(e);
      }
      PreferenceManager.savePreference(AppCredentialPreference.USER_ID,
          userLoginResponse.getUserId());
      SSO.saveUserProfile(userLoginResponse.getUserId(), userLoginResponse.getProfileImage());
      if (userLoginResponse.getUserAccountType() != null) {
        PreferenceManager.savePreference(GenericAppStatePreference.USER_LOGIN_TYPE,
            userLoginResponse.getUserAccountType().getValue());
      }
      AppUserPreferenceUtils.saveUserType(userLoginResponse.isCreator());
    }

    public LoginType getLoginType() {
      if (userLoginResponse != null && userLoginResponse.getUserAccountType() != null) {
        return userLoginResponse.getUserAccountType();
      }
      return LoginType.NONE;
    }

    public boolean isCreator() {
      if (userLoginResponse == null) {
        return false;
      }
      return userLoginResponse.isCreator();
    }


    public String getUserID() {
      if (userLoginResponse == null) {
        return null;
      }
      return userLoginResponse.getUserId();
    }

    @Nullable
    public String getUserName() {
      if (userLoginResponse == null) {
        return null;
      }
      return userLoginResponse.getName();
    }


    private void read() {
      String response =
          PreferenceManager.getPreference(GenericAppStatePreference.USER_LOGIN_RESPONSE,
              Constants.EMPTY_STRING);
      userLoginResponse = new Gson().fromJson(response, UserLoginResponse.class);
      if (userLoginResponse != null && !CommonUtils.isEmpty(userLoginResponse.getUserId())) {
        NHAnalyticsAgent.setUserId(userLoginResponse.getUserId());
      }
    }


    public @Nullable
    UserLoginResponse getUserLoginResponse() {
      return userLoginResponse;
    }
  }

  public class Publisher {

    private Publisher() {
      //Private constructor to avoid external instantiation
    }

    /**
     * Method to determine , whether the existing UserId matches with new UserId / not
     *
     * @param newUserId -- New User Id
     * @return -- true /false
     */
    private boolean isUserChanged(String newUserId) {
      if (!CommonUtils.isEmpty(newUserId) && newUserId.equals(AppUserPreferenceUtils.getUserId())) {
        return false;
      } else {
        return true;
      }
    }

    public void postGuestLoginResult(SSOResult ssoResult, UserLoginResponse response) {
      boolean isUserChanged = isUserChanged(response.getUserId());
      userDetails.setDetails(response);
      addLoginSourcetypeIfMissing();
      LoginResult loginResult = new LoginResult(ssoResult, userDetails, isUserChanged,
          ssoLoginSourceTracker.getSSOLoginSourceTypes());
      setSSOUserState(userDetails.getLoginType() == LoginType.NONE ? SSOUserState.LOGGED_OUT :
          SSOUserState.LOGGED_IN);
      uiBus.post(loginResult);
      userDetailsLiveData.setValue(userDetails);
      ssoLoginSourceTracker.resetSSOLoginSources();
    }


    public void postLoginResult(SSOResult ssoResult, UserLoginResponse userLoginResponse) {
      if (userLoginResponse == null) {
        return;
      }
      boolean isUserChanged = isUserChanged(userLoginResponse.getUserId());
      userDetails.setDetails(userLoginResponse);
      addLoginSourcetypeIfMissing();
      LoginResult loginResult = new LoginResult(ssoResult, userDetails, isUserChanged,
          ssoLoginSourceTracker.getSSOLoginSourceTypes());
      setSSOUserState(userDetails.getLoginType() == LoginType.NONE ? SSOUserState.LOGGED_OUT :
          SSOUserState.LOGGED_IN);
      uiBus.post(loginResult);
      userDetailsLiveData.setValue(userDetails);
      ssoLoginSourceTracker.resetSSOLoginSources();
    }


    public void postLoginResult(SSOResult ssoResult) {
      addLoginSourcetypeIfMissing();
      LoginResult loginResult = new LoginResult(ssoResult, userDetails, false,
          ssoLoginSourceTracker.getSSOLoginSourceTypes());
      setSSOUserState(userDetails.getLoginType() == LoginType.NONE ? SSOUserState.LOGGED_OUT :
          SSOUserState.LOGGED_IN);
      uiBus.post(loginResult);
      userDetailsLiveData.setValue(userDetails);
      ssoLoginSourceTracker.resetSSOLoginSources();
    }

    public void postLogoutResult(SSOResult result) {
      postLogoutResult(result, null);
    }

    public void postLogoutResult(SSOResult result, UserLoginResponse userLoginResponse) {
      LogoutResult logoutResult = new LogoutResult(result);
      if (result.equals(SSOResult.SUCCESS)) {
        logoutResult.setLastLoggedUser(userLoginResponse.getUserId());
        userDetails.setDetails(userLoginResponse);
        userDetailsLiveData.setValue(userDetails);
      }
      setSSOUserState(userDetails.getLoginType() == LoginType.NONE ? SSOUserState.LOGGED_OUT :
          SSOUserState.LOGGED_IN);
      uiBus.post(logoutResult);
    }
  }

  public static void saveUserProfile(String profileId, String pictureUrl) {
    PreferenceManager.savePreference(SSOPreference.PROFILEID, profileId);
    PreferenceManager.savePreference(SSOPreference.PROFILEPIC, pictureUrl);
  }

  public static String getPrefProfileId() {
    return PreferenceManager.getPreference(SSOPreference.PROFILEID, Constants.EMPTY_STRING);
  }

  public static String getPrefProfilePic() {
    return PreferenceManager.getString(SSOPreference.PROFILEPIC.getName(), Constants.EMPTY_STRING);
  }

  public @NonNull
  LiveData<UserDetails> getUserDetailsLiveData() {
    return userDetailsLiveData;
  }

  public @NonNull Publisher getPublisher() {
    return publisher;
  }

  private void addLoginSourcetypeIfMissing() {
    List<SSOLoginSourceType> sourceTypes = ssoLoginSourceTracker.getSSOLoginSourceTypes();
    if (CommonUtils.isEmpty(sourceTypes)) {
      ssoLoginSourceTracker.setSSOLoginSource(SSOLoginSourceType.IMPLICIT,
          LoginMode.BACKGROUND_ONLY);
    }
  }

  public void checkAndInitPendingSession() {
    ssoPresenter.checkAndInitPendingSession();
  }
}
