/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.model.entity;

import com.newshunt.adengine.model.entity.version.AdClubType;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Collection of ads arranged in sequence for being processed in fallback model.
 *
 * @author raunak.yadav
 */
public class AdsFallbackEntity {
  private List<BaseAdEntity> ads = new ArrayList<>();
  private AdClubType clubType;

  public List<BaseAdEntity> getBaseAdEntities() {
    return ads;
  }

  public void setBaseAdEntities(List<BaseAdEntity> adEntities) {
    this.ads = adEntities;
  }

  public void addBaseAdEntity(BaseAdEntity baseAdEntity) {
    ads.add(baseAdEntity);
  }

  public String getAdGroupId() {
    if (CommonUtils.isEmpty(ads)) {
      return null;
    }
    return ads.get(0).getAdGroupId();
  }

  public void setClubType(AdClubType clubType) {
    this.clubType = clubType;
  }

  public AdClubType getClubType() {
    return clubType;
  }
}