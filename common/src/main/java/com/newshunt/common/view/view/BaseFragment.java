/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.common.helper.listener.PagerLifecycleObserver;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.helper.share.ShareContent;
import com.newshunt.common.helper.share.ShareFactory;
import com.newshunt.common.helper.share.ShareHelper;
import com.newshunt.dataentity.common.model.entity.LifeCycleEvent;

/**
 * Base Fragment class which generates a fragmentId unique to each fragment
 * This helps in uniquely identifying which fragment made the actual call
 *
 * @author maruti.borker
 */
public class BaseFragment extends ViewLifecycleFragment {

  private static final String FRAGMENT_ID = "FRAGMENT_ID";
  private int fragmentId;
  private boolean jsScrollEnabled;
  private boolean jsRefreshEnabled;
  @Nullable
  private PagerLifecycleObserver pvObserver;

  public BaseFragment() {
  }

  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    if (savedState != null) {
      fragmentId = savedState.getInt(FRAGMENT_ID);
    } else {
      fragmentId = UniqueIdHelper.getInstance().generateUniqueId();
    }

    PreferenceManager.savePreference(GenericAppStatePreference.APP_CURRENT_TIME,
        System.currentTimeMillis());
    if (pvObserver != null) {
      getLifecycle().addObserver(pvObserver);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt(FRAGMENT_ID, fragmentId);
    super.onSaveInstanceState(outState);
  }

  /**
   * Extending class can override to do functionality needed when this fragment is in viewpager
   * and a new Page is selected
   *
   * @param pageNumberSelected - new page number selected
   * @param previousPageNumber - previous page number
   */
  public void onPageSelected(int pageNumberSelected, int previousPageNumber) {

  }

  protected int getFragmentId() {
    return fragmentId;
  }

  protected int getUniqueScreenId() {
    return hashCode();
  }


  /**
   * Method to share the book content.
   *
   * @param shareContent Information to share.
   * @param packageName  App to share.
   */
  protected void shareBooks(ShareContent shareContent, String packageName) {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setType(Constants.INTENT_TYPE_TEXT);

    if (shareContent == null || CommonUtils.isEmpty(shareContent.getTitle()) ||
        CommonUtils.isEmpty(shareContent.getShareUrl())) {
      return;
    }

    ShareHelper shareHelper =
      ShareFactory.getShareHelper(packageName, getActivity(), sendIntent, shareContent);
    shareHelper.share();
  }

  public void setSwipeRefreshEnabled(boolean enabled) {
    // do nothing
  }

  @Override
  public void onResume() {
    super.onResume();
    BusProvider.getUIBusInstance().post(new LifeCycleEvent(getUniqueScreenId(), LifeCycleEvent.RESUMED));
  }

  @Override
  public void onPause() {
    BusProvider.getUIBusInstance().post(new LifeCycleEvent(getUniqueScreenId(), LifeCycleEvent.PAUSED));
    super.onPause();
  }

  @Override
  public void onDestroy() {
    BusProvider.getUIBusInstance()
        .post(new LifeCycleEvent(getUniqueScreenId(), LifeCycleEvent.DESTROYED));
    super.onDestroy();
    AndroidUtils.getWatcher().watch(this);
  }

  @Override
  public void onDestroyView() {
    if (canRemoveWebViews()) {
      ViewUtils.deleteWebViews(getView());
    }
    super.onDestroyView();
  }

  public boolean isJsScrollEnabled() {
    return jsScrollEnabled;
  }

  public void setJsScrollEnabled(boolean jsScrollEnabled) {
    this.jsScrollEnabled = jsScrollEnabled;
  }

  public void setJsRefreshEnabled(boolean jsRefreshEnabled) {
    this.jsRefreshEnabled = jsRefreshEnabled;
  }

  public boolean isJsRefreshEnabled() {
    return jsRefreshEnabled;
  }

  public void jsScrollToTop() {
    // to be implemented by child fragments
  }

  protected boolean canRemoveWebViews() {
    return true;
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (pvObserver != null) {
      pvObserver.setUserVisibleHint(isVisibleToUser);
    }
  }

  public void setPvObserver(@Nullable PagerLifecycleObserver pvObserver) {
    this.pvObserver = pvObserver;
  }

  public boolean handleBackPress() {
    return false;
  }
}