package com.newshunt.dhutil.view.customview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DeeplinkHelper;
import com.newshunt.common.helper.common.FileUtil;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.dataentity.common.model.entity.AppSection;
import com.newshunt.dataentity.common.model.entity.NotificationUpdate;
import com.newshunt.dataentity.common.model.entity.UserAppSection;
import com.newshunt.dataentity.dhutil.model.entity.appsection.RecentLaunchList;
import com.newshunt.deeplink.navigator.HomeNavigator;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.analytics.AnalyticsHelper;
import com.newshunt.dhutil.analytics.ExploreButtonType;
import com.newshunt.dhutil.helper.appsection.AppSectionsProvider;
import com.newshunt.deeplink.navigator.CommonNavigator;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.helper.theme.ThemeUtils;
import com.newshunt.dataentity.dhutil.model.entity.NHTabClicked;
import com.newshunt.dhutil.model.entity.NHTabIconUpdate;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionInfo;
import com.newshunt.dataentity.dhutil.model.entity.appsection.AppSectionsResponse;
import com.newshunt.news.util.NewsConstants;
import com.newshunt.sdk.network.Priority;
import com.newshunt.sdk.network.image.Image;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.lifecycle.LifecycleOwner;

import static com.newshunt.dataentity.common.helper.common.CommonUtils.getTintedDrawable;

/**
 * Class to show tab view for News,Books,Testpreps & NotificationInbox
 *
 * @author vinod.bc
 */
public class NHTabView extends LinearLayout implements View.OnClickListener {
  private static final int MAXIMUM_MENU_SIZE = 6;
  private static final String TAG = NHTabView.class.getSimpleName();
  private Context context;
  private LinearLayout mainNavBarLayout;
  private boolean mShowNewNotificationCount = true;
  private List<AppSectionInfo> appSectionInfoList;
  private List<AppSectionInfo> menuList = new ArrayList<>();
  private String curSectionId;
  private String sectionFromDeeplink = null;
  private int notificationCount = -1;
  private boolean nightModeNotSupported;
  private LifecycleOwner lifecycleOwner;

  public NHTabView(Context context) {
    super(context);
    this.context = context;
    init(context, null, 0, 0);
  }

  public NHTabView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    init(context, attrs, 0, 0);
  }

  public NHTabView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;
    init(context, attrs, defStyleAttr, 0);
  }

  private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    TypedArray array =
        context.obtainStyledAttributes(attrs, R.styleable.NHTabView,
            defStyleAttr, defStyleRes);

    try {
      nightModeNotSupported = array.getBoolean(R.styleable.NHTabView_nightModeNotSupported, false);
    } catch (Exception e) {
      Logger.caughtException(e);
    } finally {
      array.recycle();
    }

    View rootView =
        LayoutInflater.from(getContext()).inflate(R.layout.view_main_navigation_bar, this, true);
    mainNavBarLayout = (LinearLayout) rootView.findViewById(R.id.main_nav_bar_layout);
  }

  public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
    this.lifecycleOwner = lifecycleOwner;
    if (this.lifecycleOwner != null) {
      AppSectionsProvider.INSTANCE.getAppSectionsObserver().observe(this.lifecycleOwner
          , this::onAppSectionsProvided);
    }
  }

  public void setCurrentSectionId(String sectionid) {
    curSectionId = sectionid;
    setTabViewSelection();
  }

  private void onAppSectionsProvided(AppSectionsResponse appSectionsResponse) {
    this.appSectionInfoList = appSectionsResponse.getSections();
    addCustomViewToRootView(ThemeUtils.isNightMode() ? appSectionsResponse.getBgColorNight() :
        appSectionsResponse.getBgColor());
    if(sectionFromDeeplink != null) {
      AppSectionInfo appSectionInfo = checkIfSectionExists(sectionFromDeeplink);
      if(appSectionInfo != null) {
        launchSection(appSectionInfo);
        setCurrentSectionId(sectionFromDeeplink);
      }
    }
    setTabViewSelection();
    setNotificationBadgeText(notificationCount);
  }

  public AppSectionInfo checkIfSectionExists(String sectionId) {
    for(AppSectionInfo info: appSectionInfoList) {
      if(sectionId.equals(info.getId())) {
        return info;
      }
    }
    return null;
  }

  public void setSectionFromDeeplink(String section) {
    sectionFromDeeplink = section;
  }

  private void addCustomViewToRootView(String bgColor) {
    if (appSectionInfoList == null) {
      return;
    }

    LinearLayout.LayoutParams mainNavBarLayoutParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    mainNavBarLayoutParams.weight = (1.0f / appSectionInfoList.size());
    mainNavBarLayout.removeAllViews();
    menuList.clear();

    List<String> sectionTitles = new ArrayList<>();
    boolean menuNotRequired = appSectionInfoList.size() <= MAXIMUM_MENU_SIZE;
    for (int i = 0; i < appSectionInfoList.size(); i++) {
      AppSectionInfo info = appSectionInfoList.get(i);
      if (i < MAXIMUM_MENU_SIZE - 1 || menuNotRequired) {
        NhTabVIewItem customView = new NhTabVIewItem(context, info, nightModeNotSupported);
        customView.setTag(info.getId());
        customView.setOnClickListener(this);
        customView.setLayoutParams(mainNavBarLayoutParams);
        mainNavBarLayout.addView(customView);
        customView.hideHighlightIcon();
        sectionTitles.add(info.getTitle());
      } else {
        // Add to menu
        menuList.add(info);
      }
    }

    if (!menuNotRequired) {
      AppSectionInfo menuInfo = new AppSectionInfo();
      NHMenuViewItem customView = new NHMenuViewItem(context, nightModeNotSupported);
      customView.setOnClickListener(this);
      customView.setLayoutParams(mainNavBarLayoutParams);
      mainNavBarLayout.addView(customView);
      sectionTitles.add(menuInfo.getTitle());
    }
  }

  @Override
  public void onClick(View v) {
    View clickedView = findViewWithTag(v.getTag());
    if (clickedView instanceof NhTabVIewItem) {
      NhTabVIewItem clickedItem = (NhTabVIewItem) clickedView;
      if (clickedItem.isHighlighted()) {
        clickedItem.scheduleHighlightIconHide(0);
      }
      if (!clickedItem.interceptClickEvent()) {
        launchSection(clickedItem.getInfo());
      }
    } else if (v instanceof NHMenuViewItem) {
      final PopupWindow popupWindow = new PopupWindow(this);
      LinearLayout superView = new LinearLayout(this.context);
      superView.setGravity(Gravity.BOTTOM);
      superView.setOrientation(LinearLayout.VERTICAL);
      superView.setPadding(0, 0, 0, 0);
      superView.setOnClickListener(clickedView1 -> {
        popupWindow.dismiss();
        ((NHMenuViewItem) v).setSelectedColor(false);
      });

      LinearLayout view = new LinearLayout(this.context);
      superView.addView(view);
      view.setGravity(Gravity.BOTTOM);
      view.setOrientation(LinearLayout.VERTICAL);
      LinearLayout.LayoutParams layoutParam =
          new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      layoutParam.bottomMargin = this.getHeight() - CommonUtils.getPixelFromDP(4, this.context);
      view.setLayoutParams(layoutParam);
      view.setBackgroundColor(CommonUtils.getColor(CommonUtils.getResourceIdFromAttribute(context,
          R.attr.like_dislike_bg)));

      LinearLayout menuView = new LinearLayout(this.context);
      menuView.setBackground(CommonUtils.getDrawable(CommonUtils.getResourceIdFromAttribute(context,
          R.attr.bottom_bar_menu_bg_drawable)));
      menuView.setOrientation(LinearLayout.VERTICAL);
      menuView.setPadding(0, CommonUtils.getDimension(R.dimen.padding_small), CommonUtils.getDimension(R.dimen.padding_small), 0);
      menuView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT));
      addMenuChild((NHMenuViewItem) v, menuView, popupWindow);
      view.addView(menuView);
      view.setOnClickListener(v1 -> {
        popupWindow.dismiss();
        ((NHMenuViewItem) v).setSelectedColor(false);
      });

      popupWindow.setFocusable(true);
      popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
      popupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
      popupWindow.setContentView(superView);
      popupWindow.showAtLocation(NHTabView.this, Gravity.BOTTOM, 0, 0);
      ((NHMenuViewItem) v).setSelectedColor(true);
    }
  }

  private void addMenuChild(NHMenuViewItem view, LinearLayout menuView, PopupWindow popupWindow) {
    for (AppSectionInfo info : menuList) {
      LayoutInflater inflater =
          (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (inflater != null) {
        View menuItem = inflater.inflate(R.layout.menu_options, null);
        ImageView iconView = menuItem.findViewById(R.id.section_icon);

        if (!checkIfIconFilePathsExist(info)) {
          if (Objects.equals(curSectionId, info.getId())) {
            iconView.setImageDrawable(getActiveIconFromDrawables(info.getType()));
          }
          else {
            int tintColor = isNightMode() ?
                R.color.navbar_icon_color_night_unselected : R.color.black_color;
            iconView.setImageDrawable(getInActiveIconFromDrawables(info.getType()));
            iconView.setImageTintList(ColorStateList.valueOf(CommonUtils.getColor(tintColor)));
          }
        } else {
          if (Objects.equals(curSectionId, info.getId())) {
            Image.load(new File(getActiveIconFilePath(info)), false)
                .priority(Priority.PRIORITY_HIGH).into(iconView);
          }
          else {
            Image.load(new File(getInActiveIconFilePath(info)), false)
                .priority(Priority.PRIORITY_HIGH).into(iconView);
            int tintColor = isNightMode() ?
                R.color.navbar_icon_color_night_unselected : R.color.black_color;
            iconView.setImageTintList(ColorStateList.valueOf(CommonUtils.getColor(tintColor)));
          }
        }

        NHTextView textView = menuItem.findViewById(R.id.section_text);
        textView.setText(info.getTitle());
        menuView.addView(menuItem);
        menuItem.setOnClickListener(v1 -> {
          popupWindow.dismiss();
          launchSection(info);
          view.setSelectedColor(false);
        });
      }
    }
  }

  public Drawable getInActiveIconFromDrawables(AppSection type) {
    int tintColor = isNightMode() ?
        R.color.navbar_icon_color_night_unselected : R.color.black_color;

    switch (type) {
      case NEWS:
        return getTintedDrawable(R.drawable.vector_news_tab, tintColor);
      case TV:
        return getTintedDrawable(R.drawable.vector_tab_tv, tintColor);
      case FOLLOW:
        return getTintedDrawable(R.drawable.vector_follow_tab_unselected,
            tintColor);
      default:
        return getTintedDrawable(R.drawable.vector_notification_tab, tintColor);
    }
  }

  public Drawable getActiveIconFromDrawables(AppSection type) {
    int tintColor = isNightMode() ?
            R.color.navbar_icon_color_night_unselected : R.color.black_color;

    switch (type) {
      case NEWS:
        return getTintedDrawable(R.drawable.vector_news_tab_selected, tintColor);
      case TV:
        return getTintedDrawable(R.drawable.vector_tab_tv_selected, tintColor);
      case FOLLOW:
        return getTintedDrawable(R.drawable.vector_follow_tab_selected, tintColor);
      default:
        return getTintedDrawable(R.drawable.vector_notification_tab_selected, tintColor);
    }
  }

  private boolean checkIfIconFilePathsExist(AppSectionInfo info) {
    return info != null && FileUtil.checkIfFileExists(info.getActiveIconFilePath()) &&
        FileUtil.checkIfFileExists(info.getActiveIconNightFilePath()) &&
        FileUtil.checkIfFileExists(info.getInActiveIconFilepath()) &&
        FileUtil.checkIfFileExists(info.getInActiveIconNightFilePath());
  }

  private String getInActiveIconFilePath(AppSectionInfo appSectionInfo) {
    if (appSectionInfo == null) {
      return null;
    }
    return isNightMode() ? appSectionInfo.getInActiveIconNightFilePath() :
        appSectionInfo.getInActiveIconFilepath();
  }

  private String getActiveIconFilePath(AppSectionInfo appSectionInfo) {
    if (appSectionInfo == null) {
      return null;
    }
    return isNightMode() ? appSectionInfo.getActiveIconNightFilePath() :
        appSectionInfo.getActiveIconFilePath();
  }

  public boolean isNightMode() {
    return !nightModeNotSupported && ThemeUtils.isNightMode();
  }

  private void launchSection(AppSectionInfo info) {
    if (info == null || curSectionId == null || curSectionId.equals(info.getId())) {
      Logger.e(VIEW_LOG_TAG, "launchSection Info null");
      return;
    }

    // Update last access time in shared preference
    String sectionLaunchList =
        PreferenceManager.getPreference(AppStatePreference.RECENT_SECTION_LAUNCH_LIST,
            Constants.EMPTY_STRING);
    List<RecentLaunchList> recentLaunchList =
        JsonUtils.fromJson(sectionLaunchList, new TypeToken<List<RecentLaunchList>>() {}.getType());
    if (recentLaunchList == null) {
      recentLaunchList = new ArrayList<>();
    }

    recentLaunchList.add(new RecentLaunchList(info.getId(), System.currentTimeMillis()));
    PreferenceManager.savePreference(AppStatePreference.RECENT_SECTION_LAUNCH_LIST,
        JsonUtils.toJson(recentLaunchList));

    AnalyticsHelper.logExploreButtonClickEvent(ExploreButtonType.BOTTOMBAR, getTabPosition(info),
        info.getTitle());

    switch (info.getType()) {
      case NEWS:
        launchNewsSection(info.getId(), false);
        break;
      case TV:
        launchBuzzSection(info.getId());
        break;
      case NOTIFICATIONINBOX:
        launchNotificationInbox();
        break;
      case WEB:
      case SEARCH:
        launchWebSection(info.getId(), info.getContentUrl(), info.getType());
        break;
      case FOLLOW:
        launchFollowSection(info.getId());
        break;
      case DEEPLINK:
        launchDeeplinkActivity(info.getDeeplinkUrl());
        break;
      default:
    }
  }

  public void setNotificationBadgeText(int unreadNotificationCount) {
    notificationCount = unreadNotificationCount;
    if (notificationCount == -1) {
      return;
    }
    NhTabVIewItem item = getChildWithType(AppSection.NOTIFICATIONINBOX);
    if (item != null) {
      item.setNotificationBadgeText(notificationCount);
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    BusProvider.getUIBusInstance().register(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    BusProvider.getUIBusInstance().unregister(this);
    super.onDetachedFromWindow();
  }

  @Subscribe
  public void onNotificationDbUpdate(NotificationUpdate notificationUpdate) {
    if (mShowNewNotificationCount) {
      setNotificationBadgeText(notificationUpdate.getUnseenNotificationCount());
    }
  }

  @Subscribe
  public void onTabClick(NHTabClicked tabCLicked) {
    if (tabCLicked.getTabType().equals(curSectionId)) {
      return;
    } else if (context instanceof Activity) {
      // ((Activity) context).finish();
    }
  }

  @Subscribe
  public void onTabUpdate(NHTabIconUpdate iconUpdate) {
    if (iconUpdate == null || iconUpdate.getUserAppSection() == null ||
        CommonUtils.isEmpty(iconUpdate
            .getUserAppSection().getId()) ||
        !CommonUtils.equals(curSectionId, iconUpdate.getUserAppSection().getId())) {
      return;
    }

    NhTabVIewItem childTab = getChildWithId(iconUpdate.getUserAppSection().getId());
    if (childTab == null) {
      return;
    }
    childTab.update(iconUpdate);
  }

  public void setTabViewSelection() {
    if (null == curSectionId || null == appSectionInfoList) {
      return;
    }
    Logger.v(TAG, "inside setTabViewSelection: " + curSectionId);
    View selectedChild = getChildWithTag(curSectionId);
    if (selectedChild instanceof NhTabVIewItem) {
      ((NhTabVIewItem) selectedChild).setSelected();
      ((NhTabVIewItem) selectedChild).hideHighlightIcon();
    }
  }

  View getChildWithTag(String id) {
    if (id == null) {
      return null;
    }
    for (int i = 0; i < mainNavBarLayout.getChildCount(); i++) {
      View currentView = mainNavBarLayout.getChildAt(i);
      if (id.equals(currentView.getTag())) {
        return currentView;
      }
    }
    return null;
  }

  NhTabVIewItem getChildWithType(AppSection section) {
    for (int i = 0; i < mainNavBarLayout.getChildCount(); i++) {
      View currentView = mainNavBarLayout.getChildAt(i);
      if (currentView instanceof NhTabVIewItem &&
          section == ((NhTabVIewItem) currentView).getInfo().getType()) {
        return (NhTabVIewItem) currentView;
      }
    }
    return null;
  }

  public NhTabVIewItem getChildWithId(String sectionId) {
    if (CommonUtils.isEmpty(sectionId)) {
      return null;
    }
    for (int i = 0; i < mainNavBarLayout.getChildCount(); i++) {
      View currentView = mainNavBarLayout.getChildAt(i);
      if (!(currentView instanceof NhTabVIewItem)) {
        continue;
      }
      AppSectionInfo sectionInfo = ((NhTabVIewItem) currentView).getInfo();
      if (sectionInfo != null && CommonUtils.equals(sectionId, sectionInfo.getId())) {
        return (NhTabVIewItem) currentView;
      }
    }
    return null;
  }

  @SuppressLint("ToastUsedDirectly")
  public void launchNewsSection(String sectionId, boolean landOnHomeTab) {
    UserAppSection prevNewsSection = AppSectionsProvider.INSTANCE.getUserAppSection(sectionId);

    if (prevNewsSection == null) {
      return;
    }

    if (isDefaultHomeSection(curSectionId)) {
      Intent newsHomeIntent =
          CommonNavigator.getNewsHomeIntent(getContext(), true, prevNewsSection.getId(),
              prevNewsSection.getAppSectionEntityKey(),
              AnalyticsHelper.getCurrentAppSectionPageReferrer(), false);
      if (newsHomeIntent != null) {
        newsHomeIntent.putExtra(NewsConstants.BUNDLE_LAND_ON_HOME_TAB, landOnHomeTab);
        HomeNavigator.launchSection(newsHomeIntent);
        BusProvider.getUIBusInstance().post(new NHTabClicked(prevNewsSection.getId()));
        AppUserPreferenceUtils.setAppSectionSelected(prevNewsSection);
      } else {
        Toast.makeText(getContext(), "News disabled", Toast.LENGTH_SHORT).show();
      }
    } else {
      boolean launchNewsHome = CommonNavigator.launchNewsHome(getContext(), true, prevNewsSection
              .getId(), prevNewsSection.getAppSectionEntityKey(),
          AnalyticsHelper.getCurrentAppSectionPageReferrer());

      if (launchNewsHome) {
        ((Activity) getContext()).finish();
        ((Activity) getContext()).overridePendingTransition(0, 0);
        BusProvider.getUIBusInstance().post(new NHTabClicked(prevNewsSection.getId()));
        AppUserPreferenceUtils.setAppSectionSelected(prevNewsSection);
      } else {
        Toast.makeText(getContext(), "News disabled", Toast.LENGTH_SHORT).show();
      }
    }
  }

  public void launchBuzzSection(String sectionId) {
    if (AndroidUtils.isInRestrictedMonkeyMode()) {
      return;
    }

    UserAppSection prevBuzzSection = AppSectionsProvider.INSTANCE.getUserAppSection(sectionId);
    if (prevBuzzSection == null) {
      prevBuzzSection = AppSectionsProvider.INSTANCE.getAnyUserAppSectionOfType(AppSection.TV);
    }

    if (prevBuzzSection != null) {
      launchBuzzSection(prevBuzzSection);
    }
  }

  private void launchBuzzSection(UserAppSection prevBuzzSection) {
    if (AndroidUtils.isInRestrictedMonkeyMode()) {
      return;
    }

    if (isDefaultHomeSection(curSectionId)) {
      Intent buzzHomeIntent =
          CommonNavigator.getTVHomeIntent(context, true, prevBuzzSection.getId(),
              prevBuzzSection.getAppSectionEntityKey(),
              AnalyticsHelper.getCurrentAppSectionPageReferrer());

      if (buzzHomeIntent != null) {
        HomeNavigator.launchSection(buzzHomeIntent);
        BusProvider.getUIBusInstance().post(new NHTabClicked(prevBuzzSection.getId()));
        AppUserPreferenceUtils.setAppSectionSelected(prevBuzzSection);
      } else {

      }
    } else {
      boolean launchTVHome = CommonNavigator.launchTVHome(context, true, prevBuzzSection.getId(),
          prevBuzzSection.getAppSectionEntityKey(),
          AnalyticsHelper.getCurrentAppSectionPageReferrer());
      if (launchTVHome) {
        ((Activity) context).finish();
        ((Activity) context).overridePendingTransition(0, 0);
        BusProvider.getUIBusInstance().post(new NHTabClicked(prevBuzzSection.getId()));
        AppUserPreferenceUtils.setAppSectionSelected(prevBuzzSection);
      }
    }
  }

  @SuppressLint("ToastUsedDirectly")
  private void launchNotificationInbox() {
    boolean launchNotiInbox = CommonNavigator.launchNotificationInbox(getContext(), true);
    if (launchNotiInbox) {
      ((Activity) getContext()).finish();
      ((Activity) getContext()).overridePendingTransition(0, 0);
    } else {
      Toast.makeText(getContext(), "Notification disabled", Toast.LENGTH_SHORT).show();
    }
  }

  @SuppressLint("ToastUsedDirectly")
  private void launchWebSection(String sectionId, String contentUrl, AppSection type) {
    UserAppSection prevWebSection = AppSectionsProvider.INSTANCE.getUserAppSection(sectionId);

    if (prevWebSection == null) {
      return;
    }

    prevWebSection = new UserAppSection.Builder().section(prevWebSection.getType()).sectionId
        (prevWebSection.getId()).entityKey(prevWebSection.getAppSectionEntityKey())
        .sectionContentUrl(contentUrl).build();
    boolean launchWebHome = CommonNavigator.launchWebSection(getContext(), true, prevWebSection
            .getId(), prevWebSection.getAppSectionEntityKey(), contentUrl,
        AnalyticsHelper.getCurrentAppSectionPageReferrer(), type);
    if (launchWebHome) {
      ((Activity) getContext()).finish();
      ((Activity) getContext()).overridePendingTransition(0, 0);
      BusProvider.getUIBusInstance().post(new NHTabClicked(prevWebSection.getId()));
      AppUserPreferenceUtils.setAppSectionSelected(prevWebSection);
    } else {
      Toast.makeText(getContext(), "Web section disabled", Toast.LENGTH_SHORT).show();
    }
  }

  @SuppressLint("ToastUsedDirectly")
  public void launchFollowSection(String sectionId) {
    UserAppSection prevFollowSection =
        AppSectionsProvider.INSTANCE.getUserAppSection(sectionId);

    if (prevFollowSection == null) {
      return;
    }

    prevFollowSection = new UserAppSection.Builder().section(prevFollowSection.getType()).sectionId
        (prevFollowSection.getId()).entityKey(prevFollowSection.getAppSectionEntityKey()).build();

    if (isDefaultHomeSection(curSectionId)) {
      Intent followHomeIntent = CommonNavigator.getFollowHomeIntent(getContext(), true,
          prevFollowSection.getId(), prevFollowSection.getAppSectionEntityKey(),
          AnalyticsHelper.getCurrentAppSectionPageReferrer());
      if (followHomeIntent != null) {
        HomeNavigator.launchSection(followHomeIntent);
        BusProvider.getUIBusInstance().post(new NHTabClicked(prevFollowSection.getId()));
        AppUserPreferenceUtils.setAppSectionSelected(prevFollowSection);
      } else {
        Toast.makeText(getContext(),
            CommonUtils.getString(R.string.follow_disabled), Toast.LENGTH_SHORT).show();
      }
    } else {
      boolean launchFollowHome = CommonNavigator.launchFollowHome(getContext(), true,
          prevFollowSection.getId(), prevFollowSection.getAppSectionEntityKey(),
          AnalyticsHelper.getCurrentAppSectionPageReferrer());
      if (launchFollowHome) {
        ((Activity) getContext()).finish();
        ((Activity) getContext()).overridePendingTransition(0, 0);
        BusProvider.getUIBusInstance().post(new NHTabClicked(prevFollowSection.getId()));
        AppUserPreferenceUtils.setAppSectionSelected(prevFollowSection);
      } else {
        Toast.makeText(getContext(),
            CommonUtils.getString(R.string.follow_disabled), Toast.LENGTH_SHORT).show();
      }
    }
  }

  @SuppressLint("ToastUsedDirectly")
  private void launchDeeplinkActivity(String url) {
    if (DeeplinkHelper.isInternalDeeplinkUrl(url)) {
      CommonNavigator.launchDeeplink(context, url, null);
    }
  }


  public int getTabPosition(AppSectionInfo info) {
    for (int index = 0; index < appSectionInfoList.size(); ++index) {
      if (info.getId().equals(appSectionInfoList.get(index).getId())) {
        // +1 - leftmost as 1
        return index + 1;
      }
    }
    return -1;
  }

  private boolean isDefaultHomeSection(String section) {
    if (NhAnalyticsEventSection.NEWS.getEventSection().equals(section) ||
        NhAnalyticsEventSection.TV.getEventSection().equals(section) ||
        NhAnalyticsEventSection.FOLLOW.getEventSection().equals(section)) {
      return true;
    } else {
      return false;
    }
  }
}

