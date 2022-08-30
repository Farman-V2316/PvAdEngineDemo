/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.sso.model.entity;

/**
 * Enum for different possible results
 *
 * @author arun.babu
 */
public enum SSOResult {
  CANCELLED,
  UNEXPECTED_ERROR,
  BACKGROUND_NOT_APPLICABLE,
  SESSION_INVALID,
  LOGIN_INVALID,
  SUCCESS,
  NETWORK_ERROR
}
