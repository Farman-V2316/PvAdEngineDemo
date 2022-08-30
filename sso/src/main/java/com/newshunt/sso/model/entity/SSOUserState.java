package com.newshunt.sso.model.entity;

/**
 * Created by ranjith.suda on 1/7/2016.
 *
 * States Defining at any point of Application , SSO User [Guest / Normal ] can be.
 */
public enum SSOUserState {

  /**
   * State saying , {@link com.newshunt.sso.SSO.UserDetails} is not NONE
   */
  LOGGED_IN,

  /**
   * State saying , {@link com.newshunt.sso.SSO.UserDetails} is NONE
   */
  LOGGED_OUT,

  /**
   * State Saying , {@link LoginMode} is in Progress
   * Here In progress implies , even SESSION_TIME_OUT ,AUTH_ERROR cases
   */
  LOG_IN_PROGRESS,

  /**
   * State saying , SSO LogOut Call is in progress.
   */
  LOG_OUT_PROGRESS
}
