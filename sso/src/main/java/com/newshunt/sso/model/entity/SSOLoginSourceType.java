package com.newshunt.sso.model.entity;

/**
 * Created by ranjith.suda on 1/7/2016.
 * <p/>
 * Enum saying Possible Login Sources Within the Applications
 *
 * //TODO(refactor) , Need to define Proper SSO Sources
 */
public enum SSOLoginSourceType {

  //Unknown State of SSOLoginSource
  UNKNOWN,

  //Generic ,In Common for Books / Test Prep
  HAMBURGER,
  REVIEW,

  //Special Case , as Guest Login is initiated Here
  USER_LOGOUT,

  PROFILE_HOME,

  GROUP_SCREENS,

  API_401_RESPONSE,

  SIGN_IN_PAGE,

  IMPLICIT, //When the session is initiated via Guest login implicitly on app launch/handshake

  CREATE_POST, //Create post screen

  ACCOUNTS_LINKING
}
