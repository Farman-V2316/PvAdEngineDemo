/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;

/**
 * Single Sign On Logout Response
 *
 * @author arun.babu
 */
public class LogoutResult {
  private final SSOResult result;
  private String lastLoggedUser;

  public LogoutResult(SSOResult result) {
    this.result = result;
  }

  public SSOResult getResult() {
    return result;
  }

  public void setLastLoggedUser(String lastLoggedUser) {
    this.lastLoggedUser = lastLoggedUser;
  }

  public String getLastLoggedUser() {
    return lastLoggedUser;
  }
}
