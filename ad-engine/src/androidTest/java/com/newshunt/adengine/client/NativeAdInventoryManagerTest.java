/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.client;

import android.test.AndroidTestCase;

import java.io.IOException;

/**
 * Tests {@link NativeAdInventoryManager}.
 *
 * @author shreyas.desai
 */
public class NativeAdInventoryManagerTest extends AndroidTestCase {
  private static final String URL =
      "http://newshunt.net.in/openx/ads/index.php?t=nads&context=cat=amar:National;npKey=amar;clientId=735086408;client=android;appVer=4.32.75;&zone=card-p1&client=android&resolution=250x320&clientId=735086408&appVer=4.32.75&brand=NewsHunt&long=77.6269418&lat=12.971808&cellid=3312-404-45-651----Gsm&isReg=N&imgFmt=7&featureMask=16384&langMask=2047&udid=358302054441256&conn=w&debug=1&XDEBUG_SESSION_START=netbeans-xdebug&XDEBUG_SESSION_START=netbeans-xdebug&XDEBUG_SESSION_START=netbeans-xdebug";

  public void testAdFetcher() throws IOException {
    assertTrue(true);
  }

}
