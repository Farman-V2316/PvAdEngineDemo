package com.newshunt.adengine.model;

import com.newshunt.adengine.model.entity.ExternalSdkAd;

/**
 * Tells whether request for external sdk ads was successful or not
 *
 * @author heena.arora
 */
public interface ExternalAdResponse {
  public void onResponse(ExternalSdkAd externalSdkAd);
}