/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper;

/**
 * Interface to read the install source, preload status etc. Each device manufacturer specific
 * code could written in separate classes implementing this interface
 * <p>
 * Created by srikanth.ramaswamy on 04/30/2018.
 */
public interface PreloadInfoProvider {
  String getInstallSource();
}
