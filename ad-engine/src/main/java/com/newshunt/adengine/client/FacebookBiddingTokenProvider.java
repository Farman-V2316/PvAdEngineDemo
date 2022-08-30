/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
*/
package com.newshunt.adengine.client;

import com.facebook.ads.BidderTokenProvider;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Provides the Bidding ID from Facebook's audience network SDK.
 * Created by srikanth on 23/01/18.
 */

public class FacebookBiddingTokenProvider {
  private String biddingTokenID = Constants.EMPTY_STRING;
  private static FacebookBiddingTokenProvider instance;
  private static final String LOG_TAG = "FBTokenProvider";

  private FacebookBiddingTokenProvider() {
  }

  public static FacebookBiddingTokenProvider getInstance() {
    if (instance == null) {
      synchronized (FacebookBiddingTokenProvider.class) {
        if (instance == null) {
          instance = new FacebookBiddingTokenProvider();
        }
      }
    }
    return instance;
  }

  public String getBiddingToken() {
    String tokenID = readBiddingTokenID();
    if (!CommonUtils.isEmpty(tokenID)) {
      return tokenID;
    }

    Logger.d(LOG_TAG, "FB bidding token not available yet, try fetching in background");
    CommonUtils.runInBackground(new Runnable() {
      @Override
      public void run() {
        //check again if bidding token is still not available
        if (CommonUtils.isEmpty(readBiddingTokenID())) {
          String tokenID = BidderTokenProvider.getBidderToken(CommonUtils.getApplication());
          Logger.d(LOG_TAG, "FB bidding token available, saving it!");
          try {
            tokenID = URLEncoder.encode(tokenID, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
          writeBiddingTokenID(tokenID);
        }
      }
    });
    return Constants.EMPTY_STRING;
  }

  private synchronized String readBiddingTokenID() {
    return biddingTokenID;
  }

  private synchronized void writeBiddingTokenID(final String tokenID) {
    biddingTokenID = tokenID;
  }
}
