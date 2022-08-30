/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.share;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.FileUtil;
import com.newshunt.common.helper.common.ImageDownloadManager;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.helper.common.DailyhuntConstants;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.dataentity.viral.model.entity.ShareConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class with generic utilities for share banner and share Text
 *
 * @author ketki.garg on 30/11/17.
 */

public class ShareHelper {

  private static final String TAG = ShareHelper.class.getSimpleName();

  private static final String DH_SHARE_HIDDEN_DIRECTORY_NAME = ".dh_share";
  private static final String CUSTOMER_BITMAPS_DIRECTORY_NAME = "images";
  private static final String KEY_DEFAULT_SHARE_CONFIG = "default";
  private static final String PREF_KEY = AppStatePreference.SHARE_CONFIG_MAP.getName();
  private static final int MAX_CUSTOM_BITMAPS_COUNT = 10;
  private Gson gson;
  private ConcurrentHashMap<String, ShareConfig> shareMap;
  private static ShareHelper instance;

  private ShareHelper() {

  }

  public static ShareHelper getInstance() {
    if (instance == null) {
      synchronized (ShareHelper.class) {
        if (instance == null) {
          instance = new ShareHelper();
          instance.gson = new Gson();
          instance.shareMap = instance.getShareConfigMapFromPref();
        }
      }
    }
    return instance;
  }

  private boolean isShareBannerPresent(String fileName) {
    String bannerFilePath = getAbsoluteFilePathInShareDirectory(fileName);
    return FileUtil.checkIfFileExists(bannerFilePath);
  }

  public boolean isCustomBitmapPresent(String fileName) {
    String bannerFilePath = getAbsoluteFilePathInCustomBitmapDirectory(fileName);
    return FileUtil.checkIfFileExists(bannerFilePath);
  }

  /**
   * Save bitmap using picasso library
   *
   * @param bannerUrl path to download the file from
   */
  private void saveShareBannerImage(final String bannerUrl, String fileName) {
    Logger.v(TAG, "bannerUrl: " + bannerUrl);
    CommonUtils.runInBackground(() -> {
      try {
        Bitmap bitmap = ImageDownloadManager.getInstance().startDownload(bannerUrl);
        Logger.d(TAG, "onBitmapLoaded");
        if (bitmap != null) {
          getUriAndSaveBitmap(new WeakReference<>(bitmap), fileName, true);
        }
      } catch (Exception e) {
        Logger.e(TAG, "exception: " + e.toString());
      }
    });
  }

  private String getAbsoluteFilePathInShareDirectory(String fileName) {
    return getShareHiddenDirectoryPath() + Constants.FORWARD_SLASH + fileName;
  }

  private String getAbsoluteFilePathInCustomBitmapDirectory(String fileName) {
    return getCustomBitmapSubDirectoryPath() + Constants.FORWARD_SLASH + fileName;
  }

  private String getShareHiddenDirectoryPath() {
    return CommonUtils.getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        .getAbsolutePath() + Constants.FORWARD_SLASH + DH_SHARE_HIDDEN_DIRECTORY_NAME;
  }

  private String getCustomBitmapSubDirectoryPath() {
    return getShareHiddenDirectoryPath() + Constants.FORWARD_SLASH +
        CUSTOMER_BITMAPS_DIRECTORY_NAME;
  }

  /**
   * The method returns shareBannerUrl in case no banner is stored while handshake, it fallbacks
   * to bundled image
   *
   * @param fileNames
   * @return share banner bitmap
   */
  public Bitmap getShareBannerBitmap(String[] fileNames) {
    if (!CommonUtils.isEmpty(fileNames)) {
      for (int i = fileNames.length - 1; i >= 0; i--) {
        String fileName = fileNames[i];
        if (isShareBannerPresent(fileName)) {
          return BitmapFactory.decodeFile(getAbsoluteFilePathInShareDirectory(fileName));
        }
      }
    }
    if (isDefaultShareBannerPresent()) {
      return BitmapFactory.decodeFile(
          getAbsoluteFilePathInShareDirectory(KEY_DEFAULT_SHARE_CONFIG));
    }
    return BitmapFactory.decodeResource(CommonUtils.getApplication().getResources(),
        R.drawable.dh_banner);
  }

  private boolean isDefaultShareBannerPresent() {
    return isShareBannerPresent(KEY_DEFAULT_SHARE_CONFIG);
  }

  /**
   * The method is responsible for:
   * 1. creating the share hidden directory
   * 2. reading, compressing and saving the bitmap into fileSystem and
   * returns corresponding Uri
   *
   * @param bitmap   sourceBitmap
   * @param fileName fileName to store with
   * @return Uri of the stored file
   */
  public Uri getUriAndSaveBitmap(WeakReference<Bitmap> bitmap, String fileName, boolean
      isBannerImage) {
    try {
      String filePath = null;
      createShareHiddenDirectory();
      if (isBannerImage) {
        filePath = getShareHiddenDirectoryPath();
      } else {
        createCustomBitmapSubDirectory();
        filePath = getCustomBitmapSubDirectoryPath();
      }
      String finalFilePath = filePath + Constants.FORWARD_SLASH + fileName;
      if (!FileUtil.checkIfFileExists(finalFilePath)) {
        if (!saveBitmapImage(bitmap, finalFilePath)) {
          return null;
        }
      }
      Context context = CommonUtils.getApplication().getApplicationContext();
      return FileUtil.getFileUri(context,finalFilePath);
    } finally {
      if (bitmap != null && bitmap.get() != null) {
        bitmap.get().recycle();
        bitmap.clear();
      }
    }
  }

  private void createShareHiddenDirectory() {
    createDirectory(getShareHiddenDirectoryPath());
  }

  private void createCustomBitmapSubDirectory() {
    createDirectory(getCustomBitmapSubDirectoryPath());
  }

  private static void createDirectory(String path) {
    if (CommonUtils.isEmpty(path)) {
      return;
    }
    File folder = new File(path);
    if (!folder.exists()) {
      folder.mkdir();
    }
  }

  /**
   * The method aims at overlapping one bitmap over another based on configurations
   * Could be further extended depending upon the requirement
   *
   * @param primaryBitmap source bitmap
   * @param overlayBitmap bitmap to be overlapped
   * @return custom Bitmap
   */
  public Bitmap overlayBitmap(Bitmap primaryBitmap, Bitmap overlayBitmap) {
    int primaryBitmapWidth = primaryBitmap.getWidth();
    int primaryBitmapHeight = primaryBitmap.getHeight();

    int overlayBitmapWidth = overlayBitmap.getWidth();
    int overlayBitmapHeight = overlayBitmap.getHeight();
    overlayBitmapHeight = (overlayBitmapHeight * primaryBitmapWidth) / overlayBitmapWidth;
    overlayBitmap = Bitmap.createScaledBitmap(
        overlayBitmap, primaryBitmapWidth, overlayBitmapHeight, false);

    float marginLeft = 0;
    float marginTop = (float) (primaryBitmapHeight);
    Bitmap customBitmap =
        Bitmap.createBitmap(primaryBitmapWidth, primaryBitmapHeight + overlayBitmapHeight,
            primaryBitmap.getConfig());

    Canvas canvas = new Canvas(customBitmap);
    canvas.drawBitmap(primaryBitmap, new Matrix(), null);
    canvas.drawBitmap(overlayBitmap, marginLeft, marginTop, null);
    return customBitmap;
  }

  public void updateShareConfig(String id, String shareText, String shareBannerUrl) {
    if (CommonUtils.isEmpty(shareBannerUrl) && CommonUtils.isEmpty(shareText)) {
      return;
    }
    boolean isSaveText = true, isSaveBanner = true;

    //check pref for previous value
    if (!CommonUtils.isEmpty(shareMap)) {
      ShareConfig config = shareMap.get(id);
      if (config != null) {
        String savedShareBannerUrl = config.getShareBannerUrl();
        String savedShareText = config.getShareText();
        if (CommonUtils.isEmpty(shareBannerUrl) || (!CommonUtils.isEmpty(savedShareBannerUrl) &&
            savedShareBannerUrl.equalsIgnoreCase(shareBannerUrl) &&
            isShareBannerPresent(id))) {
          isSaveBanner = false;
        }
        if (CommonUtils.isEmpty(shareText) || (!CommonUtils.isEmpty(savedShareText) && savedShareText
            .equalsIgnoreCase(shareText))) {
          isSaveText = false;
        }
      }
    } else if (shareMap == null) {
      shareMap = new ConcurrentHashMap<>();
    }

    if (isSaveBanner || isSaveText) {
      shareMap.put(id, createShareConfigItem(shareText, shareBannerUrl));
      saveShareConfigMapIntoPref();
    }

    if (isSaveBanner && !CommonUtils.isEmpty(shareBannerUrl)) {
      saveShareBannerImage(shareBannerUrl, id);
    }
  }

  public void updateDefaultShareConfig(String shareText, String shareBannerUrl) {
    updateShareConfig(KEY_DEFAULT_SHARE_CONFIG, shareText, shareBannerUrl);
  }

  private ShareConfig createShareConfigItem(String shareText, String bannerUrl) {
    ShareConfig shareConfig = new ShareConfig();
    shareConfig.setShareBannerUrl(bannerUrl);
    shareConfig.setShareText(shareText);
    return shareConfig;
  }

  private ConcurrentHashMap<String, ShareConfig> getShareConfigMapFromPref() {
    String storedConcurrentHashMapString = PreferenceManager.getString(PREF_KEY, null);
    Type type = new TypeToken<ConcurrentHashMap<String, ShareConfig>>() {
    }.getType();
    FirebaseAnalytics.getInstance(CommonUtils.getApplication()).setUserProperty(DailyhuntConstants.CRASHLYTICS_KEY_SHARE_VALUE,
        storedConcurrentHashMapString);
    return gson.fromJson(storedConcurrentHashMapString, type);
  }

  private void saveShareConfigMapIntoPref() {
    String concurrentHashMapString = gson.toJson(shareMap);
    PreferenceManager.saveString(PREF_KEY, concurrentHashMapString);
  }

  public String getShareText(String[] ids) {
    if (!CommonUtils.isEmpty(shareMap)) {
      if (!CommonUtils.isEmpty(ids)) {
        for (int i = ids.length - 1; i >= 0; i--) {
          ShareConfig config = shareMap.get(ids[i]);
          if (config != null && !CommonUtils.isEmpty(config.getShareText())) {
            return config.getShareText();
          }
        }
      }
      ShareConfig config = shareMap.get(KEY_DEFAULT_SHARE_CONFIG);
      if (config != null && !CommonUtils.isEmpty(config.getShareText())) {
        return config.getShareText();
      }
    }
    return CommonUtils.getString(R.string.share_txt);
  }

  public void clearFiles() {
    deleteCustomBitmapDirectory(new File(getCustomBitmapSubDirectoryPath()));
  }

  private void deleteCustomBitmapDirectory(File dir) {
    if (dir != null && dir.isDirectory()) {
      File[] files = dir.listFiles();
      if (!CommonUtils.isEmpty(files) && files.length > MAX_CUSTOM_BITMAPS_COUNT) {
        for (File f : files) {
          f.delete();
        }
        dir.delete();
      }
    }
  }

  private static boolean saveBitmapImage(WeakReference<Bitmap> bitmap, String fileName) {
    File newFile = new File(fileName);
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(newFile);
      if (bitmap == null || bitmap.get() == null) {
        return false;
      }
      bitmap.get().compress(Bitmap.CompressFormat.PNG, 90, out);
      Logger.v(TAG, "saveBitmapImage");
      return true;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      if (bitmap != null) {
        bitmap.clear();
      }
    }
    return false;
  }
}
