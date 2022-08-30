/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.deeplink.navigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.analytics.referrer.NhGenericReferrer;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.dataentity.analytics.referrer.SocialCommentReferrer;
import com.newshunt.dhutil.analytics.RunTimeReferrer;
import com.newshunt.dataentity.notification.SocialCommentsModel;
import com.newshunt.socialfeatures.helper.analytics.SocialTabAnalyticsInfo;
import com.newshunt.socialfeatures.util.SocialFeaturesConstants;

/**
 * @author santhosh.kc
 * <p>
 * TODO(santosh.D) to convert into kotlin
 */
public class SocialCommentsNavigator {

  public static Intent getViewAllCommentsIntent(Context context,
                                                SocialCommentsModel socialCommentsModel,
                                                PageReferrer pageReferrer) {
    Intent intent = new Intent(Constants.ALL_COMMENTS_ACTION);
    intent.putExtra(Constants.BUNDLE_COMMENTS_MODEL, socialCommentsModel);
    intent.putExtra(Constants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.setPackage(AppConfig.getInstance().getPackageName());
    return intent;
  }

  public static SocialTabAnalyticsInfo extractAnalyticsParametersFromBundle(
      Bundle bundle) {
    if (bundle == null) {
      return null;
    }

    SocialTabAnalyticsInfo socialReferrerInfo;

    //Deeplink Flow
    SocialCommentsModel socialCommentsModel = (SocialCommentsModel) bundle.getSerializable(Constants
        .BUNDLE_COMMENTS_MODEL);
    if (socialCommentsModel != null) {
      socialReferrerInfo = new SocialTabAnalyticsInfo();
      socialReferrerInfo.setSection(
          NhAnalyticsEventSection.getSection(socialCommentsModel.getSection()));
      socialReferrerInfo.setTabItemId(socialCommentsModel.getReferrerId());
      socialReferrerInfo.setReferrer(socialCommentsModel.getReferrer());
      socialReferrerInfo.setReferrerId(socialCommentsModel.getReferrerId());
      return socialReferrerInfo;
    }

    socialReferrerInfo = (SocialTabAnalyticsInfo) bundle.getSerializable(SocialFeaturesConstants
        .BUNDLE_BASE_ASSET_ANALYTICS_PARAMS);
    return socialReferrerInfo;
  }

  public static PageReferrer getPageReferrer(SocialTabAnalyticsInfo socialTabAnalyticsInfo,
                                             boolean isViewAllReplies) {
    if (socialTabAnalyticsInfo == null) {
      return null;
    }
    return new PageReferrer(isViewAllReplies ? NhGenericReferrer
        .CARD_WIDGET : SocialCommentReferrer.COMMENTS, isViewAllReplies ?
        SocialCommentReferrer.COMMENT.getReferrerName() :
        socialTabAnalyticsInfo.getReferrerId());
  }

  public static PageReferrer getStoryListViewReferrer(SocialTabAnalyticsInfo
                                                          socialTabAnalyticsInfo) {
    if (socialTabAnalyticsInfo == null) {
      return null;
    }

    RunTimeReferrer runTimeReferrer = new RunTimeReferrer(socialTabAnalyticsInfo.getReferrer(),
        null);
    return new PageReferrer(runTimeReferrer, socialTabAnalyticsInfo.getReferrerId(), null,
        NhAnalyticsUserAction.CLICK);
  }

}
