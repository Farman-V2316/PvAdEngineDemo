/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.share;

import android.net.Uri;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.HashMap;

import javax.annotation.Nullable;

/**
 * To get code as per app for share story url.
 *
 * @author sumedh.tambat
 */
public class ShareUTMHelper {

  public static final String SHORT_URL_PARAMETER = "ss";
  public static final String SHORT_URL_PARAMETER_SOURCE = "s";
  public static final String SHORT_URL_PARAMETER_USER = "uu";
  public static final String SHORT_URL_PARAMETER_VALUE = "a";
  public static final String SHORT_URL_PARAMETER_APP_VERSION = "av";


  private static final String PLATFORM_DEPENDENT = "pd";

  private static String getApplicationCode(String packageName) {
    String appName = getPackageMap().get(packageName);
    if (CommonUtils.isEmpty(appName)) {
      return PLATFORM_DEPENDENT;
    } else {
      return appName;
    }
  }

  public static String getShareURL(String url, @Nullable String packageName) {
    Uri.Builder uriBuilder = Uri.parse(url).buildUpon();
    uriBuilder.appendQueryParameter(SHORT_URL_PARAMETER, getApplicationCode(packageName));
    if (packageName != null && (packageName.equalsIgnoreCase("com.facebook.katana") ||
        packageName.equalsIgnoreCase("com.facebook.lite") ||
        packageName.equalsIgnoreCase("com.facebook.orca"))) {
      uriBuilder.appendQueryParameter(SHORT_URL_PARAMETER_APP_VERSION,
          AppConfig.getInstance().getAppVersion());
    }
    return uriBuilder.build().toString();
  }

  private static HashMap<String, String> getPackageMap() {
    final HashMap<String, String> packageMap = new HashMap<>();
    packageMap.put("com.adobe.reader", "adb");
    packageMap.put("com.android.email", "aem");
    packageMap.put("com.bbm", "bbm");
    packageMap.put("com.android.bluetooth", "bth");
    packageMap.put("com.estrongs.android.pop", "esf");
    packageMap.put("com.facebook.katana", "fb");
    packageMap.put("com.facebook.lite", "fbl");
    packageMap.put("com.facebook.orca", "fbm");
    packageMap.put("com.sec.android.app.FileShareClient", "FSC");
    packageMap.put("com.google.android.apps.docs", "gdc");
    packageMap.put("com.google.android.keep", "gkp");
    packageMap.put("com.google.android.gm", "gml");
    packageMap.put("com.google.android.talk", "gtk");
    packageMap.put("com.google.android.apps.translate", "gtns");
    packageMap.put("com.bsb.hike", "hk");
    packageMap.put("com.imo.android.imoim", "imo");
    packageMap.put("com.sec.android.app.memo", "memo");
    packageMap.put("com.samsung.android.app.memo", "memo");
    packageMap.put("com.android.mms", "mms");
    packageMap.put("com.mobisystems.office", "offc");
    packageMap.put("com.lenovo.anyshare.gps", "shrt");
    packageMap.put("com.sec.android.widgetapp.diotek.smemo", "smem");
    packageMap.put("com.majedev.superbeam", "sprb");
    packageMap.put("com.truecaller.messenger", "trcl");
    packageMap.put("com.twitter.android", "twt");
    packageMap.put("com.whatsapp", "wsp");
    packageMap.put("cn.xender", "xndr");
    return packageMap;
  }
}
