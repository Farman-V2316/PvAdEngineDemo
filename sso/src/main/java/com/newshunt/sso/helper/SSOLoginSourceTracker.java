package com.newshunt.sso.helper;

import com.newshunt.sso.model.entity.LoginMode;
import com.newshunt.sso.model.entity.SSOLoginSourceType;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ranjith.suda on 1/7/2016.
 * <p/>
 * Class Responsible for Tracking SSO Login Requests
 * <p/>
 * a) Holds the Current Login Source Requests {@link SSOLoginSourceType} and Deliver back on
 * Login Result
 * b) Discard all Other Low priority Pending Login Requests , for this Login Cycle Completion
 */
public class SSOLoginSourceTracker {

  private final ConcurrentHashMap<SSOLoginSourceType, LoginMode> ssoLoginSources =
      new ConcurrentHashMap<>();

  /**
   * Wrapper around SSO , Which sets the Current SSO Login Source
   *
   * @param ssoLoginSourceType -- {@link SSOLoginSourceType}
   * @param loginMode          -- {@link LoginMode}
   */
  public void setSSOLoginSource(SSOLoginSourceType ssoLoginSourceType, LoginMode loginMode) {
    ssoLoginSources.put(ssoLoginSourceType, loginMode);
  }

  /**
   * Wrapper around SSO , which gives Current SSO Login Sources as ArrayList
   *
   * @return -- ArrayList of {@link SSOLoginSourceType}
   */
  public ArrayList<SSOLoginSourceType> getSSOLoginSourceTypes() {
    ArrayList<SSOLoginSourceType> ssoLoginSourceTypes = new ArrayList<>(ssoLoginSources.keySet());
    return ssoLoginSourceTypes;
  }

  /**
   * Wrapper around SSO , to clear/reset the SSO Login Sources
   * Need to be called after Current Login Cycle Completion.
   */
  public void resetSSOLoginSources() {
    ssoLoginSources.clear();
  }
}
