package com.dailyhunt.tv.players.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.bwutil.BwEstRepo;
import com.dailyhunt.tv.exolibrary.util.ExoBufferSettings;
import com.dailyhunt.tv.exolibrary.util.ExoUtilsKt;
import com.dailyhunt.tv.players.constants.PlayerContants;
import com.dailyhunt.tv.players.helpers.PlayerNetworkType;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsUserAction;
import com.newshunt.dataentity.common.asset.VideoAsset;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.adupgrade.AdsUpgradeInfo;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerDimensions;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerItemQuality;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerVideoQuality;
import com.newshunt.dataentity.news.model.entity.server.asset.ContentScale;
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset;
import com.newshunt.dataentity.news.model.entity.server.asset.NewsVideoAsset;
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerAsset;
import com.newshunt.dataentity.news.model.entity.server.asset.VideoItem;
import com.newshunt.dhutil.helper.AdsUpgradeInfoProvider;
import com.newshunt.dhutil.helper.PlayerDataProvider;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.news.model.helper.VideoPlayedCache;
import com.newshunt.sdk.network.connection.ConnectionSpeed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * Created by Jayanth on 09/05/18.
 */

public class PlayerUtils {

  private static final String TAG = PlayerUtils.class.getName();
  private static String idsMap;
  private static NhAnalyticsUserAction userAction;
  private static PlayerItemQuality selectedQuality;
  public static final String DEFAULT_QUALITY_BUCKET_VALUE = "h";
  public static final String DEFAULT_RESOULTION_BUCKET_VALUE = "l";
  private static final Map<String, String> QUALITY_BUCKETS_MAP  = new HashMap<String, String>() {{
    put("slow", "l");
    put("average", "m");
    put("good", "h");
    put("fast", "vh");
    put("veryfast", "hd");
  }};
  private static boolean useYoutubeWebviewIFrame = false;
  private static boolean isYoutubePlayerAvailable = true;

  public static RelativeLayout.LayoutParams getScaledParamsForData(PlayerAsset item) {
    if (item == null) {
      Logger.d(TAG, "item.getDataUrl() is NULL");
      return new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    RelativeLayout.LayoutParams layoutParams = null;
    if (item.getHeight() <= 0 || item.getWidth() <= 0) {
      //Default fix to 9 : 16 ratio
      int width = CommonUtils.getDeviceScreenWidth();
      int height = ((width * 9) / 16);

      layoutParams = new RelativeLayout.LayoutParams(width, height);
      layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

      item.setWidth(width);
      item.setHeight(height);
      ContentScale scale = PlayerUtils.getScale(CommonUtils.getApplication(), width, height);
      item.setDataScale(scale);
      return layoutParams;
    }

    ContentScale scale = item.getDataScale();
    if (item.isInExpandMode()) {
      scale = item.getDataExpandScale();
    }

    if (scale == null) {
      scale = PlayerUtils.getScale(CommonUtils.getApplication(), item.getWidth(), item.getHeight());
      item.setDataScale(scale);
    }

    layoutParams =
        new RelativeLayout.LayoutParams(scale.getWidth(), scale.getHeight());
    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    return layoutParams;
  }

  public static RelativeLayout.LayoutParams getScaledParamsForData(PlayerAsset item,
                                                                   int screenWidth,
                                                                   int screenHeight) {
    if (item == null) {
      Logger.d(TAG, "item.getDataUrl() is NULL");
      return new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    RelativeLayout.LayoutParams layoutParams = null;
    if (item.getHeight() <= 0 || item.getWidth() <= 0) {
      //Default fix to 9 : 16 ratio
      int width = screenWidth;
      int height = ((width * 9) / 16);

      layoutParams = new RelativeLayout.LayoutParams(width, height);
      layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

      item.setWidth(width);
      item.setHeight(height);
      ContentScale scale =
          PlayerUtils.getScale(CommonUtils.getApplication(), width, height, screenWidth,
              screenHeight);
      item.setDataScale(scale);
      return layoutParams;
    }

    ContentScale scale = item.getDataScale();
    if (item.isInExpandMode()) {
      scale = item.getDataExpandScale();
    }

    if (scale == null) {
      scale = PlayerUtils.getScale(CommonUtils.getApplication(), item.getWidth(), item.getHeight(),
          screenWidth, screenHeight);
      item.setDataScale(scale);
    }

    Logger.i("Autoplay",
        "Scale width and height are  " + scale.getWidth() + ":" + scale.getHeight());
    layoutParams =
        new RelativeLayout.LayoutParams(scale.getWidth(), scale.getHeight());
    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    return layoutParams;
  }

  public static ConstraintLayout.LayoutParams getScaledParamsForDataConstraint(
      NewsVideoAsset item) {
    if (item == null) {
      Logger.d(TAG, "item.getDataUrl() is NULL");
      return new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
          ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    ConstraintLayout.LayoutParams layoutParams = null;
    if (item.getHeight() <= 0 || item.getWidth() <= 0) {
      //Default fix to 9 : 16 ratio
      int width = CommonUtils.getDeviceScreenWidth();
      int height = ((width * 9) / 16);

      layoutParams = new ConstraintLayout.LayoutParams(width, height);

      item.setWidth(width);
      item.setHeight(height);
      ContentScale scale = PlayerUtils.getScale(CommonUtils.getApplication(), width, height);
      item.setDataScale(scale);
      return layoutParams;
    }

    ContentScale scale = item.getDataScale();
    if (item.isInExpandMode()) {
      scale = item.getDataExpandScale();
    }

    if (scale == null) {
      scale = PlayerUtils.getScale(CommonUtils.getApplication(), item.getWidth(), item.getHeight());
      item.setDataScale(scale);
    }

    layoutParams =
        new ConstraintLayout.LayoutParams(scale.getWidth(), scale.getHeight());
    return layoutParams;
  }

  public static ContentScale getScale(Context context, int contentWidth, int contentHeight) {

    ContentScale scale = new ContentScale();
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    int screenWidth = metrics.widthPixels;
    int tempWidth = contentWidth;
    int tempHeight = contentHeight;
    int finalWidth;
    int finalHeight;
    int maxContentHeight = metrics.heightPixels - CommonUtils.getPixelFromDP(
        PlayerContants.TV_STATUS_BAR_HEIGHT * 2, context);

    if (tempWidth >= tempHeight) {
      tempWidth = screenWidth;
      tempHeight = (screenWidth * contentHeight) / contentWidth;

      if (tempHeight <= maxContentHeight) {
        finalWidth = tempWidth;
        finalHeight = tempHeight;
      } else {
        finalHeight = maxContentHeight;
        finalWidth = (finalHeight * contentWidth) / contentHeight;
      }
    } else {
      tempWidth = screenWidth;
      tempHeight = (screenWidth * contentHeight) / contentWidth;

      if (tempHeight > maxContentHeight) {
        tempHeight = maxContentHeight;
        tempWidth = (tempHeight * contentWidth) / contentHeight;
      }
      finalHeight = tempHeight;
      finalWidth = tempWidth;
    }

    scale.setWidth(finalWidth);
    scale.setHeight(finalHeight);
    return scale;
  }

  public static ContentScale getScale(Context context, int contentWidth, int
      contentHeight, int screenWidth, int screenHeight) {

    ContentScale scale = new ContentScale();
    int tempWidth = contentWidth;
    int tempHeight = contentHeight;
    int finalWidth;
    int finalHeight;
    int maxContentHeight = screenHeight;

    if (tempWidth >= tempHeight) {
      tempWidth = screenWidth;
      tempHeight = (screenWidth * contentHeight) / contentWidth;

      if (tempHeight <= maxContentHeight) {
        finalWidth = tempWidth;
        finalHeight = tempHeight;
      } else {
        finalHeight = maxContentHeight;
        finalWidth = (finalHeight * contentWidth) / contentHeight;
      }
    } else {
      tempWidth = screenWidth;
      tempHeight = (screenWidth * contentHeight) / contentWidth;

      if (tempHeight > maxContentHeight) {
        tempHeight = maxContentHeight;
        tempWidth = (tempHeight * contentWidth) / contentHeight;
      }
      finalHeight = tempHeight;
      finalWidth = tempWidth;
    }

    scale.setWidth(finalWidth);
    scale.setHeight(finalHeight);
    return scale;
  }

  public static PlayerVideoQuality getDesiredVideoQuality() {
    PlayerVideoQuality desiredVideoQuality = null;
    PlayerDimensions dimensions = PlayerDataProvider.getInstance().getPlayerDimensions();
    // For Auto mode
    desiredVideoQuality = dimensions.getAdaptiveSettings();
    // Check the connection speed and based on that pickup nominal bitrate from other video
    // quality array

    PlayerNetworkType currentNetworkType = getNetworkType(CommonUtils.getApplication());
    List<PlayerVideoQuality> videoQualities = dimensions.getVideoQualities();

    if (!CommonUtils.isEmpty(videoQualities) && null != currentNetworkType) {
      for (int i = 0; i < videoQualities.size(); i++) {
        if (currentNetworkType.getIndex() == videoQualities.get(i).getNetworkType()) {
          desiredVideoQuality.setNomialBitRateForHLSFirstvariant(
              videoQualities.get(i).getNomialBitRateForHLSFirstvariant());
          desiredVideoQuality.setBufferMinSize(videoQualities.get(i).getBufferMinSize());
          desiredVideoQuality.setBufferMaxSize(videoQualities.get(i).getBufferMaxSize());
          break;
        }
      }
    }

    if (desiredVideoQuality != null) {
      ExoBufferSettings
          .setBufferSettings(desiredVideoQuality.getBufferMinSize(),
              desiredVideoQuality.getBufferMaxSize(),
              desiredVideoQuality.getHlsMinTimeForSwitchUpMs(),
              desiredVideoQuality.getHlsMaxTimeForSwitchDownMs(),
              getBufferSegmentSize(desiredVideoQuality),
              getInitialBufferMs(desiredVideoQuality),
              getPlaybackDurationAfterRebuffer(desiredVideoQuality),
              desiredVideoQuality.isUseDefaultConfigForLivestreams());
    }

    return desiredVideoQuality;
  }

  public static int getPlaybackDurationAfterRebuffer(PlayerVideoQuality playerVideoQuality) {
    if (playerVideoQuality.getPlaybackDurationAfterRebuffer() == 0) {
      return DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS;
    } else {
      return playerVideoQuality.getPlaybackDurationAfterRebuffer();
    }
  }

  public static int getBufferSegmentSize(PlayerVideoQuality playerVideoQuality) {
    if (playerVideoQuality.getBufferSegmentSize() == 0) {
      return ExoUtilsKt.BUFFER_SEGMENT_SIZE;
    } else {
      return playerVideoQuality.getBufferSegmentSize();
    }
  }

  public static int getInitialBufferMs(PlayerVideoQuality playerVideoQuality) {
    if (playerVideoQuality.getInitialBufferMs() == 0) {
      return DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS;
    } else {
      return playerVideoQuality.getInitialBufferMs();
    }
  }

  public static PlayerNetworkType getNetworkType(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = connectivityManager.getActiveNetworkInfo();
    // No network connectivity then NetworkInfo becomes null. So below condition is applied.
    if (info == null || info.getType() == ConnectivityManager.TYPE_WIFI) {
      return PlayerNetworkType.NETWORK_TYPE_WIFI;
    } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
      return PlayerNetworkType.fromIndex(info.getSubtype());
    }
    return PlayerNetworkType.NETWORK_TYPE_UNKNOWN;
  }

  //Force EveryOne to use this
  public static String getQualifiedVideoUrl(VideoAsset asset) {
    if (!CommonUtils.isEmpty(asset.getStreamCachedUrl())) {
      return asset.getStreamCachedUrl();
    }
    String streamUrl = getQualifiedUrl(asset);
    asset.setStreamCachedUrl(streamUrl);
    return streamUrl;
  }

  private static String getQualifiedUrl(VideoAsset asset) {
    String url = asset.getUrl();
    if (CommonUtils.isEmpty(url)) {
      return url;
    }

    Logger.d(TAG, "VIDEO::BEFORE -> " + url);
    String resolutionBucket = DEFAULT_RESOULTION_BUCKET_VALUE;
    String uc = BwEstRepo.currentConnectionQuality();
    if (TextUtils.isEmpty(uc)) {
      uc = "good";
      Logger.d(TAG, "uc is null, pick default uc = good");
    }
    asset.setSelectedQuality(uc);
    Logger.d(TAG, "selected uc = " + uc);
    String quality = QUALITY_BUCKETS_MAP.get(uc);
    if(TextUtils.isEmpty(quality)) {
      quality = DEFAULT_QUALITY_BUCKET_VALUE;
      Logger.d(TAG, "quality is null, pick default quality = h");
    }

    PlayerDimensions playerDimensions = PlayerDataProvider.getInstance().getPlayerDimensions();
    if (playerDimensions == null || CommonUtils.isEmpty(playerDimensions.getResolutionBucket())) {
      url = url.replace("{0}", resolutionBucket);
      url = url.replace("{1}", quality);
      return url;
    }

//    PlayerNetworkType networkType = getNetworkType(CommonUtils.getApplication());
    resolutionBucket = playerDimensions.getResolutionBucket();
//    quality = PlayerDimensionUtils.getVideoQuality(networkType, playerType);


    url = url.replace("{0}", resolutionBucket);
    url = url.replace("{1}", quality);

    Logger.d(TAG, "VIDEO::AFTER -> " + url);
    return url;
  }

  public static PlayerItemQuality getItemQualityPlayed(String fileType) {
    return selectedQuality;
  }

  public static PlayerItemQuality getItemQualitySetting(String fileType) {
    return selectedQuality;
  }

  public static void setItemQuality(PlayerItemQuality itemQuality) {
    selectedQuality = itemQuality;
  }

  public static PlayerItemQuality getItemQuality() {
    return selectedQuality;
  }

  public static void resetVideoStartSystemTime() {
    PreferenceManager.saveLong(PlayerContants.VIDEO_START_SYSTEM_TIME, 0L);
  }


  public static void resetWebViewState(WebView webView) {
    if (null == webView) {
      return;
    }
    try {
      webView.loadUrl("about:blank");
    } catch (Exception e) {
    }

  }

  public static String getAppName() {
    ApplicationInfo appInfo = CommonUtils.getApplication().getApplicationInfo();
    if (appInfo != null) {
      int stringId = appInfo.labelRes;
      return stringId == 0 ? appInfo.nonLocalizedLabel.toString() :
          CommonUtils.getApplication().getString(stringId);
    }
    return PlayerContants.EMPTY_STR;
  }

  public static String getResolutionBucket(Context context) {
    //tvImageDimensions object wont be created every time
    PlayerDimensions playerDimensions = PlayerDataProvider.getInstance().getPlayerDimensions();
    if (playerDimensions != null) {
      return playerDimensions.getResolutionBucket();
    }
    return PlayerConstants.DEFAULT_RESOULTION_BUCKET_VALUE;
  }

  /**
   * @return the screen height in pixels
   */
  public static int getScreenHeight() {
    WindowManager windowManager =
        (WindowManager) CommonUtils.getApplication().getSystemService(Context.WINDOW_SERVICE);
    Display display = windowManager.getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    return size.y;
  }

  public static int getUniqueId() {
    return View.generateViewId();
  }

  public static String getIdsMap() {
    return idsMap;
  }

  public static void setIdsMap(String idsMap) {
    PlayerUtils.idsMap = idsMap;
  }

  public static NhAnalyticsUserAction getUserAction() {
    return userAction;
  }

  public static void setUserAction(NhAnalyticsUserAction userAction) {
    PlayerUtils.userAction = userAction;
  }

  public static void addVideoToCache(VideoItem videoItem) {
    if (videoItem != null) {
      Logger.d("Videoitem",
          "added - " + videoItem.getItemId() + " - duration : " + videoItem.getDurationInSec());
      VideoPlayedCache.INSTANCE.put(videoItem, System.currentTimeMillis());
    } else {
      Logger.d("Videoitem", "is null");
    }
  }

  public static boolean isUseYoutubeWebviewIFrame() {
    return useYoutubeWebviewIFrame;
  }

  public static void setYoutubeIFrame() {
    useYoutubeWebviewIFrame = true;
  }

  public static boolean isYoutubePlayerAvailable() {
    return isYoutubePlayerAvailable;
  }

  public static void setYoutubePlayerAvailable(boolean youtubePlayerAvailable) {
    isYoutubePlayerAvailable = youtubePlayerAvailable;
  }

  public static void resetYoutubeIFrame() {
    useYoutubeWebviewIFrame = false;
  }

  public static void checkErroneousYTPlayer() {
    if (isUseYoutubeWebviewIFrame() && isYoutubePlayerAvailable()) {
      resetYoutubeIFrame();
    }
  }

  public static boolean canShowCompanionAd(PlayerAsset playerAsset) {
    if (playerAsset == null) {
      return false;
    }
    AdsUpgradeInfo adsUpgradeInfo = AdsUpgradeInfoProvider.getInstance().getAdsUpgradeInfo();
    if (adsUpgradeInfo == null || adsUpgradeInfo.getInstreamAdsConfig() == null ||
        adsUpgradeInfo.getInstreamAdsConfig().getCompanionAdsConfig() == null) {
      return false;
    }
    float aspectRatio = (1f * playerAsset.getWidth()) / playerAsset.getHeight();
    return aspectRatio >= adsUpgradeInfo.getInstreamAdsConfig()
        .getCompanionAdsConfig().getVideoAspectRatioLimit();
  }

  /**
   * if connections speed is slow or 2G return true
   *
   * @return true/false
   */
  public static boolean isConnectionSlow() {
    String connectionSpeed = BwEstRepo.getINST().currentConnectionQuality();
    if(ConnectionSpeed.SLOW.name().equalsIgnoreCase(connectionSpeed) ||
            ConnectionSpeed.AVERAGE.name().equalsIgnoreCase(connectionSpeed)) {
      return true;
    }
    return false;
  }

  public static long getTimeBasedOnNetwork() {
    long time = 0;
    if (isConnectionSlow()) {
      time = PreferenceManager.getPreference(
          AppStatePreference.SLOW_NETWORK_TIME, 3000L);
    } else {
      time = PreferenceManager.getPreference(
          AppStatePreference.GOOD_NETWORK_TIME, 1000L);
    }
    Logger.d("APISEQUENCE", "time = " + time);
    return time;
  }

  /**
   * Source :- http://stackoverflow.com/questions/24048308/how-to-get-the-video-id-from-a-youtube-url-with-regex-in-java
   */
  public static String extractYouTubeVideoId(String videoUrl) {
    String videoId = null;
    Pattern pattern = Pattern.compile(
        ".*(?:youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=)([^#\\&\\?]*).*");
    Matcher matcher = pattern.matcher(videoUrl);
    if (matcher.matches()) {
      videoId = matcher.group(1);
    }
    return CommonUtils.isEmpty(videoId) ? "#" : videoId;
  }


  private static final String VIDEO_ID = "vId";
  private static final String VIDEO_LENGTH = "vLength";
  private static final String VIDEO_AUTOPLAY = "vAutoplay";
  private static final String VIDEO_INDEX = "vIndex";
  private static final String VIDEO_PLAYER_TYPE = "vFileType";
  private static final String LANGUAGE_KEY = "vLanguage";
  private static final String LIVE_KEY = "vLive";
  private static final String SOURCE_KEY = "sourceKey";
  private static final String SOURCE_ID = "sourceId";
  private static final String SOURCE_TYPE = "sourceType";
  private static final String CHANNEL_KEY = "channelKey";

  public static Map<String,String> getInstreamAdParams(ExoPlayerAsset exoAsset, int vIndex,
                                                       boolean autoplay) {
    if (exoAsset == null) {
      return null;
    }
    Map<String,String> adParams = new HashMap<>();

    //&vlength=50&vindex=2&vtimesincelast=124&vid=923490234
    adParams.put(VIDEO_ID, exoAsset.getId());
    adParams.put(VIDEO_LENGTH, String.valueOf(intoSeconds(exoAsset.getDurationLong())));
    adParams.put(VIDEO_INDEX, String.valueOf(vIndex));

    adParams.put(LIVE_KEY, String.valueOf(exoAsset.isLiveStream()));

    if (!CommonUtils.isEmpty(exoAsset.getPlayerType())) {
      adParams.put(VIDEO_PLAYER_TYPE, exoAsset.getPlayerType());
    }
    if (!CommonUtils.isEmpty(exoAsset.getLanguageKey())) {
      adParams.put(LANGUAGE_KEY, exoAsset.getLanguageKey());
    }
    if (null != exoAsset.getSourceInfo()) {
      adParams.put(SOURCE_KEY, exoAsset.getSourceInfo().getPlayerKey());
      adParams.put(SOURCE_ID, exoAsset.getSourceInfo().getSourceId());
      adParams.put(SOURCE_TYPE, exoAsset.getSourceInfo().getSourceSubType());
      adParams.put(CHANNEL_KEY, exoAsset.getSourceInfo().getLegacyKey());
    }
    if (autoplay) {
      adParams.put(VIDEO_AUTOPLAY, "true");
    }
    return adParams;
  }

  private static int intoSeconds(long milli) {
    return (int) TimeUnit.MILLISECONDS.toSeconds(milli);
  }

}
