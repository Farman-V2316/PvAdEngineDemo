/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.dhutil.model.versionedapi;

/**
 * @author shrikant.agrawal
 */
public enum VersionMode {

  CACHE,
  CACHE_AND_UPDATE,
  FETCH_IF_NOT_IN_CACHE,
  NETWORK;
}
