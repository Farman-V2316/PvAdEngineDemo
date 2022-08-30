/**
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.news.view.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.deeplink.navigator.CommonNavigator;
import com.newshunt.dhutil.helper.CustomTabActivityHelper;
import com.newshunt.news.view.fragment.SavedArticleCoachMarkDialogFragment;
import com.newshunt.news.view.view.SavedArticlesPromptView;
import com.newshunt.sdk.network.internal.NetworkSDKUtils;

import java.lang.ref.WeakReference;

/**
 * This class is to be extended by all activities having either news list or news details
 * (activities that display list of cards)
 * Takes cares of handling dialogs and other UI common to above activities.
 *
 * @author sntosh.dhanyamraju
 */
public class NewsListDetailActivity extends NewsBaseActivity implements
    SavedArticlesPromptView, SavedArticleCoachMarkDialogFragment.Adapter {
  private static final String SAVED_ARTICLE_PROMPT = "savedArticlePrompt";
  private static final int DISMISS_COACHMARK = 1;
  private static final long STICKY_DISPLAY_TIME_MILLIS = 15000L;
  private static final String TAG = "NewsListDetailActivity";

  private final CustomTabActivityHelper mCustomTabActivityHelper = new CustomTabActivityHelper();

  private SavedArticleCoachMarkDialogFragment coachMarkDialogFragment;
  private Handler handler;
  private ConnectivityManager connectivityManager;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    handler = new CoachmarkHandler(coachMarkDialogFragment);
  }

  static class CoachmarkHandler extends Handler {
    private WeakReference<SavedArticleCoachMarkDialogFragment> coachMarkDialogFragmentRef;

    public CoachmarkHandler(SavedArticleCoachMarkDialogFragment coachMarkDialogFragment) {
      coachMarkDialogFragmentRef = new WeakReference<>(coachMarkDialogFragment);
    }

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case DISMISS_COACHMARK:
          //dismiss is causing exception when fragment animates out, after activity pause
          SavedArticleCoachMarkDialogFragment coachMarkDialogFragment =
              coachMarkDialogFragmentRef.get();
          if (coachMarkDialogFragment != null) {
            coachMarkDialogFragment.dismissAllowingStateLoss();
            coachMarkDialogFragmentRef.clear();
          }
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    mCustomTabActivityHelper.bindCustomTabsService(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mCustomTabActivityHelper.unbindCustomTabsService(this);
  }

  @Override
  public Context getViewContext() {
    return this;
  }


  @Override
  public void showSavedArticlePrompt() {
    if (NetworkSDKUtils.isLastKnownConnection()) {
      return;
    }

    if (coachMarkDialogFragment != null) { // after dismissing, it will be set to null
      Logger.d(TAG, "showSavedArticlePrompt : already showing");
      return;
    }
    coachMarkDialogFragment = SavedArticleCoachMarkDialogFragment.createInstance(this);
    /*
    Fix for the following crash
    https://fabric.io/verse-innovation-pvt-ltd--bangalore/android/apps/com.eterno/issues/572d2654ffcdc04250763ce2
     */
    try {
      coachMarkDialogFragment.show(getSupportFragmentManager(), SAVED_ARTICLE_PROMPT);
    } catch (IllegalStateException e) {
      Logger.caughtException(e);
    }
    handler.sendEmptyMessageDelayed(DISMISS_COACHMARK, STICKY_DISPLAY_TIME_MILLIS);
  }

  @Override
  public void onCoachClick() {
    CommonNavigator.launchSavedArticles(this, false);
    resetCoachMarkPrompt();
  }

  @Override
  public void onCancelClick() {
    //ignore
    resetCoachMarkPrompt();
  }

  @Override
  public void onDismiss() {
    //ignore
    resetCoachMarkPrompt();
  }

  private void resetCoachMarkPrompt() {
    coachMarkDialogFragment = null;
    handler.removeMessages(DISMISS_COACHMARK);
  }

}
