/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.adengine.client;

import com.dailyhunt.huntlytics.sdk.NHAnalyticsSession;
import com.newshunt.adengine.model.entity.version.AdRequest;
import com.newshunt.adengine.model.entity.version.AmazonSdkPayload;
import com.newshunt.adengine.util.AdConstants;
import com.newshunt.adengine.util.AdFrequencyStats;
import com.newshunt.adengine.util.AdLogger;
import com.newshunt.adengine.util.AdStatisticsHelper;
import com.newshunt.adengine.util.AdsUtil;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.util.LangInfoRepo;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo;
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider;
import com.newshunt.permissionhelper.utilities.PermissionUtils;
import com.newshunt.sdk.network.Priority;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import in.dailyhunt.money.contentContext.ContentContext;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Fetches ad from the server.
 *
 * @author shreyas.desai
 */
public class AdFetcher {
  private static final String LOG_TAG = "AdFetcher";
  private AdRequester adRequester;
  private Map<String, Call> pendingRequests;

  public AdFetcher(AdRequester adRequester) {
    this.adRequester = adRequester;
    this.pendingRequests = new ConcurrentHashMap<>();
  }

  public int getPendingListSize() {
    return pendingRequests.size();
  }

  public void clearPendingRequests() {
    if (pendingRequests == null || pendingRequests.size() == 0) {
      return;
    }
    Iterator<Map.Entry<String, Call>> iterator = pendingRequests.entrySet().iterator();
    while (iterator.hasNext()) {
      Call pendingCall = iterator.next().getValue();
      if (pendingCall != null) {
        pendingCall.cancel();
      }
      iterator.remove();
    }
  }

  public void run(final AdRequest adRequest, String excludeBanners, final int uniqueRequestId,
                  Priority priority) {
    final String randomUniqueId = UUID.randomUUID().toString();
    final Map<String, ContentContext> contentContext = adRequest.getContentContextMap();
    final Map<String, ContentContext> parentContentContext = adRequest.getParentContextMap();
    final String adExtras = adRequest.getAdExtras();
    final AmazonSdkPayload amazonSdkPayload = adRequest.getAmazonSdkPayload();

    Map<String, String> bodyParams = new HashMap<>();
    bodyParams.put(AdConstants.AD_REQ_EXCLUDE_BANNERS, excludeBanners);
    AdsUpgradeInfo adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().getAdsUpgradeInfo();
    if (adsUpgradeInfo != null && adsUpgradeInfo.isEnableFBBidding()) {
      bodyParams.put(AdConstants.AD_REQ_FB_TOKEN,
          FacebookBiddingTokenProvider.getInstance().getBiddingToken());
    }

    if (contentContext != null) {
      bodyParams.put(AdConstants.AD_REQ_CONTEXT, JsonUtils.toJson(contentContext));
    }
    if (parentContentContext != null) {
      bodyParams.put(AdConstants.AD_REQ_PARENT_CONTEXT, JsonUtils.toJson(parentContentContext));
    }
    if (!CommonUtils.isEmpty(adExtras)) {
      bodyParams.put(AdConstants.AD_REQ_AD_EXTRA, adExtras);
    }
    if(amazonSdkPayload != null) {
      bodyParams.put(AdConstants.AD_REQ_AMAZON_PAYLOAD, JsonUtils.toJson(amazonSdkPayload));
    }
    String adStats = AdStatisticsHelper.INSTANCE.getDataAsString(NHAnalyticsSession.getSessionId());
    if (!CommonUtils.isEmpty(adStats)) {
      bodyParams.put(AdConstants.AD_REQ_STATS, adStats);
    }

    bodyParams.put(AdConstants.AD_REQ_PERMISSION, PermissionUtils.getUsesPermission());
    bodyParams.put(AdConstants.AD_REQ_FCAP, JsonUtils.toJson(AdFrequencyStats.getFcMetCampaignsFor(uniqueRequestId)));
    bodyParams.put(AdConstants.AD_REQ_LANG_INFO, JsonUtils.toJson(LangInfoRepo.INSTANCE.getNonNullLangInfo()));

    //PV_AD_SERVER_URL
    //final String url = "http://qa-money.newshunt.com/publicVibe/v1/list-ad/native.json";
    final String url = "http://qa-money.newshunt.com/publicVibe/v1/pgi/html.json";

    //PANDA: removed manually for testing
    //final String url = AdsUtil.buildAdServerURL(adRequest);

    Call newRequest = HttpClientManager.newAdRequestCall(url, null, priority);
    //PANDA: removed manually for testing
    //Call newRequest = HttpClientManager.newAdRequestCall(url, bodyParams, priority);
    if (newRequest == null) {
      adRequester.onAdRequestError("Failed to create okHttp request.", uniqueRequestId);
      return;
    }
    AdLogger.d(LOG_TAG, "Request url for " + adRequest.getZoneType() + " : " + url);

    newRequest.enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        pendingRequests.remove(randomUniqueId);
        adRequester.onAdRequestFailedAtServer(adRequest, uniqueRequestId);
      }

      @Override
      public void onResponse(Call call, final Response response) throws IOException {
        pendingRequests.remove(randomUniqueId);
        if (response != null && response.isSuccessful()) {
          adRequester.onAdReceivedFromServer(response.body().string(), adRequest, uniqueRequestId);
        } else {
          adRequester.onAdRequestFailedAtServer(adRequest, uniqueRequestId);
        }
        if (response != null) {
          response.close();
        }
      }
    });
    pendingRequests.put(randomUniqueId, newRequest);
  }

  /**
   * User of ad-fetcher must implement this interface for it
   * to be notified when the response is available.
   */
  public interface AdRequester {
    void onAdReceivedFromServer(String data, AdRequest adRequest, int uniqueRequestId);

    void onAdRequestFailedAtServer(AdRequest adRequest, int uniqueRequestId);

    void onAdRequestError(String errorMessage, int uniqueRequestId);
  }
}
