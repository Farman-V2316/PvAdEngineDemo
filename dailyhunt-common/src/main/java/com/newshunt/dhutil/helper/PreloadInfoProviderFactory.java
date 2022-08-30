/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper;

import android.os.Build;
import androidx.annotation.Nullable;

/**
 * Factory class to check the manufacturer and instantiate the right PreloadInfoProvider
 * <p>
 * Created by srikanth.ramaswamy on 04/30/2018.
 */
public class PreloadInfoProviderFactory {

  private static final String MANUFACTURER_XIAOMI = "Xiaomi";
  private static final String MANUFACTURER_HUAWEI = "HUAWEI";
  private static final String MANUFACTURER_OPPO = "OPPO";
  private static final String MANUFACTURER_REALME = "realme";

  @Nullable
  public static PreloadInfoProvider create() {
    String manufacturer = Build.MANUFACTURER;
    if (MANUFACTURER_XIAOMI.equalsIgnoreCase(manufacturer)) {
      return new XiaomiPreloadInfoProvider();
    } else if (MANUFACTURER_HUAWEI.equalsIgnoreCase(manufacturer)) {
      return new HuaweiPreloadInfoProvider();
    } else if (MANUFACTURER_OPPO.equalsIgnoreCase(manufacturer) ||
            MANUFACTURER_REALME.equalsIgnoreCase(manufacturer)) {
      return new OppoPreloadInfoProvider();
    }

    return null;
  }
}
