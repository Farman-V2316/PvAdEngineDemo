package com.newshunt.adengine.client.requester;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.newshunt.adengine.model.ExternalAdResponse;
import com.newshunt.adengine.model.entity.ExternalSdkAd;

/**
 * Provides method to fetch external native ads
 *
 * @author heena.arora
 */
public interface AdRequester {
  void fetchAd(ExternalAdResponse externalAdResponse, ExternalSdkAd externalSdkAd,
                      Activity activity);
  default void decodeAdMetaData(@NonNull ExternalSdkAd externalSdkAd){
  }
}
