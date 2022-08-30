/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper.handler;

import android.app.Activity;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.NHCommand;
import com.newshunt.common.view.view.UniqueIdHelper;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dhutil.helper.nhcommand.NHActivityCommandHandler;
import com.newshunt.dhutil.helper.preference.AstroPreference;
import com.newshunt.appview.R;
import com.newshunt.news.util.NewsConstants;
import com.newshunt.dataentity.news.view.entity.Gender;
import com.newshunt.news.view.listener.AstroSubscriptionView;
import com.newshunt.news.view.listener.NewsDetailPhotoClickListener;
import com.newshunt.dataentity.notification.NavigationType;

/**
 * NHCommand handler for news
 *
 * @author maruti.borker
 */
public class NewsNHActivityCommandHandler implements NHActivityCommandHandler {

  private int uniqueRequestId;

  public NewsNHActivityCommandHandler() {
    uniqueRequestId = UniqueIdHelper.getInstance().generateUniqueId();
  }

  @Override
  public boolean handle(NHCommand command, String params, Activity parentActivity, Fragment
      fragment, PageReferrer pageReferrer) {
    if (command == null) {
      return false;
    }

    if (parentActivity == null && fragment != null) {
      parentActivity = fragment.getActivity();
    }

    if (parentActivity == null) {
      return false;
    }

    switch (command) {
      case CHANGE_NEWSPAPER:
        openSourcesTab(parentActivity, pageReferrer);
        return true;
      case OPEN_CATEGORY:
        // TODO handle the open category as per the new code
        return true;
      case OPEN_NEWSPAPER:
        openNewsPaper(params, parentActivity, pageReferrer);
        return true;
      case OPEN_TOPIC:
        openTopic(params, parentActivity, pageReferrer);
        return true;
      case OPEN_WEB_ITEM_RESOURCE:
        return openWebItemResource(params, fragment);
      case SUBSCRIPTION_ACTIONS:
        return handleAstroActions(params, fragment);
      case SELECTED_GENDER:
        return handleAstroSelectedGender(params, fragment);
      case STORY_PHOTO_CLICK:
        return handleStoryPhotoClick(params, fragment);
    }
    return false;
  }

  private boolean openWebItemResource(String params, Fragment fragment) {

    if (!(fragment instanceof NhCommandCallback)) {
      return false;
    }

    if (DataUtil.isEmpty(params) || !params.contains(Constants.EQUAL_CHAR)) {
      return false;
    }

    int indexOfEqual = params.indexOf(Constants.EQUAL_CHAR);
    String webItemResourceId = params.substring(indexOfEqual + 1);

    ((NhCommandCallback) fragment).openWebItemResource(webItemResourceId);

    return true;
  }

  private void openNewsPaper(String paramsString, Activity parentActivity,
                             PageReferrer pageReferrer) {
    Intent intent = new Intent(Constants.ENTITY_OPEN_ACTION);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    String newspaper, category = null, webResourceId = null;

    if (!paramsString.contains("#")) {
      newspaper = paramsString;
    } else {
      String[] params = paramsString.split("#");
      newspaper = params[0];
      category = params[1];
      if (params.length > 2) {
        webResourceId = params[2];
      }
    }

    intent.putExtra(NewsConstants.ENTITY_TYPE, "epaper");
    intent.putExtra(NewsConstants.ENTITY_KEY, newspaper);
    if (!DataUtil.isEmpty(category)) {
      intent.putExtra(NewsConstants.SUB_ENTITY_KEY, category);
    }
    if (!DataUtil.isEmpty(webResourceId)) {
      intent.putExtra(NewsConstants.BUNDLE_WEB_RESOURCE_ID, webResourceId);
    }
    intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    parentActivity.startActivity(intent);
  }

  private void openSourcesTab(Activity parentActivity, PageReferrer pageReferrer) {
    Intent intent = new Intent(Constants.NEWS_HOME_ACTION);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    intent.putExtra(NewsConstants.INTENT_NEWS_HOME_TAB,
        parentActivity.getString(R.string.newspapers_third_tab));
    intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    parentActivity.startActivity(intent);
  }


  private void openTopic(String topicKey, Activity parentActivity, PageReferrer pageReferrer) {
    Intent intent = new Intent(NewsConstants.INTENT_ACTION_LAUNCH_NEWS_HOME_ROUTER);
    intent.setPackage(CommonUtils.getApplication().getPackageName());
    intent.putExtra(NewsConstants.TOPIC_KEY, topicKey);
    intent.putExtra(NewsConstants.BUNDLE_ACTIVITY_REFERRER, pageReferrer);
    intent.putExtra(Constants.BUNDLE_NAVIGATION_TYPE, NavigationType.TYPE_OPEN_TOPIC.name());
    parentActivity.startActivity(intent);
  }

  private boolean handleAstroActions(String params, Fragment fragment) {
    if (CommonUtils.isEmpty(params)) {
      return false;
    }
    switch (params) {
      case DailyhuntConstants.CLICK_DATE_PICKER:
        return handleAstroDatePickerClick(fragment);
      case DailyhuntConstants.CLICK_SUBSCRIBE_BUTTON:
        return handleAstroSubscribeButton(fragment);
      case DailyhuntConstants.CLICK_CROSS_BUTTON:
        return handleAstroCrossButton(fragment);
      case DailyhuntConstants.CLICK_EDIT_BUTTON:
        return handleAstroEditButton(fragment);
    }
    return false;
  }

  /**
   * Called when the gender button is invoked from the client.
   *
   * @param genderStr
   * @param parentFragment
   * @return
   */
  private boolean handleAstroSelectedGender(String genderStr, Fragment parentFragment) {
    if (CommonUtils.isEmpty(genderStr)) {
      return false;
    }
    Gender gender = Gender.getGender(genderStr);
    if (gender == null) {
      return false;
    }
    if (!(parentFragment instanceof AstroSubscriptionView)) {
      return false;
    }
    ((AstroSubscriptionView) parentFragment).onGenderSelected(genderStr);
    return true;
  }

  private boolean handleStoryPhotoClick(String url, Fragment parentFragment) {

    if (CommonUtils.isEmpty(url) || !(parentFragment instanceof NewsDetailPhotoClickListener)) {
      return false;
    }

    ((NewsDetailPhotoClickListener) parentFragment).handleStoryPhotoClick(url);
    return true;
  }

  /**
   * Called when the date picker is invoked from the html page.
   *
   * @param fragment
   * @return
   */
  private boolean handleAstroDatePickerClick(Fragment fragment) {
    if (!(fragment instanceof AstroSubscriptionView)) {
      return false;
    }
    ((AstroSubscriptionView) fragment).onDatePickerClicked();
    return true;
  }

  /**
   * Called when the subscribe button is invoked from the html page.
   *
   * @param parentFragment
   * @return
   */
  private boolean handleAstroSubscribeButton(Fragment parentFragment) {
    if (!(parentFragment instanceof AstroSubscriptionView)) {
      return false;
    }

    String gender =
        PreferenceManager.getPreference(AstroPreference.USER_GENDER, Constants.EMPTY_STRING);
    String dob =
        PreferenceManager.getPreference(AstroPreference.USER_DOB, Constants.EMPTY_STRING);
    ((AstroSubscriptionView) parentFragment).onSubscriptionButtonClicked(gender, dob);
    return true;
  }

  /**
   * Called when the cross button is invoked from the html page.
   *
   * @param parentFragment
   * @return
   */
  private boolean handleAstroCrossButton(Fragment parentFragment) {
    if (!(parentFragment instanceof AstroSubscriptionView)) {
      return false;
    }

    ((AstroSubscriptionView) parentFragment).onAstroCrossButtonClicked();
    return true;
  }

  /**
   * Called when the edit button is invoked from the html page.
   *
   * @param parentFragment
   * @return
   */
  private boolean handleAstroEditButton(Fragment parentFragment) {
    if (!(parentFragment instanceof AstroSubscriptionView)) {
      return false;
    }

    ((AstroSubscriptionView) parentFragment).onAstroEditButtonClicked();
    return true;
  }

}
