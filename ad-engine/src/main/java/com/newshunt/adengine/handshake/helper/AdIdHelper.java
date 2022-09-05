/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.adengine.handshake.helper;

import android.util.Pair;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.info.ClientInfoHelper;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * Helper class for fetching Google Ad Id
 *
 * @author karthik.r
 */
public class AdIdHelper {

  public static Pair<String, Boolean> getAdIdInfo() {
    AdvertisingIdClient.Info idInfo = null;
    Pair<String, Boolean> response;
    try {
      idInfo = AdvertisingIdClient.getAdvertisingIdInfo(CommonUtils.getApplication());
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    String googleAdId;
    boolean gaidOptOutStatus;
    if (idInfo != null) {
      gaidOptOutStatus = idInfo.isLimitAdTrackingEnabled();
      googleAdId = gaidOptOutStatus ? Constants.EMPTY_STRING : idInfo.getId();
      response = new Pair<>(googleAdId, gaidOptOutStatus);
    } else {
      response = new Pair<>(Constants.EMPTY_STRING, true);
    }
    ClientInfoHelper.setGoogleAdId(response.first);
    ClientInfoHelper.setGaidOptOutStatus(response.second);

    return response;
  }
}
