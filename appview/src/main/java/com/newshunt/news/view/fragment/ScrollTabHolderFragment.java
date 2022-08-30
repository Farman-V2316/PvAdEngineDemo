/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.news.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.util.Pair;
import android.view.View;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.common.helper.share.NHShareView;
import com.newshunt.common.helper.share.ShareUi;
import com.newshunt.common.view.view.BaseFragment;
import com.newshunt.appview.R;
import com.newshunt.dataentity.common.model.entity.EventsInfo;
import com.newshunt.news.view.listener.CommunicationEventInterface;
import com.newshunt.news.view.listener.CustomScrollListener;
import com.newshunt.news.view.listener.VisibleFragmentListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Base class for implementing fragment with scrolling list.
 *
 * @author nilesh.borkar
 */
public abstract class ScrollTabHolderFragment extends BaseFragment
    implements CustomScrollListener {

  protected CustomScrollListener scrollTabHolder;

  protected boolean isVisible;

  private boolean isEventShown;

  protected CommunicationEventInterface communicationEventInterface;

  protected TabSwitchCallback tabSwitchCallback;

  public void setScrollTabHolder(CustomScrollListener scrollTabHolder) {
    this.scrollTabHolder = scrollTabHolder;
  }

  @Override
  public void onScroll(int scrollY, int pagePosition) {
    // nothing
  }

  public void setSwipeRefreshEnabled(boolean enabled) {
    // do nothing
  }

  public void onPageSelected() {
    // do nothing
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    if (activity instanceof CommunicationEventInterface) {
      communicationEventInterface = (CommunicationEventInterface) activity;
    }

    if (activity instanceof TabSwitchCallback) {
      tabSwitchCallback = (TabSwitchCallback) activity;
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    communicationEventInterface = null;
    tabSwitchCallback = null;
  }

  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    isVisible = isVisibleToUser;
    if (isVisible && getView() != null && !isEventShown) {
      isEventShown = true;
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (isVisible && !isEventShown) {
      isEventShown = true;
    }
  }

  protected boolean isVisibleAndSelected(int position) {
    boolean result = isVisible;
    if (getActivity() != null && getActivity() instanceof VisibleFragmentListener) {
      result = isVisible && ((VisibleFragmentListener) getActivity())
          .isSelectedFragment(position);
    }
    return result;
  }

  public void refresh() {
    // do nothing
  }

  public void scrollToTopAndRefresh() {}

  public boolean isAtTheTop() {
    return true;
  }

  public void logTimeSpentEvent(NhAnalyticsUserAction exitAction) {
    // do nothing
  }

  public void onShareClick(String packageName, ShareUi shareUi) {
    // do nothing
  }

  public void showShareView(@NonNull NHShareView nhShareView) {
    nhShareView.setVisibility(View.GONE);
  }

  public void setBackToTopView(@NonNull View backToTopView) {
    // do nothing
  }

  @Override
  public void adjustScroll(int scrollHeight, int headerTranslationY) {

  }

  public Intent getShareIntent() {
    return null;
  }

  @Override
  public void onDestroy() {
    scrollTabHolder = null;
    super.onDestroy();
  }

  protected EventsInfo getEvents(String resource, String event) {
    if (communicationEventInterface != null) {
      return communicationEventInterface.getEvent(resource, event);
    }
    return null;
  }

  @Override
  public void startActivity(Intent intent) {
    super.startActivity(intent);
    if (getActivity() == null) {
      return;
    }
    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  }

  public interface TabSwitchCallback {
    void onTabsSwitched();
  }

  @Nullable
  public Pair<Object,Integer> getItemAndPosition() {
    return null;
  }
}

