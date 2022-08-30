/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.client;

import androidx.annotation.NonNull;

import com.newshunt.adengine.model.entity.AdsFallbackEntity;
import com.newshunt.adengine.model.entity.BaseAdEntity;
import com.newshunt.adengine.model.entity.BaseDisplayAdEntity;
import com.newshunt.adengine.model.entity.MultipleAdEntity;
import com.newshunt.adengine.model.entity.version.AdClubType;
import com.newshunt.adengine.model.entity.version.AdPosition;
import com.newshunt.adengine.util.AdLogger;
import com.newshunt.adengine.util.AdsUtil;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Clubs all ads with same group id into one ad ordered by sdk-order field for fallback.
 *
 * @author shreyas.desai
 */
public class AdClassifier {
  private static final String LOG_TAG = "AdClassifier";

  private final List<AdsFallbackEntity> clubbedAds = new ArrayList<>();

  public AdClassifier(final List<BaseDisplayAdEntity> baseDisplayAdEntities) {
    processAds(baseDisplayAdEntities);
  }

  public List<AdsFallbackEntity> getClubbedAds() {
    return clubbedAds;
  }

  public void processAds(final List<BaseDisplayAdEntity> baseDisplayAdEntities) {
    if (CommonUtils.isEmpty(baseDisplayAdEntities)) {
      AdLogger.d(LOG_TAG, "no ads to be processed");
      return;
    }

    AdLogger.d(LOG_TAG, "number of ads to be processed  " + baseDisplayAdEntities.size());
    HashMap<String, List<BaseAdEntity>> clubbedCarouselEntities = new HashMap<>();
    HashMap<String, List<BaseAdEntity>> clubbedEntities = new HashMap<>();

    for (final BaseDisplayAdEntity displayAdEntity : baseDisplayAdEntities) {

      if (displayAdEntity != null) {

        if (AdPosition.PGI == displayAdEntity.getAdPosition()) {
          AdsUtil.saveMinSessionsToPersistSwipeCount(displayAdEntity.getSessionCount());
        }
        AdsUtil.saveCacheCountForGoodNwSpeed(displayAdEntity.getAdPosition(),
            displayAdEntity.getAdCacheGood());
        AdsUtil.saveCacheCountForAverageNwSpeed(displayAdEntity.getAdPosition(),
            displayAdEntity.getAdCacheAverage());
        AdsUtil.saveCacheCountForSlowNwSpeed(displayAdEntity.getAdPosition(),
            displayAdEntity.getAdCacheSlow());

        if (displayAdEntity.getClubType() == AdClubType.SEQUENCE) {
          clubAdsWithSameGroupId(clubbedCarouselEntities, displayAdEntity);
          continue;
        }
        clubAdsWithSameGroupId(clubbedEntities, displayAdEntity);
      }
    }

    // group sequential ads and drop it in correct fallback bucket.
    for (List<BaseAdEntity> clubbedEntity : clubbedCarouselEntities.values()) {
      MultipleAdEntity multipleAdEntity = new MultipleAdEntity();

      for (BaseAdEntity adEntity : clubbedEntity) {
        multipleAdEntity.addBaseDisplayAdEntity((BaseDisplayAdEntity) adEntity);
        multipleAdEntity.setAdContentType(adEntity.getType());
      }
      AdLogger.d(LOG_TAG, "number of ads in carousel Ad " + multipleAdEntity
          .getBaseDisplayAdEntities().size());
      clubAdsWithSameGroupId(clubbedEntities, multipleAdEntity);
    }

    // Combine ads in same group to a single fallback entity.
    for (List<BaseAdEntity> clubbedEntity : clubbedEntities.values()) {
      AdsFallbackEntity adsFallbackEntity = new AdsFallbackEntity();
      adsFallbackEntity.setClubType(AdClubType.SEQUENCE);
      for (BaseAdEntity adEntity : clubbedEntity) {
        adsFallbackEntity.addBaseAdEntity(adEntity);
        if (adEntity.getClubType() == AdClubType.FALLBACK) {
          adsFallbackEntity.setClubType(AdClubType.FALLBACK);
        }
      }

      Collections.sort(adsFallbackEntity.getBaseAdEntities(), baseAdComparator);
      AdLogger.d(LOG_TAG, "number of ads in clubbed entity for fallback : " +
          adsFallbackEntity.getBaseAdEntities().size());
      clubbedAds.add(adsFallbackEntity);
    }
  }

  Comparator<BaseAdEntity> baseAdComparator =
      (lhs, rhs) -> Integer.compare(lhs.getSdkOrder(), rhs.getSdkOrder());

  private void clubAdsWithSameGroupId(
      @NonNull final HashMap<String, List<BaseAdEntity>> clubbedEntities,
      @NonNull final BaseAdEntity displayAdEntity) {

    final String key = displayAdEntity.getAdGroupId();
    if (!clubbedEntities.containsKey(key)) {
      clubbedEntities.put(key, new ArrayList<>());
    }
    clubbedEntities.get(key).add(displayAdEntity);

    AdLogger.d(LOG_TAG,
        "Adding to group id : " + key + " ad with type :" +
            displayAdEntity.getType());
  }
}
