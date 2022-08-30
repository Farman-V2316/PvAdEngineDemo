/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.common.model.entity;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * @author shrikant.agrawal
 */
public interface TabEntity extends Serializable {

  @Nullable
  String getName1();

  String getTabType();

  String getTabId();

  @Nullable
  String getTabLayout();
}
