/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.share;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;

import com.newshunt.dataentity.common.helper.share.ShareApplication;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.newshunt.common.util.R;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DataUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides functionality for various sharing options
 *
 * @author anand.winjit
 */
public class NHShareView extends LinearLayout implements View.OnClickListener {

  private static final String LOG_TAG = NHShareView.class.getSimpleName();

  private final int MAX_SHARE_OPTIONS = 4;
  private NHShareViewType nhShareViewType = NHShareViewType.BOTTOMBAR;

  private ShareViewShowListener shareViewListener;
  private ImageButton firstShareOption, secondShareOption, thirdShareOption, fourthShareOption,
      moreShareOptions;
  private Context context;

  public NHShareView(Context context) {
    super(context);
    onNHShareView(context, null, 0);
  }

  public NHShareView(Context context, AttributeSet attrs) {
    super(context, attrs);
    onNHShareView(context, attrs, 0);
  }

  public NHShareView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    onNHShareView(context, attrs, defStyleAttr);
  }

  public NHShareViewType getShareViewType() {
    return nhShareViewType;
  }

  private void onNHShareView(Context context, AttributeSet attrs, int defStyleAttr) {
    this.context = context;
    if (attrs != null) {
      TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NHShareView);
      int shareType = typedArray.getInteger(R.styleable.NHShareView_show_share_type,
          NHShareViewType.BOTTOMBAR.getType());
      if (shareType == NHShareViewType.FLOATINGICON.getType()) {
        ShareUi shareUi = ShareUtils.getShareUiForFloatingIcon();
        switch (shareUi) {
          case FLOATING_ICON:
            nhShareViewType = NHShareViewType.FLOATINGICON;
            break;
          case FLOATING_ICON_BENT_ARROW:
            nhShareViewType = NHShareViewType.FLOATINGICON_BENT_ARROW;
            break;
          case FLOATING_ICON_W_STRING:
            nhShareViewType = NHShareViewType.FLOATINGICON_W_STRING;
            break;
        }
      } else if (shareType == NHShareViewType.BIGSTORYCARD.getType()) {
        nhShareViewType = NHShareViewType.BIGSTORYCARD;
      }
      typedArray.recycle();
    }
    init();
  }

  private void init() {
    View shareView = null;
    ShareUi shareUi = getShareViewType().getShareUi();
    if (shareUi == null) {
      return;
    }

    switch (shareUi) {
      case BOTTOM_BAR:
        shareView = LayoutInflater.from(getContext())
            .inflate(R.layout.view_share_options, this, true);
        firstShareOption = (ImageButton) shareView.findViewById(R.id.first_share_shortcut);
        secondShareOption = (ImageButton) shareView.findViewById(R.id.second_share_shortcut);
        thirdShareOption = (ImageButton) shareView.findViewById(R.id.third_share_shortcut);
        fourthShareOption = (ImageButton) shareView.findViewById(R.id.fourth_share_shortcut);
        firstShareOption.setOnClickListener(this);
        secondShareOption.setOnClickListener(this);
        thirdShareOption.setOnClickListener(this);
        fourthShareOption.setOnClickListener(this);
        String shareAppShortcuts =
            PreferenceManager.getPreference(GenericAppStatePreference.SHARE_APP_OPTIONS,
                Constants.EMPTY_STRING);
        if (DataUtil.isEmpty(shareAppShortcuts) ||
            getAppShortcutNames().length < MAX_SHARE_OPTIONS) {
          setDefaultShareList();
        }
        getDefaultShareList();
        break;

      case FLOATING_ICON:
      case FLOATING_ICON_BENT_ARROW:
      case FLOATING_ICON_W_STRING:
        shareView = getNhShareViewForFloatingIcon(shareUi);
        break;

      case BIG_STORY_CARD:
        shareView = LayoutInflater.from(getContext())
            .inflate(R.layout.view_share_options_big, this, true);
    }

    if (null != shareView) {
      moreShareOptions = (ImageButton) shareView.findViewById(R.id.more_share_options);
      moreShareOptions.setOnClickListener(this);
    }
  }

  @NonNull
  private View getNhShareViewForFloatingIcon(ShareUi shareUi) {
    View shareView = LayoutInflater.from(getContext())
        .inflate(R.layout.view_share_options_fab, this, true);
    FloatingActionButton fab = (FloatingActionButton) shareView.findViewById(R.id
        .more_share_options);
    TextView fabShareText = (TextView) shareView.findViewById(R.id.fab_share_text);

    switch (shareUi) {
      case FLOATING_ICON:
        fab.setImageDrawable(
            ContextCompat.getDrawable(getContext(), R.drawable.share_floating_icon_selector));
        break;
      case FLOATING_ICON_BENT_ARROW:
        fab.setImageDrawable(ContextCompat.getDrawable(getContext(),
            R.drawable.share_floating_icon_bent_arrow_selector));
        break;
      case FLOATING_ICON_W_STRING:
        fabShareText.setVisibility(VISIBLE);
        fabShareText.setText(CommonUtils.getString(R.string.fab_share_text));
        break;
    }
    return shareView;
  }

  /**
   * Called when a view has been clicked.
   *
   * @param view The view that was clicked.
   */
  @Override
  public void onClick(View view) {
    String[] appShortcuts = getAppShortcutNames();

    if (view.equals(moreShareOptions)) {
      ShareUtils.clickOnMoreShareOptions(shareViewListener, getContext(),
          getShareViewType().getShareUi(),
          view.getContext() instanceof Activity ? (Activity) view.getContext() : null);
    } else if (getShareViewType().getShareUi() == ShareUi.BOTTOM_BAR) {

      String appName = null;
      if (view.equals(firstShareOption) && appShortcuts.length > 0) {
        appName = appShortcuts[0];
      } else if (view.equals(secondShareOption) && appShortcuts.length > 1) {
        appName = appShortcuts[1];
      } else if (view.equals(thirdShareOption) && appShortcuts.length > 2) {
        appName = appShortcuts[2];
      } else if (view.equals(fourthShareOption) && appShortcuts.length > 3) {
        appName = appShortcuts[3];
      }

      if (!DataUtil.isEmpty(appName) && null != shareViewListener) {
        shareViewListener.onShareViewClick(appName, ShareUi.BOTTOM_BAR);
      } else {
        Logger.e(LOG_TAG, "shareViewListener or appname is null");
      }
    }
  }

  /**
   * function to set NH view.
   *
   * @param shareViewListener
   */
  public void setShareListener(ShareViewShowListener shareViewListener) {
    this.shareViewListener = shareViewListener;
  }

  /**
   * function to set default shortcut app list.
   */
  public void setDefaultShareList() {
    String preferredShareOptions[] = new String[]{
        ShareApplication.FACEBOOK_APP_PACKAGE.getPackageName(),
        ShareApplication.TWITTER_APP_PACKAGE.getPackageName(),
        ShareApplication.GMAIL_APP_PACKAGE.getPackageName(),
        ShareApplication.WHATS_APP_PACKAGE.getPackageName()
    };

    List<String> shareOptions = new ArrayList<>();
    for (String preferredShareOption : preferredShareOptions) {
      if (AndroidUtils.isAppInstalled(preferredShareOption)) {
        shareOptions.add(preferredShareOption);
      }
    }

    // if default app are not installed then add other app from shareable list.
    if (shareOptions.size() < MAX_SHARE_OPTIONS) {
      backFillShareableApps(shareOptions);
    }

    PreferenceManager.savePreference(GenericAppStatePreference.SHARE_APP_OPTIONS,
        TextUtils.join(Constants.PIPE_CHARACTER, shareOptions));
  }

  private void backFillShareableApps(List<String> shareOptions) {
    List<ShareAppDetails> appDetails = AndroidUtils.getShareableApps();
    for (int i = 0; i < appDetails.size(); i++) {
      if ((shareOptions.contains(appDetails.get(i).getAppPackage()))) {
        continue;
      }

      shareOptions.add(appDetails.get(i).getAppPackage());
      if (shareOptions.size() == 4) {
        break;
      }
    }
  }

  /**
   * function to get sharable app shortcut list.
   */
  public void getDefaultShareList() {
    checkDefaultAppInstalled();
    String[] appShortcuts = getAppShortcutNames();
    if (appShortcuts == null || appShortcuts.length < MAX_SHARE_OPTIONS) {
      return;
    }

    if (getShareViewType().getShareUi() == ShareUi.BOTTOM_BAR) {
      try {
        firstShareOption.setImageDrawable(
            context.getPackageManager().getApplicationIcon(appShortcuts[0]));
        secondShareOption.setImageDrawable(
            context.getPackageManager().getApplicationIcon(appShortcuts[1]));
        thirdShareOption.setImageDrawable(
            context.getPackageManager().getApplicationIcon(appShortcuts[2]));
        fourthShareOption.setImageDrawable(
            context.getPackageManager().getApplicationIcon(appShortcuts[3]));
      } catch (PackageManager.NameNotFoundException e) {
        // Do nothing
      }
    }
  }

  public void shareApp(View view){
    ShareUtils.clickOnMoreShareOptions(shareViewListener, getContext(),
        getShareViewType().getShareUi(),
        view.getContext() instanceof Activity ? (Activity) view.getContext() : null);
  }

  private String[] getAppShortcutNames() {
    String defaultValue =
        PreferenceManager.getPreference(GenericAppStatePreference.SHARE_APP_OPTIONS,
            Constants.EMPTY_STRING);
    return defaultValue.split("\\" + Constants.PIPE_CHARACTER);
  }

  private void checkDefaultAppInstalled() {
    String preferredShareOptions[] = getAppShortcutNames();
    List<String> shareOptions = new ArrayList<>();
    for (String defaultShareOption : preferredShareOptions) {
      if (AndroidUtils.isAppInstalled(defaultShareOption)) {
        shareOptions.add(defaultShareOption);
      }
    }

    PreferenceManager.savePreference(GenericAppStatePreference.SHARE_APP_OPTIONS,
        TextUtils.join(Constants.PIPE_CHARACTER, shareOptions));
  }

  public boolean isShowSingleShareButton() {
    ShareUi shareUi = nhShareViewType.getShareUi();
    return (shareUi == ShareUi.FLOATING_ICON || shareUi == ShareUi.FLOATING_ICON_BENT_ARROW ||
        shareUi == ShareUi.FLOATING_ICON_W_STRING);
  }

  @Override
  protected void onDetachedFromWindow() {
    shareViewListener = null;
    super.onDetachedFromWindow();
  }

  public enum NHShareViewType {
    BOTTOMBAR(0, ShareUi.BOTTOM_BAR),
    FLOATINGICON(1, ShareUi.FLOATING_ICON),
    BIGSTORYCARD(2, ShareUi.BIG_STORY_CARD),
    FLOATINGICON_BENT_ARROW(3, ShareUi.FLOATING_ICON_BENT_ARROW),
    FLOATINGICON_W_STRING(4, ShareUi.FLOATING_ICON_W_STRING);

    private final int type;
    private final ShareUi shareUi;

    NHShareViewType(int type, ShareUi shareUi) {
      this.type = type;
      this.shareUi = shareUi;
    }

    public int getType() {
      return type;
    }

    public ShareUi getShareUi() {
      return shareUi;
    }
  }
}