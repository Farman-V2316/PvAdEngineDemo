/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity.server.navigation;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * Represents the possible list of section types
 *
 * @author amarjit
 */
public enum SectionType {

  FEATURED, NORMAL, FAVOURITE, TRENDING;

  public static SectionType fromName(String name) {
    if (CommonUtils.isEmpty(name)) {
      return SectionType.NORMAL;
    }

    for (SectionType sectionType : SectionType.values()) {
      if (sectionType.name().equalsIgnoreCase(name)) {
        return sectionType;
      }
    }

    return SectionType.NORMAL;
  }

}
