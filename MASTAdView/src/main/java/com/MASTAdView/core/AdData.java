//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved.
//
package com.MASTAdView.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.MASTAdView.MASTAdConstants;
import com.MASTAdView.MASTAdLog;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.sdk.network.NetworkExecutorService;
import com.newshunt.sdk.network.NetworkSDK;
import com.newshunt.sdk.network.Priority;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

final public class AdData implements Serializable {
  // recognized ad type names
  public static final String typeNameImage = "image";
  public static final String typeNameText = "text";
  public static final String typeNameRichMedia = "richmedia";
  public static final String typeNameThirdParty = "thirdparty";
  public static final String typeNameExternalThirdParty = "externalthirdparty";

  public Integer adType = MASTAdConstants.AD_TYPE_UNKNOWN;
  public String thirdPartyFeed = null;
  public String clickUrl = null;
  public String text = null;
  public String imageUrl = null;
  volatile public Bitmap imageBitmap = null;
  public String trackUrl = null;
  public String richContent = null;
  public String error = null;
  public Integer serverErrorCode = null;
  public List<NameValuePair> externalCampaignProperties = null;
  public String responseData = null;
  public String useDHFont = null;

  // relative path used for giving reference on WebView load Data
  public String mBasePath = null;
  public String mAdCachedPath = null;
  // metaTag used for MraidCompliant Ads can be controlled from NH AdServer
  public String mMetaData = null;

  // variable meant for enabling transparency for resized ad Background
  public boolean mIsTransparentBgForResizedRichAd = false;

  public static InputStream fetchUrl(String fromUrl, AdData aAdData) {
    return fetchUrl(fromUrl, null, aAdData);
  }

  // Get content from provided URL; NOTE: This runs in the current thread context;
  // never call this from the UI thread!!!
  public static InputStream fetchUrl(String fromUrl, String userAgent,
                                     AdData aAdData) {
    try {
      Request.Builder requestBuilder = new Request.Builder();
      requestBuilder.url(fromUrl);
      // Always include our user agent header, if needed
      if (CommonUtils.isEmpty(userAgent)) {
        requestBuilder.addHeader("User-Agent", System.getProperty("http.agent"));
      } else {
        requestBuilder.addHeader("User-Agent", userAgent);
      }
      Request request = requestBuilder.build();

      OkHttpClient okHttpClient = NetworkSDK.newClient(Priority.PRIORITY_NORMAL, null);
      Call call = okHttpClient.newCall(request);
      Response response = call.execute();
      if (response != null) {
        if (response.isSuccessful() && response.code() == Constants.HTTP_SUCCESS
            && response.body() != null) {
          return new OkHttpInputStreamWrapper(response);
        } else {
          response.close();
        }
      }
    } catch (IOException e) {
      MASTAdLog logger = new MASTAdLog(null);
      logger.log(MASTAdLog.LOG_LEVEL_ERROR, "AdData.fetchUrl exception",
          e.getMessage());
    }

    // if the control came here the fetching failed, so try local
    // path/relative path
    return fetchLocalUrl(fromUrl, aAdData);
  }

  private static InputStream fetchLocalUrl(String fromUrl, AdData aAdData) {
    InputStream retInputstream = null;
    try {
      if (null != aAdData && null != aAdData.mAdCachedPath
          && aAdData.mAdCachedPath.length() > 0) {
        retInputstream = new FileInputStream(aAdData.mAdCachedPath
            + fromUrl);
      }
    } catch (Exception e) {
      retInputstream = null;
    }
    return retInputstream;
  }

  public static void sendImpressionOnThread(final String url, final String userAgent) {
    if ((url == null) || (url.length() < 1)) {
      return;
    }

    InputStream is = fetchUrl(url, userAgent, null);
    if (is != null) {
      try {
        is.close();
      } catch (Exception ex) {
        // error closing stream... bleh!
      }
    }

  }

  public static void sendImpressionInBackground(final String url,
                                                final String userAgent) {
    if ((url == null) || (url.length() < 1)) {
      return;
    }

    NetworkExecutorService networkExecutorService = new NetworkExecutorService(Priority
        .PRIORITY_NORMAL, null);
    networkExecutorService.submit(new Runnable() {
      @Override
      public void run() {
        sendImpressionOnThread(url, userAgent);
      }
    });
  }

  public static Bitmap fetchImage(String url) {
    InputStream is = fetchUrl(url, null);

    if (is != null) {
      Bitmap image = BitmapFactory.decodeStream(is);

      try {
        is.close();
      } catch (Exception ex) {
        // error closing stream... bleh!
      }

			/*
       * if (image != null) {
			 * System.out.println("fetchImage: Image decoded, size: " +
			 * image.getWidth() + "x" + image.getHeight()); }
			 */

      return image;
    }

    return null;
  }

  synchronized public boolean hasContent() {
    if (adType != null) {
      if ((adType == MASTAdConstants.AD_TYPE_TEXT) && (text != null)
          && (text.length() > 0)) {
        return true;
      }

      if ((adType == MASTAdConstants.AD_TYPE_IMAGE)
          && ((imageBitmap != null) || (imageUrl != null))) {
        return true;
      }

      if ((adType == MASTAdConstants.AD_TYPE_RICHMEDIA)
          && (richContent != null) && (richContent.length() > 0)) {
        return true;
      }

      if ((adType == MASTAdConstants.AD_TYPE_THIRDPARTY)
          && (richContent != null) && (richContent.length() > 0)) {
        return true;
      }

      if ((adType == MASTAdConstants.AD_TYPE_EXTERNAL_THIRDPARTY)
          && (externalCampaignProperties != null)
          && (externalCampaignProperties.size() > 0)) {
        return true;
      }
    }

    return false;
  }

  synchronized public void setAdTypeByName(String typeName) {
    if (typeName.compareTo(typeNameImage) == 0) {
      adType = MASTAdConstants.AD_TYPE_IMAGE;
    } else if (typeName.compareTo(typeNameText) == 0) {
      adType = MASTAdConstants.AD_TYPE_TEXT;
    } else if (typeName.compareTo(typeNameRichMedia) == 0) {
      adType = MASTAdConstants.AD_TYPE_RICHMEDIA;
    } else if (typeName.compareTo(typeNameThirdParty) == 0) {
      adType = MASTAdConstants.AD_TYPE_THIRDPARTY;
    } else if (typeName.compareTo(typeNameExternalThirdParty) == 0) {
      adType = MASTAdConstants.AD_TYPE_EXTERNAL_THIRDPARTY;
    } else {
      adType = MASTAdConstants.AD_TYPE_UNKNOWN;
    }
  }

  synchronized public String getAdTypeName() {
    switch (adType) {
      case MASTAdConstants.AD_TYPE_IMAGE:
        return typeNameImage;
      case MASTAdConstants.AD_TYPE_TEXT:
        return typeNameText;
      case MASTAdConstants.AD_TYPE_RICHMEDIA:
        return typeNameRichMedia;
      case MASTAdConstants.AD_TYPE_THIRDPARTY:
        return typeNameThirdParty;
      case MASTAdConstants.AD_TYPE_EXTERNAL_THIRDPARTY:
        return typeNameExternalThirdParty;
      default:
        return null;
    }
  }

  synchronized public void setImage(final String url, boolean prefetch) {
    imageUrl = url;
    if (prefetch) {
      NetworkExecutorService networkExecutorService =
          new NetworkExecutorService(Priority.PRIORITY_NORMAL, null);
      networkExecutorService.submit(new Runnable() {
        @Override
        public void run() {
          imageBitmap = fetchImage(url);
        }
      });
    }
  }

  synchronized public void setImage(String url, Bitmap bitmap) {
    imageUrl = url;
    imageBitmap = bitmap;
  }

  synchronized public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("Ad: type=" + getAdTypeName());

    if (adType == MASTAdConstants.AD_TYPE_TEXT) {
      sb.append(", text=" + text);
    } else if (adType == MASTAdConstants.AD_TYPE_IMAGE) {
      sb.append(", url=" + imageUrl);
    } else if (adType == MASTAdConstants.AD_TYPE_RICHMEDIA) {
      sb.append(", richContent=" + richContent);
    } else if (adType == MASTAdConstants.AD_TYPE_THIRDPARTY) {
      sb.append(", feed=" + thirdPartyFeed + ", richContent="
          + richContent);
    } else if (adType == MASTAdConstants.AD_TYPE_EXTERNAL_THIRDPARTY) {
      sb.append(", external campaign properties="
          + externalCampaignProperties.toString());
    }

    if (clickUrl != null) {
      sb.append(", clickUrl=" + clickUrl);
    }

    if (error != null) {
      sb.append(", ERROR=" + error);
    }

    return sb.toString();
  }
}
