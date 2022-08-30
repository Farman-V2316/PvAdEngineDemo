/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.common.model.entity.model;

import java.io.Serializable;

/**
 * Represents a status object having application specific code, message and
 * detail description of the message. It can be used for error or a user message
 * representation while giving api response.
 *
 * @author amarjit
 */
public class Status implements Serializable {

  private static final long serialVersionUID = 3201935778952169221L;

  /**
   * Represents the application specific error code
   */
  private String code;

  /**
   * Represents the message to be shown to the api user
   */
  private String message;

  /**
   * Represents the detail description to be shown to the api user
   */

  private StatusError codeType;

  public Status() {
  }

  @Deprecated
  public Status(int code) {
    this(Integer.toString(code), null);
  }

  public Status(String code) {
    this(code, null);
  }

  @Deprecated
  public Status(int code, String message) {
    this(Integer.toString(code), message);
  }

  public Status(String code, String message) {
    this(code, message, StatusError.fromName(code));
  }

  public Status(String code, String message, StatusError codeType) {
    this.code = code;
    this.message = message;
    this.codeType = codeType;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public StatusError getCodeType() {
    return codeType;
  }

  public void setCodeType(StatusError codeType) {
    this.codeType = codeType;
  }


  @Override
  public String toString() {
    return new StringBuilder().append(getClass()).append(" [").append("code=").append(code)
        .append(", message=").append(message).append(", description=").append("]").toString();
  }
}
