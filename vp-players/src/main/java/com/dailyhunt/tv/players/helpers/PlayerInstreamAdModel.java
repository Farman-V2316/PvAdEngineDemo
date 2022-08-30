package com.dailyhunt.tv.players.helpers;

import com.newshunt.adengine.model.entity.BaseAdEntity;
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity;

public class PlayerInstreamAdModel {

  static PlayerInstreamAdModel __instance;
  BaseDisplayAdEntity baseDisplayAdEntity;

  private PlayerInstreamAdModel() {

  }

  public static PlayerInstreamAdModel getInstance() {
    if (__instance == null) {
      synchronized ((PlayerInstreamAdModel.class)) {
        if (__instance == null) {
          __instance = new PlayerInstreamAdModel();
        }
      }
    }
    return __instance;
  }

  public void setBaseDisplayAdEntity(BaseDisplayAdEntity baseDisplayAdEntity) {
    this.baseDisplayAdEntity = baseDisplayAdEntity;
  }

  public BaseDisplayAdEntity getBaseDisplayAdEntity() {
    return baseDisplayAdEntity;
  }
}
