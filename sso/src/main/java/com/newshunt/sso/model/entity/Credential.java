/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;

/**
 * Encode guest user name and password
 *
 * @author arun.babu
 */
public class Credential {
  private String userName;
  private String password;

  public Credential(String userName, String password) {
    this.userName = userName;
    this.password = password;
  }

  public String getUserId() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return "Credential [userName=" + userName + ", password=" + password + "]";
  }
}
