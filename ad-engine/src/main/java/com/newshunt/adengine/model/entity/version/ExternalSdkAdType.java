package com.newshunt.adengine.model.entity.version;

/**
 * Different types of external sdk ads
 *
 * @author heena.arora
 */
public enum ExternalSdkAdType {
  DFP_STANDARD("DFP-Standard"),
  DFP_INTERSTITIAL("DFP-Interstitial"),
  DFP_NATIVE("DFP-Native"),
  DFP_NATIVE_APP_DOWNLOAD("DFP-Native-AppDownload"),
  DFP_NATIVE_CONTENT("DFP-Native-Content"),
  DFP_NATIVE_INTERSTITIAL("DFP-Native-Interstitial"),
  DFP_CUSTOM_NATIVE("DFP-Custom-Native"),
  FB_NATIVE_AD("FB-Native"),
  FB_INTERSTITIAL_AD("FB-Interstitial"),
  FB_NATIVE_INTERSTITIAL("FB-Native-Interstitial"),
  FB_NATIVE_BID("FB-Native-Bid"),
  FB_NATIVE_INTERSTITIAL_BID("FB-Native-Interstitial-Bid"),
  INLINE_VIDEO_AD("inline-vdo-ad"),
  AMAZON_STANDARD("Amazon-Standard"),
  AMAZON_INTERSTITIAL("Amazon-Interstitial"),
  IMA_SDK("IMA-SDK");

  private String adType;

  public String getAdType() {
    return adType;
  }

  ExternalSdkAdType(String adType) {
    this.adType = adType;
  }

  public static ExternalSdkAdType fromAdType(String adType) {
    for (ExternalSdkAdType externalSdkAdType : ExternalSdkAdType.values()) {
      if (externalSdkAdType.adType.equalsIgnoreCase(adType)) {
        return externalSdkAdType;
      }
    }

    return null;
  }
}
