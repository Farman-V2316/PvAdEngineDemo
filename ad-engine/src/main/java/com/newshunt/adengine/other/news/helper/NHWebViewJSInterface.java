package com.newshunt.adengine.other.news.helper;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.reflect.TypeToken;
import com.newshunt.adengine.other.dhutil.helper.BackgroundChangeListener;
import com.newshunt.common.helper.AppUtilsProvider;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.common.NHWebViewUtils;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.common.helper.cookie.CustomCookieManager;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.info.ConnectionInfoHelper;
import com.newshunt.common.helper.info.DeviceInfoHelper;
import com.newshunt.common.helper.info.LocationInfoHelper;
import com.newshunt.common.helper.preference.AppUserPreferenceUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.helper.share.ShareContent;
import com.newshunt.common.helper.share.ShareFactory;
import com.newshunt.common.helper.share.ShareHelper;
import com.newshunt.common.helper.share.ShareUi;
import com.newshunt.common.helper.share.ShareViewShowListener;
import com.newshunt.common.helper.sticky.StickyAudioPlayControlsKt;
import com.newshunt.common.util.R;
import com.newshunt.common.view.view.BaseFragment;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.common.JsPhoneNumber;
import com.newshunt.dataentity.common.JsPostActionParam;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.pages.PageEntity;
import com.newshunt.dataentity.dhutil.model.entity.PhoneSelectorInterface;
import com.newshunt.dataentity.news.analytics.NewsReferrer;
import com.newshunt.dataentity.notification.asset.OptInEntity;
import com.newshunt.dataentity.notification.asset.OptOutEntity;
import com.newshunt.dataentity.social.entity.MenuLocation;
import com.newshunt.dhutil.ExtnsKt;
import com.newshunt.dhutil.helper.autoplay.AutoPlayHelper;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dhutil.helper.preference.AstroPreference;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.dhutil.helper.theme.ThemeUtils;
import com.squareup.otto.Subscribe;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * JS interface for WebView's to handle NHCommand
 *
 * @author maruti.borker
 */
public class NHWebViewJSInterface {

  public static final String INTERFACE_NAME = "newsHuntAction";
  protected WebView webView;
  protected WeakReference<Activity> activityRef;
  protected WeakReference<Fragment> fragmentRef;
  private PageReferrer pageReferrer;
  private ShareContent shareContent;
  public static final String LOG_TAG = "NHWebViewJSInterface";
  private MenuJsInterface menuJsInterface;
  private RepostWebItemJsInterface repostWebItemJsInterface;
  private PageEntity pageEntity;

  public NHWebViewJSInterface(WebView webView, Activity activity) {
    this.webView = webView;
    this.activityRef = activity != null ? new WeakReference<>(activity) : null;
  }

  public NHWebViewJSInterface(WebView webView, Activity activity, PageReferrer pageReferrer) {
    this(webView, activity);
    this.pageReferrer = pageReferrer;
  }

  public NHWebViewJSInterface(WebView webView, Activity activity, Fragment fragment,
                              PageReferrer pageReferrer) {
    this(webView, activity, pageReferrer);
    this.fragmentRef = fragment != null ? new WeakReference<>(fragment) : null;
  }

  public MenuJsInterface getMenuJsInterface() {
    return menuJsInterface;
  }

  public void setMenuJsInterface(MenuJsInterface menuJsInterface) {
    this.menuJsInterface = menuJsInterface;
  }

  public RepostWebItemJsInterface getRepostWebItemJsInterface() {
    return repostWebItemJsInterface;
  }

  public void setRepostWebItemJsInterface(RepostWebItemJsInterface repostWebItemJsInterface) {
    this.repostWebItemJsInterface = repostWebItemJsInterface;
  }

  public PageEntity getPageEntity() {
    return pageEntity;
  }

  public void setPageEntity(PageEntity pageEntity) {
    this.pageEntity = pageEntity;
  }

  @JavascriptInterface
  public void showJSToast(String show) {
    if (CommonUtils.isEmpty(show)) {
      return;
    }
    if (activityRef.get() != null) {
      FontHelper.showCustomFontToast(activityRef.get(), show, Toast.LENGTH_SHORT);
    }
  }

  @JavascriptInterface
  public String getDefaultSharePackageName(){
    return PreferenceManager.getPreference(AppStatePreference.SELECTED_APP_TO_SHARE,Constants.EMPTY_STRING);
  }

  @JavascriptInterface
  public String getAstroSupportedLanguages() {
    return PreferenceManager.getPreference(AstroPreference.ASTRO_SUPPORTED_LANGUAGES, Constants
        .EMPTY_STRING);
  }

  @JavascriptInterface
  public String getAstroPriorityLanguages() {
    return PreferenceManager.getPreference(AstroPreference.ASTRO_PRIORITY_LANGUAGES, Constants
        .EMPTY_STRING);
  }

  @JavascriptInterface
  public boolean isAstroSubscribed() {
    return PreferenceManager.getPreference(AstroPreference.ASTRO_SUBSCRIBED, false);
  }

  @JavascriptInterface
  public void setNewsReferrer(String referrer) {
    if (CommonUtils.isEmpty(referrer)) {
      return;
    }
    NewsReferrer newsReferrer = NewsReferrer.getNewsReferrer(referrer);
    if (newsReferrer == null) {
      return;
    }
    pageReferrer = new PageReferrer(newsReferrer);
  }

  @JavascriptInterface
  public String getLangCodes() {
    return AppUserPreferenceUtils.getUserLanguages();
  }

  @JavascriptInterface
  public String getAppLanguage() {
    return AppUserPreferenceUtils.getUserNavigationLanguage();
  }

  @JavascriptInterface
  public String getConvertedString(String string) {
    return FontHelper.getFontConvertedString(string);
  }

  @JavascriptInterface
  public void setBackgroundColor(String backgroundColor) {
    if (webView == null || fragmentRef == null) {
      return;
    }
    BackgroundChangeListener backgroundChangeListener =
        (BackgroundChangeListener) fragmentRef.get();

    if (backgroundChangeListener != null &&
        (backgroundChangeListener instanceof BackgroundChangeListener)) {
      int backgroundColorCode = ViewUtils.getColor(backgroundColor,
          ThemeUtils.getBackgroundColor(CommonUtils.getApplication()));
      webView.post(() -> backgroundChangeListener.onBackgroundColorChanged(backgroundColorCode));
    }
  }

  @JavascriptInterface
  public int getAPIVersionNumber() {
    return Build.VERSION.SDK_INT;
  }

  @JavascriptInterface
  public void enableScrollEventPost(String value) {
    if (fragmentRef != null && fragmentRef.get() instanceof BaseFragment) {
      ((BaseFragment) fragmentRef.get()).setJsScrollEnabled(Boolean.valueOf(value));
    }
  }

  @JavascriptInterface
  public void enableJsRefreshOnPullDown(String value) {
    if (fragmentRef != null && fragmentRef.get() instanceof BaseFragment) {
      ((BaseFragment) fragmentRef.get()).setJsRefreshEnabled(Boolean.valueOf(value));
    }
  }

  @JavascriptInterface
  public void jsScrollToTop() {
    if (fragmentRef != null && fragmentRef.get() instanceof BaseFragment) {
      ((BaseFragment) fragmentRef.get()).jsScrollToTop();
    }
  }

  @JavascriptInterface
  public void loggerJsFunction(String value) {
    Logger.i("JSInterface", "JS Logger :: " + value);
  }

  @JavascriptInterface
  public boolean isNightMode() {
    return ThemeUtils.isNightMode();
  }

  @JavascriptInterface
  public String getClientId() {
    return UserPreferenceUtil.getClientId();
  }

  @JavascriptInterface
  public String getEdition() {
    return UserPreferenceUtil.getUserEdition();
  }

  @JavascriptInterface
  public float getDeviceWidth() {
    return CommonUtils.getDeviceScreenWidth();
  }

  @JavascriptInterface
  public float getDeviceHeight() {
    return CommonUtils.getDeviceScreenHeight();
  }

  @JavascriptInterface
  public String getPrimaryLanguage() {
    return AppUserPreferenceUtils.getUserPrimaryLanguage();
  }

  @JavascriptInterface
  public String getSecondaryLanguages() {
    return AppUserPreferenceUtils.getUserSecondaryLanguages();
  }

  @JavascriptInterface
  public String getLatitude() {
    return LocationInfoHelper.getLocationInfo(false).getLat();
  }

  @JavascriptInterface
  public String getLongitude() {
    return LocationInfoHelper.getLocationInfo(false).getLon();
  }

  @JavascriptInterface
  public String getLatitudeForAds() {
    return LocationInfoHelper.getLocationInfo(true).getLat();
  }

  @JavascriptInterface
  public String getLongitudeForAds() {
    return LocationInfoHelper.getLocationInfo(true).getLon();
  }

  @JavascriptInterface
  public String getConnectionType() {
    return ConnectionInfoHelper.getConnectionType();
  }

  @JavascriptInterface
  public String getAppVersion() {
    return DeviceInfoHelper.getAppVersion();
  }

  @JavascriptInterface
  public void setWebCookiesToHttp(String loadedUrl, String domainName) {
    if (CommonUtils.isEmpty(loadedUrl) || CommonUtils.isEmpty(domainName)) {
      return;
    }
    NHWebViewUtils.storeWebViewCookiestoHttp(loadedUrl, domainName);
  }

  @JavascriptInterface
  public String getShareFloatingIconType() {
    return PreferenceManager.getPreference(GenericAppStatePreference.FLOATING_ICON_TYPE,
        ShareUi.FLOATING_ICON.getShareUiName());
  }

  @JavascriptInterface
  public boolean isAutoPlayAllowed() {
    return AutoPlayHelper.isAutoPlayAllowed();
  }

  @JavascriptInterface
  public void setWebCookiesToHttp(String domainName) {
    if (webView == null || CommonUtils.isEmpty(domainName)) {
      return;
    }
    // The callbacks are made in another thread JavaBridge. To get the url of the webView, we
    // must obtain it in Main thread, otherwise it will throw an exception.
    webView.post(() -> {
      String loadedUrl = webView != null ? webView.getUrl() : null;
      if (CommonUtils.isEmpty(loadedUrl)) {
        return;
      }
      setWebCookiesToHttp(loadedUrl, domainName);
    });
  }

  //A js interface for whether an app is enabled on the device or not.
  @JavascriptInterface
  public boolean isAppEnabled(String packageName) {
    if (CommonUtils.isEmpty(packageName)) {
      return true;
    }

    return !AndroidUtils.isAppDisabled(packageName);
  }

  @JavascriptInterface
  public String getCookie(String url, String cookieName) {
    if (CommonUtils.isEmpty(url) || CommonUtils.isEmpty(cookieName)) {
      return Constants.EMPTY_STRING;
    }
    return CustomCookieManager.getCookieValue(url, cookieName);
  }

  @Nullable
  private Activity getActivity() {
    Activity activity = activityRef.get();
    if (activity == null && fragmentRef.get() != null) {
      activity = fragmentRef.get().getActivity();
    }
    return activity;
  }

  private void handleDirectShare(String packageName, ShareContent shareContent,
                                 Activity activity) {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setType(Constants.INTENT_TYPE_TEXT);
    ShareHelper shareContentHelper =
        ShareFactory.getShareHelper(packageName, activity, sendIntent, shareContent, true,
            shareContent.isAds());
    shareContentHelper.share();
    logShareAnalyticsForWeb();
  }

  private ShareViewShowListener shareViewShowListener = new ShareViewShowListener() {
    @Override
    public void onShareViewClick(String packageName, ShareUi shareUi) {
      if (shareContent == null || getActivity() == null) {
        return;
      }
      handleDirectShare(packageName, shareContent, getActivity());
    }

    @Override
    public Intent getIntentOnShareClicked(ShareUi shareUi) {
      if (shareContent == null) {
        return null;
      }

      logShareAnalyticsForWeb();
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType(Constants.INTENT_TYPE_TEXT);
      if (!CommonUtils.isEmpty(shareContent.getSubject())) {
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareContent.getSubject());
      }
      String shareUrl = shareContent.getShareUrl();
      String title = shareContent.getTitle();
      Map langTitles = new HashMap();
      if (!CommonUtils.isEmpty(shareContent.getContentLanguage())) {
        langTitles.put(shareContent.getContentLanguage(), title);
      }
      String shareText =
          AppUtilsProvider.getAppUtilsService()
              .getShareableString(shareUrl, title, langTitles, true);

      shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
      if (shareContent.getFileUri() != null) {
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareContent.getFileUri());
      }
      return Intent.createChooser(shareIntent,
          CommonUtils.getString(R.string.share_source));
    }
  };


  @JavascriptInterface
  public void setSupportMultipleWindows(boolean supportMultipleWindows) {
    if (webView != null) {
      webView.post(() -> webView.getSettings().setSupportMultipleWindows(supportMultipleWindows));
    }
  }

  /**
   * Event for sharing via web.
   */
  private void logShareAnalyticsForWeb() {
    //TODO: PANDA removed
    //if (shareContent == null) {
    //  return;
    //}
    //
    //Map<NhAnalyticsEventParam, Object> map = new HashMap<>();
    //if (!CommonUtils.isEmpty(shareContent.getPackageName())) {
    //  map.put(NhAnalyticsNewsEventParam.SHARE_TYPE, shareContent.getPackageName());
    //}
    //if (!CommonUtils.isEmpty(shareContent.getShareUi())) {
    //  map.put(NhAnalyticsNewsEventParam.SHARE_UI, shareContent.getShareUi());
    //}
    //if (pageReferrer != null && pageReferrer.getReferrer() != null) {
    //  map.put(NhAnalyticsAppEventParam.REFERRER, pageReferrer.getReferrer());
    //}
    //if (pageReferrer != null && !CommonUtils.isEmpty(pageReferrer.getId())) {
    //  map.put(NhAnalyticsAppEventParam.REFERRER_ID, pageReferrer.getId());
    //}
    //if (pageReferrer != null) {
    //  map.put(NhAnalyticsAppEventParam.REFERRER_FLOW, pageReferrer.getReferrer());
    //  map.put(NhAnalyticsAppEventParam.REFERRER_FLOW_ID, pageReferrer.getId());
    //}
    //
    //AnalyticsClient.log(NhAnalyticsAppEvent.STORY_SHARED, NhAnalyticsEventSection.NEWS, map);
    //webShareAPIHelper.onShared(shareContent);
  }

  @JavascriptInterface
  public void logEvent(String eventName, String sectionName, String jsonParams) {
    //TODO: PANDA removed
    //try {
    //  Map<String, String> stringParams = new HashMap<>();
    //  stringParams = JsonUtils.fromJson(jsonParams, stringParams.getClass());
    //  NhAnalyticsEventSection section = null;
    //  if (!DataUtil.isEmpty(sectionName)) {
    //    section = NhAnalyticsEventSection.valueOf(sectionName);
    //  }
    //
    //  if (section == null) {
    //    section = NhAnalyticsEventSection.APP;
    //  }
    //
    //  Map<String, Object> params = new HashMap<>();
    //  params.putAll(stringParams);
    //  AnalyticsClient.logStringParamsBasedEvents(eventName, section, params);
    //} catch (Exception ex) {
    //  AnalyticsClient.logDynamic(NhAnalyticsDevEvent.DEV_CUSTOM_ERROR, NhAnalyticsEventSection.APP,
    //      null, Collections.singletonMap("Error", ex.getMessage()), false);
    //}
  }

  @JavascriptInterface
  public void onEventOptIn(String json) {
    if (CommonUtils.isEmpty(json)) {
      return;
    }

    List<OptInEntity> optInEntityList =
        JsonUtils.fromJson(json, new TypeToken<List<OptInEntity>>() {
        }.getType());
    //TODO: PANDA removed
    //StickyNotificationsManager.INSTANCE.userOptInNotifications(optInEntityList);
  }

  @JavascriptInterface
  public void onEventOptOut(String json) {
    if (CommonUtils.isEmpty(json)) {
      return;
    }

    List<OptOutEntity> optOutEntityList =
        JsonUtils.fromJson(json, new TypeToken<List<OptOutEntity>>() {
        }.getType());
    //TODO: PANDA removed
    //StickyNotificationsManager.INSTANCE.optOutNotifications(optOutEntityList, true);
  }

  @JavascriptInterface
  public void stopAudioCommentary(String json) {
    if (!StickyAudioPlayControlsKt.STICKY_AUDIO_COMMENTARY_ENABLED || CommonUtils.isEmpty(json)) {
      return;
    }

    OptOutEntity optOutEntity = JsonUtils.fromJson(json, new TypeToken<OptOutEntity>() {
    }.getType());
    //TODO: PANDA removed
    //StickyNotificationsManager.INSTANCE.jsCallbackStopAudio(optOutEntity);
  }

  @JavascriptInterface
  public String getAllOptedInIds(String type) {
    if (CommonUtils.isEmpty(type)) {
      return Constants.EMPTY_STRING;
    }
    //TODO: PANDA removed
    //List<String> optedIds = StickyNotificationsManager.INSTANCE.getOptedInNotificationIds(type);
    //return (CommonUtils.isEmpty(optedIds)) ? Constants.EMPTY_STRING :
    //    TextUtils.join(Constants.COMMA_CHARACTER, optedIds.toArray());
    return Constants.EMPTY_STRING;
  }

  @JavascriptInterface
  public boolean getOptInState(String id, String type) {
    if (CommonUtils.isEmpty(id) || CommonUtils.isEmpty(type)) {
      return false;
    }
    //TODO: PANDA removed
    //return StickyNotificationsManager.INSTANCE.getOptInState(id, type);
    return false;
  }

  @JavascriptInterface
  public String getOptInSeries(String json, String type) {
    if (CommonUtils.isEmpty(json) || CommonUtils.isEmpty(type)) {
      return Constants.EMPTY_STRING;
    }

    //TODO: PANDA removed
    //List<String> optedIdsFromSeries = StickyNotificationsManager.INSTANCE
    //    .getOptInSeries(JsonUtils.fromJson(json, new TypeToken<List<String>>() {
    //    }.getType()), type);
    //return CommonUtils.isEmpty(optedIdsFromSeries) ? Constants.EMPTY_STRING :
    //    JsonUtils.toJson(optedIdsFromSeries);
    return Constants.EMPTY_STRING;
  }

  public PageReferrer getPageReferrer() {
    return pageReferrer;
  }

  @JavascriptInterface
  public String getAudioOptInState(String id, String type) {
    if (!StickyAudioPlayControlsKt.STICKY_AUDIO_COMMENTARY_ENABLED) {
      return Constants.EMPTY_STRING;
    }

    //TODO: PANDA removed
    //StickyAudioCommentary stickyAudioCommentary =
    //    StickyNotificationsManager.INSTANCE.getAudioCommentaryState(id, type);
    //return (stickyAudioCommentary == null || stickyAudioCommentary.getState() == null) ?
    //    Constants.EMPTY_STRING : stickyAudioCommentary.getState().getJsState();
    return Constants.EMPTY_STRING;
  }

  @JavascriptInterface
  public void onMenuButtonClick(@NotNull String postJson, @Nullable Boolean isInList,
                                @Nullable Boolean canHide) {
    if (menuJsInterface != null) {
      menuJsInterface.onMenuButtonClick(postJson, (isInList==null || isInList) ? "List" : "Detail",
          (isInList==null || isInList) ? MenuLocation.LIST.name() : MenuLocation.DETAIL.name(), Constants.EMPTY_STRING);
    }
  }

  @JavascriptInterface
  public boolean canShowMenuButton() {
    if (menuJsInterface != null) {
      return menuJsInterface.canShowMenuButton();
    }
    return false;
  }

  @JavascriptInterface
  @Nullable
  public String isStoryRead(@NotNull String postsJson) {
    if (menuJsInterface != null) {
      return menuJsInterface.isStoryRead(postsJson);
    }
    return null;
  }

  @JavascriptInterface
  @Nullable
  public String isStoryDisliked(@NotNull String postsJson) {
    if (menuJsInterface != null) {
      return menuJsInterface.isStoryDisliked(postsJson);
    }
    return null;
  }


  @JavascriptInterface
  public boolean isSocialBarSupported() {
    return true;
  }

  /**
   * To be called where webview shows cards with social interactions.
   * Currently enable only for webview fragments and activities. Disabled for viewholders.
   */
  public void observeBus() {
    LifecycleOwner lifecycleOwner = determineLifecycleOwner();
    if(lifecycleOwner != null){
      ExtnsKt.observeWhenCreated(BusProvider.getUIBusInstance(), lifecycleOwner, this);
    }
  }

  // TODO(satosh.dhanyamraju): check sender: comment and repost
  @Subscribe
  public void onJsRepostWebItemPostResponse(JsPostActionParam jsPostActionParam){
    Logger.d(LOG_TAG, "onJsRepostWebItemPostResponse: received : "+ jsPostActionParam);
    if (!CommonUtils.isEmpty(jsPostActionParam.getId())) {
      String script = NHWebViewUtils.formatScript("onPostAction", JsonUtils.toJson(jsPostActionParam));
      NHWebViewUtils.callJavaScriptFunction(webView, script);
    }
  }

  /**
   * Use fragment, if available; else activity
   */
  @Nullable
  private LifecycleOwner determineLifecycleOwner() {
    Fragment fragment = fragmentRef != null ? fragmentRef.get() : null;
    Activity activity = activityRef != null ? activityRef.get() : null;

    final LifecycleOwner lifecycleOwner;
    if (fragment != null) {
      lifecycleOwner = fragment;
    } else if (activity instanceof AppCompatActivity) {
      lifecycleOwner = (AppCompatActivity) activity;
    } else {
      lifecycleOwner = null;
    }
    return lifecycleOwner;
  }

  @JavascriptInterface
  public void onRepostClicked(String postsJson){
    if(repostWebItemJsInterface != null && !CommonUtils.isEmpty(postsJson) ){
      repostWebItemJsInterface.onRepostClicked(postsJson);
    }
  }

  @JavascriptInterface
  public float getDeviceDensity() {
    return CommonUtils.getDeviceDensity();
  }

  @JavascriptInterface
  public void getPhNumber() {
    observeBus();
    PhoneSelectorInterface phoneSelectorInterface = ((PhoneSelectorInterface) getActivity());
    if (phoneSelectorInterface != null) {
      phoneSelectorInterface.showPhoneNumberDialog();
    }
  }

  @JavascriptInterface
  public boolean isViewPortHeightFixed() {
    return true;
  }

  @Subscribe
  public void updatePhNumber(JsPhoneNumber jsPhoneNumber) {
    Logger.d(LOG_TAG, "onPhNumber: received : " + jsPhoneNumber.getPhNumber());
    String script = NHWebViewUtils.formatScript(Constants.JS_UPDATE_PH_NUMBER, jsPhoneNumber.getPhNumber());
    NHWebViewUtils.callJavaScriptFunction(webView, script);
  }

}