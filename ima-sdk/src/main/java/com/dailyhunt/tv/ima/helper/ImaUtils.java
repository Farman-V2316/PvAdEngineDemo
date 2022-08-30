/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.ima.helper;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo;
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.TvAdData;
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider;
import com.newshunt.sdk.network.connection.ConnectionManager;
import com.newshunt.sdk.network.connection.ConnectionType;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;

import java.util.ArrayList;

/**
 * CommonUtils class for IMA SDK.
 *
 * @author raunak.yadav
 */
public class ImaUtils {

  /**
   * Get maximum bitrate to be used for selecting mediaFile tag in VAST.
   * If none available, IMA will pick the nearest bitrate tag.
   *
   * @return preferred bitrate in kbps
   */
  public static int getMaxBitrateFromHandshake() {
    AdsUpgradeInfo adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().getAdsUpgradeInfo();
    if (adsUpgradeInfo == null || adsUpgradeInfo.getBuzzAd() == null) {
      return -1;
    }

    TvAdData buzzAd = adsUpgradeInfo.getBuzzAd();

    switch (ConnectionManager.getInstance().getCurrentConnectionSpeed(CommonUtils.getApplication())) {
      case GOOD:
      case FAST:
        return buzzAd.getMaxBitrateKbpsGood();
      case AVERAGE:
        return buzzAd.getMaxBitrateKbpsAverage();
      default:
        return buzzAd.getMaxBitrateKbpsSlow();
    }
  }

  /**
   * @return Whether to enable OMID in IMA sdk.
   */
  public static boolean enableOmidExperimentally() {
    AdsUpgradeInfo adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().getAdsUpgradeInfo();
    return adsUpgradeInfo != null && adsUpgradeInfo.isEnableOmidExperimentally();
  }

  public static final String SIZE_TOKEN = "x";
  public  static ArrayList<Pair<Integer, Integer>> getCompanionSize(@NonNull  String sizes) {
    ArrayList<Pair<Integer,Integer>>adSizes = new ArrayList();
    try {
      String[] adSizesArgs = sizes.split(Constants.COMMA_CHARACTER);
      for (String size: adSizesArgs) {
        if (size.contains(SIZE_TOKEN)) {
          String[] args = size.split(SIZE_TOKEN);
          int width = DataUtil.parseInt(args[0], 0);
          int height = DataUtil.parseInt(args[1], 0);
          if (width != 0 && height != 0) {
            adSizes.add(new Pair(width, height));
          }
        }
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return adSizes;
  }

}
