/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.upgrade;

import com.google.gson.reflect.TypeToken;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.view.view.UniqueIdHelper;
import com.newshunt.helper.ImageUrlReplacer;
import com.newshunt.news.helper.ImageQualityInterceptor;
import com.newshunt.news.helper.MigrationUtils;
import com.newshunt.onboarding.helper.HandshakeCompleteEvent;
import com.newshunt.pref.NewsPreference;
import com.newshunt.sdk.network.NetworkSDK;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * An Helper class to perform News Section Events of App Events like register complete, handshake
 * complete etc
 *
 * @author santhosh.kc
 */
public class NewsAppEventUpdateHelper {

  private static final int uniqueRequestId;

  static {
    uniqueRequestId = UniqueIdHelper.getInstance().generateUniqueId();
  }

  public static void onHandshakeComplete(HandshakeCompleteEvent handshakeCompleteEvent) {
    performNewsPageSyncIfUpgrade();
    initNewsImageQualityHeaderInterceptor();
    ImageUrlReplacer.updateMacros();
  }

  private static void performNewsPageSyncIfUpgrade() {
    if (!MigrationUtils.needsPageSyncOnUpgrade()) {
      return;
    }

    MigrationUtils.setNeedsPageSyncedOnUpgrade(false);
  }

  public static void initNewsImageQualityHeaderInterceptor() {
    String qualitiesMapString = PreferenceManager.getPreference(NewsPreference
        .IMAGE_DOWNLOAD_QUALITIES, Constants.EMPTY_STRING);
    ImageQualityInterceptor imageQualityInterceptor = null;
    if (!CommonUtils.isEmpty(qualitiesMapString)) {
      Type type = new TypeToken<HashMap<String, String>>() {

      }.getType();
      Map<String, String> qualitiesMap = JsonUtils.fromJson(qualitiesMapString, type);
      imageQualityInterceptor = new ImageQualityInterceptor(qualitiesMap);
    }
    NetworkSDK.setImageQualityHeaderInterceptor(imageQualityInterceptor);
  }
}
