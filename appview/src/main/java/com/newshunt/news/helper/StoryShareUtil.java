/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.NhAnalyticsAppEvent;
import com.newshunt.common.helper.share.ShareContent;
import com.newshunt.common.helper.share.ShareHelper;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsAppEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.appview.R;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.helper.share.ShareFactory;
import com.newshunt.common.helper.share.ShareUTMHelper;
import com.newshunt.common.helper.share.ShareUi;
import com.newshunt.common.helper.share.ShareUtils;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.asset.PostEntity;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.analytics.entity.AnalyticsParam;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.news.analytics.NhAnalyticsNewsEventParam;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

import static com.newshunt.common.helper.common.Constants.NEW_LINE;

/**
 * An utility class to share story of type BaseContentAsset
 * this story  may have a parent story of type BaseContentAsset
 *
 * @author santhosh.kc
 */
public class StoryShareUtil {

  private static final String PLATFORM_DEPENDENT = "pd";

  public static void logShareAnalyticsForNhBrowser(String packageName, ShareUi shareUi,
                                                   String webUrl, String id, String type,
                                                   PageReferrer pageReferrer) {

    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsNewsEventParam.SHARE_TYPE, packageName);
    if (shareUi == ShareUi.FLOATING_ICON) {
      shareUi = ShareUtils.getShareUiForFloatingIcon();
    }
    if (null != shareUi) {
      map.put(NhAnalyticsNewsEventParam.SHARE_UI, shareUi.getShareUiName());
    }

    map.put(NhAnalyticsAppEventParam.WEBITEM_URL, webUrl);
    map.put(AnalyticsParam.ITEM_TYPE, type);
    if (!CommonUtils.isEmpty(id)) {
      map.put(AnalyticsParam.ITEM_ID, id);
    }
    if (pageReferrer != null) {
      map.put(NhAnalyticsAppEventParam.REFERRER_FLOW, pageReferrer.getReferrer());
      map.put(NhAnalyticsAppEventParam.REFERRER_FLOW_ID, pageReferrer.getId());
    }
    AnalyticsClient.log(NhAnalyticsAppEvent.STORY_SHARED, NhAnalyticsEventSection.NEWS, map,
        pageReferrer);
  }

  public static String getShareableString(String shareUrl, String title,
                                          Map<String, String> langTitles,
                                          boolean displayViaDailyhunt) {
    return getShareableString(shareUrl, title, langTitles, displayViaDailyhunt, null);
  }

  public static String getShareableString(String shareUrl, String title,
                                          Map<String, String> langTitles,
                                          boolean displayViaDailyhunt,
                                          @Nullable String sourceName) {
    StringBuilder builder = new StringBuilder();
    try {
      builder.append(String.valueOf(android.text.Html.fromHtml(
          title + Constants.BR_TAG)));
      Uri.Builder uriBuilder = Uri.parse(shareUrl).buildUpon();
      uriBuilder.appendQueryParameter(ShareUTMHelper.SHORT_URL_PARAMETER_SOURCE,
          ShareUTMHelper.SHORT_URL_PARAMETER_VALUE);
      uriBuilder.appendQueryParameter(ShareUTMHelper.SHORT_URL_PARAMETER, PLATFORM_DEPENDENT);
      String token = PreferenceManager.getPreference(AppStatePreference.SHARE_TOKEN, Constants.EMPTY_STRING);
      if (!CommonUtils.isEmpty(token)) {
        uriBuilder.appendQueryParameter(ShareUTMHelper.SHORT_URL_PARAMETER_USER, token);
      }
      builder.append(uriBuilder.toString())
          .append(NEW_LINE);
      if (displayViaDailyhunt) {
        String storyShareFooterText = getStoryShareFooterText(langTitles, sourceName);
        if (!CommonUtils.isEmpty(storyShareFooterText)) {
          builder.append(storyShareFooterText);
        }
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return builder.toString();
  }

  public static Intent getShareIntent(final String shareUrl, final String title) {
    Intent shareIntent = buildBasicShareIntent();
    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getShareableString(shareUrl,
        title, null, true, null));
    return Intent.createChooser(shareIntent, CommonUtils.getString(R.string.share_source));
  }

  public static Intent buildBasicShareIntent() {
    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
    shareIntent.setType(Constants.INTENT_TYPE_TEXT);
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return shareIntent;
  }

  private static String getStoryShareFooterText(Map<String, String> langTitles, String sourceName) {
    return ShareFactory.getStoryShareFooterText(getItemLang(langTitles), sourceName);
  }

  private static String getItemLang(Map<String, String> langTitles) {
    if (langTitles != null && langTitles.size() > 0) {
      Set<String> languages = langTitles.keySet();
      return languages.iterator().next();
    }
    return UserPreferenceUtil.getUserNavigationLanguage();
  }

  /**
   * A utility method for sharing a news entity which can be topic , location, newspaper category etc.
   *
   * @param activity         - The activity from which it is shared
   * @param shareUrl         - Share Url
   * @param shareTitle       - The title of the share .
   * @param shareDescription - The share description.
   * @param packageName      - The package name through which it is shared.
   * @param shareUi          - The share ui ie. floatingIcon etc
   * @param type             - type of shared item
   * @param itemId           - id of the shared item
   * @param experiment       - experimental params
   */
  public static void shareNewsEntity(Activity activity, String shareUrl,
                                     String shareTitle, String shareDescription, String packageName,
                                     ShareUi shareUi, String type, String itemId,
                                     Map<String, String> experiment, PageReferrer pageReferrer) {
    if (activity == null || CommonUtils.isEmpty(shareUrl) || CommonUtils.isEmpty(shareTitle)) {
      return;
    }
    logAnalytics(packageName, shareUi, type, itemId, experiment, pageReferrer);
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setType(Constants.INTENT_TYPE_TEXT);

    ShareContent shareContent = new ShareContent();
    shareContent.setTitle(shareTitle);
    shareContent.setShareUrl(shareUrl);
    shareContent.setContent(shareDescription);
    shareContent.setSubject(shareTitle);
    // setting the lang title as user app language
    shareContent.setLangTitles(UserPreferenceUtil.getUserNavigationLanguage());

    ShareHelper shareHelper = ShareFactory.getShareHelper(
        packageName, activity, sendIntent, shareContent);
    shareHelper.share();
  }

  public static void logAnalytics(String packageName, ShareUi shareUi, String type, String
      itemId, Map<String, String> experiment, PageReferrer pageReferrer) {
    // Log event
    Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    map.put(NhAnalyticsNewsEventParam.SHARE_TYPE, packageName);
    if (null != shareUi) {
      map.put(NhAnalyticsNewsEventParam.SHARE_UI, shareUi.getShareUiName());
    }
    map.put(AnalyticsParam.ITEM_ID, itemId);
    map.put(AnalyticsParam.ITEM_TYPE, type);
    if (pageReferrer != null) {
      map.put(NhAnalyticsAppEventParam.REFERRER_FLOW, pageReferrer.getReferrer());
      map.put(NhAnalyticsAppEventParam.REFERRER_FLOW_ID, pageReferrer.getId());
    }
    AnalyticsClient.logDynamic(NhAnalyticsAppEvent.STORY_SHARED, NhAnalyticsEventSection.NEWS,
        map, experiment, pageReferrer, false);
  }

  public static Intent getWebShareIntent(final String shareUrl,
                                         final PostEntity webItemAsset) {
    Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
    shareIntent.setType(Constants.INTENT_TYPE_TEXT);
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    String title = Constants.EMPTY_STRING;
    if (webItemAsset != null && webItemAsset.getShareParams() != null) {
      title = webItemAsset.getShareParams().getShareTitle();
    }
    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, getShareableString(shareUrl,
        title, null, true, extractSourceName(webItemAsset)));
    return Intent.createChooser(shareIntent, CommonUtils.getString(R.string.share_source));
  }

  private static String extractSourceName(PostEntity postEntity) {
    if (postEntity == null || postEntity.getSource() == null) {
      return null;
    }
    return postEntity.getSource().getDisplayName();
  }
}
