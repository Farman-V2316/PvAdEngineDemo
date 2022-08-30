/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.common.track;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.model.ApiResponse;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import java.io.File;

import androidx.annotation.NonNull;
import retrofit2.Response;

/**
 * Utility class for dailyhunt-commons module
 *
 * @author: bedprakash on 10/07/17.
 */

public class DailyhuntUtils {
  private static final String PRELOADED_APK_DIR = "apk";
  private static final String PRELOADED_APK_NAME = "dailyhunt.apk";

  public static void fireTrackRequestForApi(@NonNull Response response) {
    logTrackUrl(response.body(), false);
  }

  public static void logTrackUrl(Object data, boolean fromCache) {
    if (!(data instanceof ApiResponse) || ((ApiResponse) data).getTrack() == null) {
      return;
    }
    if (AsyncTrackHandler.FIRE_TRACK_FROM_CACHE || !fromCache) {
      AsyncTrackHandler.getInstance().sendTrack(((ApiResponse) data).getTrack().getUrl());
    }

    if (AsyncTrackHandler.FIRE_COMSCORE_TRACKS_FROM_CACHE || !fromCache) {
      if (!CommonUtils.isEmpty(((ApiResponse) data).getTrack().getComscoreUrls())) {
        for (String comScoreUrl : ((ApiResponse) data).getTrack().getComscoreUrls()) {
          AsyncTrackHandler.getInstance().sendTrackForComscore(comScoreUrl);
        }
      }
    }
  }

  /**
   * On some Samsung devices, Dailyhunt is preloaded as a stub app. A real dailyhunt apk is
   * wrapped in asset folder of a stub app. On opening the stub app, the real apk is copied to
   * "files/apk" directly and installed. After installation, we need to delete the APK in the files
   * directory. This method takes care of deleting the APK from "files/apk" directory.
   */
  public static void deleteUnwantedFiles() {
    try {
      if (!PreferenceManager.getPreference(GenericAppStatePreference.PRELOADED_APK_DELETED,
          false)) {
        File filesDir = CommonUtils.getApplication().getFilesDir();
        File apkDir = new File(filesDir + File.separator + PRELOADED_APK_DIR);
        if (apkDir.exists() && apkDir.isDirectory()) {
          File dailyhuntAPK =
              new File(apkDir.getAbsolutePath() + File.separator + PRELOADED_APK_NAME);
          if (dailyhuntAPK.exists()) {
            boolean deleted = dailyhuntAPK.delete();
            if (deleted) {
              apkDir.delete();
              Logger.d("PreloadAPK", "Deleted Preloaded APK");
              PreferenceManager.savePreference(GenericAppStatePreference.PRELOADED_APK_DELETED,
                  true);
            }
          }
        } else {
          PreferenceManager.savePreference(GenericAppStatePreference.PRELOADED_APK_DELETED, true);
        }
      }
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  /**
   * Is register or first handshake done? This is added in profile release to ensure login
   * requests go through only after a register/handshake has succeeded.
   */
  public static boolean isRegisterOrFirstHandshakeDoneInThisVersion() {
    return PreferenceManager.getPreference(AppStatePreference.REGISTER_OR_FIRST_HANDSHAKE_DONE,
        false);
  }

  /**
   * Sets the preference REGISTER_OR_FIRST_HANDSHAKE_DONE
   */
  public static void setRegisterOrFirstHandshakeDoneInThisVersion(
      final boolean registerOrHandshakeDone) {
    PreferenceManager.savePreference(AppStatePreference.REGISTER_OR_FIRST_HANDSHAKE_DONE,
        registerOrHandshakeDone);
  }
}
