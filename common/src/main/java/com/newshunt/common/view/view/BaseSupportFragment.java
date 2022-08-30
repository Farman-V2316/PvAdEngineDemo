/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.view;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.common.helper.listener.PagerLifecycleObserver;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.LifeCycleEvent;

/**
 * Base Support Fragment class which generates a fragmentId unique to each fragment
 * This helps in uniquely identifying which fragment made the actual call
 *
 * @author maruti.borker
 */
public class BaseSupportFragment extends BaseFragment {

  private static final String FRAGMENT_ID = "FRAGMENT_ID";
  private int fragmentId;
  private boolean isStoryShared = false;
  protected boolean backPressed;
  private int pagerIndex;
  @Nullable
  private PagerLifecycleObserver pvObserver;

  public BaseSupportFragment() {
  }

  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    if (savedState != null) {
      fragmentId = savedState.getInt(FRAGMENT_ID);
    } else {
      fragmentId = UniqueIdHelper.getInstance().generateUniqueId();
    }

    PreferenceManager.savePreference(GenericAppStatePreference.APP_CURRENT_TIME, System.currentTimeMillis());
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

  @Override
  public void onPause() {
    BusProvider.getUIBusInstance().post(new LifeCycleEvent(getUniqueScreenId(), LifeCycleEvent.PAUSED));
    super.onPause();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    BusProvider.getUIBusInstance()
        .post(new LifeCycleEvent(getUniqueScreenId(), LifeCycleEvent.DESTROYED));
    AndroidUtils.getWatcher().watch(this);
  }

  @Override
  public void onDestroyView() {
    ViewUtils.deleteWebViews(getView());
    super.onDestroyView();
  }

  protected void setStoryShared (boolean isStoryShared) {
    this.isStoryShared = isStoryShared;
  }

  protected void updateStoryInPVObserver(Object story) {
    if (pvObserver != null) {
      pvObserver.updateStory(story);
    }
  }

  public boolean isStoryShared () {
    return isStoryShared;
  }

  @Override
  public void onResume() {
    super.onResume();
    backPressed = false;
    BusProvider.getUIBusInstance().post(new LifeCycleEvent(getUniqueScreenId(), LifeCycleEvent.RESUMED));
  }

  public void onBackPressed() {
    backPressed = true;
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

  public int getPagerIndex() {
    return pagerIndex;
  }

  public void setPagerIndex(int pagerIndex) {
    this.pagerIndex = pagerIndex;
  }
}