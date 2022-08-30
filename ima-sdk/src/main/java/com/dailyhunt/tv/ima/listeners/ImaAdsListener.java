/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.ima.listeners;

import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.CompanionAdSlot;

import java.util.List;

/**
 * @author raunak.yadav
 */
public interface ImaAdsListener extends AdEvent.AdEventListener {
  void setCompanionAdSlots(List<CompanionAdSlot> companionAdSlots);
}