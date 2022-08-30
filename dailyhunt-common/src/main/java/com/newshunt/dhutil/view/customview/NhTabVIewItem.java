/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.request.transition.Transition;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.FileUtil;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.helper.common.ImageDownloadSourceType;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionInfo;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.theme.ThemeUtils;
import com.newshunt.dhutil.model.entity.NHTabIconUpdate;
import com.newshunt.dhutil.view.NHTabIconClickInterceptor;
import com.newshunt.sdk.network.Priority;
import com.newshunt.sdk.network.image.Image;

import java.io.File;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import static com.newshunt.dataentity.common.helper.common.CommonUtils.getTintedDrawable;


/**
 * @author: bedprakash on 12/1/17.
 */

public class NhTabVIewItem extends LinearLayout {

  private static final int MAX_BADGE_COUNT = 9;
  private TextView tvNotificationBadge;
  private ImageView ivIcon;
  private NHTabIconClickInterceptor clickInterceptor;
  private UpdateLoadTarget updateLoadTarget;
  private View highlightIcon;
  private View buttonBackground;
  private AppSectionInfo info;
  private Runnable hideHighlightIconTask;
  private boolean nightModeNotSupported;

  public NhTabVIewItem(Context context, AppSectionInfo info, boolean nightModeNotSupported) {
    super(context);
    init(info, nightModeNotSupported);
  }

  void init(AppSectionInfo info, boolean nightModeNotSupported) {
    this.info = info;
    this.nightModeNotSupported = nightModeNotSupported;
    View view;
    view = LayoutInflater.from(getContext()).inflate(com.newshunt.common.util.R.layout
        .view_items_navigation_bar, this, true);
    ivIcon = view.findViewById(com.newshunt.common.util.R.id.navbar_appsection_icon);
    tvNotificationBadge = view.findViewById(com.newshunt.common.util.R.id.navbar_notification_count_tv);
    highlightIcon = findViewById(com.newshunt.common.util.R.id.navbar_highlight);
    buttonBackground = findViewById(com.newshunt.common.util.R.id.navbar_item_container);

    setPressedStateColor();
    setBadgeColor();

    //if any of the file paths of active (day/night), inactive (day/night) does not exist, then
    // load from drawable resources to avoid inconsistency
    if (!checkIfIconFilePathsExist(info)) {
      ivIcon.setImageDrawable(getInActiveIconFromDrawables(info.getType()));
    } else {
      Image.load(new File(getInActiveIconFilePath(info)), false).priority(Priority.PRIORITY_HIGH)
          .into(ivIcon);
      Image.load(new File(getActiveIconFilePath(info)), true).priority(Priority.PRIORITY_HIGH).into
          (new EmptyTarget());
    }
  }

  private boolean checkIfIconFilePathsExist(AppSectionInfo info) {
    return info != null && FileUtil.checkIfFileExists(info.getActiveIconFilePath()) &&
        FileUtil.checkIfFileExists(info.getActiveIconNightFilePath()) &&
        FileUtil.checkIfFileExists(info.getInActiveIconFilepath()) &&
        FileUtil.checkIfFileExists(info.getInActiveIconNightFilePath());
  }

  private String getActiveIconFilePath(AppSectionInfo appSectionInfo) {
    if (appSectionInfo == null) {
      return null;
    }
    return isNightMode() ? appSectionInfo.getActiveIconNightFilePath() : appSectionInfo
        .getActiveIconFilePath();
  }

  private String getInActiveIconFilePath(AppSectionInfo appSectionInfo) {
    if (appSectionInfo == null) {
      return null;
    }
    return isNightMode() ? appSectionInfo.getInActiveIconNightFilePath() :
        appSectionInfo.getInActiveIconFilepath();
  }

  private void setPressedStateColor() {
    boolean isNightMode = isNightMode();
    String pressedStateColor =
        isNightMode ? info.getPressedStateColorNight() : info.getPressedStateColor();
    int normalStateColor = isNightMode ? CommonUtils.getColor(R.color.bottom_bar_text_color_night)
            : CommonUtils.getColor(R.color.bottom_bar_text_color_day);
    int pressedColor;

    int normalColor = -1;
    if (!CommonUtils.isEmpty(pressedStateColor)) {
      pressedColor = Color.parseColor(pressedStateColor);
    } else {
      pressedColor = isNightMode ? CommonUtils.getColor(com.newshunt.common.util.R.color.bottom_bar_pressed_color_night) :
          CommonUtils.getColor(com.newshunt.common.util.R.color.bottom_bar_pressed_color);
    }


    try {
      GradientDrawable rectShape = new GradientDrawable();
      rectShape.setShape(GradientDrawable.RECTANGLE);
      rectShape.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
      rectShape.setColor(pressedColor);

      StateListDrawable states = new StateListDrawable();
      states.addState(new int[]{android.R.attr.state_pressed}, rectShape);

      if (normalStateColor != -1) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        shape.setColor(normalStateColor);
        states.addState(new int[]{android.R.attr.state_enabled}, shape);
      }

      buttonBackground.setBackground(states);
    } catch (IllegalArgumentException e) {
      Logger.caughtException(e);
    }
  }

  private void setBadgeColor() {
    Integer textColor = ViewUtils.getColor(info.getBadgeTextColor());
    if (textColor != null) {
      tvNotificationBadge.setTextColor(textColor);
    }
  }

  public Drawable getInActiveIconFromDrawables(AppSection type) {
    int tintColor = isNightMode() ?
        com.newshunt.common.util.R.color.navbar_icon_color_night_unselected : com.newshunt.common.util.R.color.black_color;

    switch (type) {
      case NEWS:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_news_tab, tintColor);
      case TV:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_tab_tv, tintColor);
      case DEEPLINK:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_local_tab,
            tintColor);
      case SEARCH:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_search_tab, tintColor);
      default:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_follow_tab_unselected, tintColor);
    }
  }

  public Drawable getActiveIconFromDrawables(AppSection type) {
    int tintColor = isNightMode() ?
            com.newshunt.common.util.R.color.navbar_icon_color_night_unselected : com.newshunt.common.util.R.color.black_color;
    switch (type) {
      case NEWS:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_news_tab_selected, tintColor);
      case TV:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_tab_tv_selected, tintColor);
      case DEEPLINK:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_local_tab_selected, tintColor);
      case SEARCH:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_serach_selected, tintColor);
      default:
        return getTintedDrawable(com.newshunt.common.util.R.drawable.vector_follow_tab_selected, tintColor);
    }
  }

  /**
   * Post a runnable to hide the highlight icon after delay.
   *
   * @param delay delay in ms
   */
  public void scheduleHighlightIconHide(long delay) {
    hideHighlightIconTask = () -> {
      if (CommonUtils.isEmpty(info.getId())) {
        return;
      }
      hideHighlightIcon();
    };
    postDelayed(hideHighlightIconTask, delay);
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    clickInterceptor = null;
    removeCallbacks(hideHighlightIconTask);
    hideHighlightIconTask = null;
  }

  public void setSelected() {
    if (!checkIfIconFilePathsExist(info)) {
      ivIcon.setImageDrawable(getActiveIconFromDrawables(info.getType()));
    } else {
      Image.load(new File(getActiveIconFilePath(info)), false).priority(Priority.PRIORITY_HIGH)
          .into(ivIcon);
    }

    setSelected(true);
  }

  public boolean interceptClickEvent() {
    return clickInterceptor != null && clickInterceptor.onInterceptUpdateIconClick(this, info);
  }

  public void update(NHTabIconUpdate nhTabIconUpdate) {
    if (ivIcon == null || nhTabIconUpdate == null) {
      return;
    }

    if (nhTabIconUpdate.isReset()) {
      resetIcon();
      return;
    }

    clickInterceptor = nhTabIconUpdate.getClickInterceptor();
    String imageUrl = isSelected() ? nhTabIconUpdate.getActiveIconUrl() : nhTabIconUpdate
        .getInActiveIconUrl();
    if (CommonUtils.isEmpty(imageUrl)) {
      return;
    }
    ImageDownloadSourceType sourceType = isSelected() ? nhTabIconUpdate.getActiveIconSourceType()
        : nhTabIconUpdate.getInActiveIconSourceType();

    updateLoadTarget = new UpdateLoadTarget(ivIcon, imageUrl, isSelected(),
        nhTabIconUpdate.getIconDownloadCallback());
    switch (sourceType) {
      case RESOURCE:
        updateLoadTarget = null;
        int resId = AndroidUtils.parseResourceIdFromString(imageUrl);
        if (resId != AndroidUtils.NOT_A_RESOURCE_ID) {
          ivIcon.setImageResource(resId);
        }
        break;
      case FILE:
        if (FileUtil.checkIfFileExists(imageUrl)) {
          Image.load(new File(imageUrl), true)
              .priority(Priority.PRIORITY_HIGH)
              .into(updateLoadTarget);
        }
        break;
      case NETWORK:
        Image.load(imageUrl, true).priority(Priority.PRIORITY_HIGH).into(updateLoadTarget);
        break;
    }
  }

  public void resetIcon() {
    clickInterceptor = null;
    if (updateLoadTarget != null) {
      Image.cancelTargetRequest(updateLoadTarget);
      updateLoadTarget = null;
    }
    setSelected();
    setPressedStateColor();
    setBadgeColor();
  }

  public AppSectionInfo getInfo() {
    return info;
  }

  public void setNotificationBadgeText(int notificationBadgeText) {
    if (notificationBadgeText == 0) {
      clearNotificationBadge();
    } else {
      tvNotificationBadge.setVisibility(VISIBLE);
      boolean maxCountReached = notificationBadgeText > MAX_BADGE_COUNT;
      String text = String.valueOf(notificationBadgeText);
      ConstraintLayout.LayoutParams layoutParams =
          (ConstraintLayout.LayoutParams) tvNotificationBadge.getLayoutParams();
      int drawable;
      if (maxCountReached) {
        text = MAX_BADGE_COUNT + "+";
        drawable = ThemeUtils.getThemeDrawableByAttribute(getContext(), R.attr
            .notification_badge_squared, View.NO_ID);
        layoutParams.width = CommonUtils.getDimension(com.newshunt.common.util.R.dimen.notification_badge_width_squared);
      } else {
        drawable = ThemeUtils.getThemeDrawableByAttribute(getContext(), R.attr
            .notification_badge_circle, View.NO_ID);
        layoutParams.width = CommonUtils.getDimension(com.newshunt.common.util.R.dimen.notification_badge_width_circle);
      }
      tvNotificationBadge.setText(text);
      tvNotificationBadge.setLayoutParams(layoutParams);
      tvNotificationBadge.setBackgroundResource(drawable);
      setBadgeColor();
    }
  }

  public void clearNotificationBadge() {
    tvNotificationBadge.setVisibility(GONE);
  }

  public void hideHighlightIcon() {
    highlightIcon.setVisibility(GONE);
  }

  public boolean isHighlighted() {
    return highlightIcon.getVisibility() == View.VISIBLE;
  }

  private static class EmptyTarget extends Image.ImageTarget {

    @Override
    public void onResourceReady(@NonNull Object o, @Nullable Transition transition) {
      //DO NOTHING
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
      super.onLoadFailed(errorDrawable);
    }
  }

  private static class UpdateLoadTarget extends Image.ImageTarget {

    private final WeakReference<ImageView> iconRef;
    private final String downloadUrl;
    private final WeakReference<NHTabIconUpdate.IconDownloadCallback> callbackRef;
    private final boolean isActiveIcon;

    UpdateLoadTarget(ImageView icon, String downloadUrl, boolean isActiveIcon,
                     NHTabIconUpdate.IconDownloadCallback iconDownloadCallback) {
      iconRef = new WeakReference<>(icon);
      this.downloadUrl = downloadUrl;
      this.isActiveIcon = isActiveIcon;
      callbackRef = new WeakReference<>(iconDownloadCallback);
    }


    @Override
    public void onResourceReady(@NonNull Object bitmap, @Nullable Transition transition) {
      if (!(bitmap instanceof Bitmap)) {
        return;
      }

      ImageView icon = iconRef.get();

      if (icon == null) {
        return;
      }
      icon.setImageBitmap((Bitmap) bitmap);

      NHTabIconUpdate.IconDownloadCallback callback = callbackRef.get();
      if (callback == null) {
        return;
      }
      callback.onSuccess();
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
      super.onLoadFailed(errorDrawable);
      NHTabIconUpdate.IconDownloadCallback callback = callbackRef.get();
      if (callback == null) {
        return;
      }
      callback.onFailure(downloadUrl, isActiveIcon);
    }
  }

  public boolean isNightMode() {
    return !nightModeNotSupported && ThemeUtils.isNightMode();
  }

}
