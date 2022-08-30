/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.share;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;

import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;

import java.util.List;

/**
 * Created by anshul on 30/05/17.
 * A utility class for sharing.
 */

public class ShareUtils {
  private static final String TAG = "ShareUtils";
  /**
   * A utility method to show sharing options based on the OS version.
   *
   * @param shareViewListener - Listener for Share View
   * @param context           - Context
   * @param shareUi           - Type of ShareUI
   */
  public static void clickOnMoreShareOptions(ShareViewShowListener shareViewListener, Context
          context, ShareUi shareUi, Activity activity) {

    if (shareViewListener == null || context == null || shareUi == null) {
      Logger.e(NHShareView.class.getSimpleName(), "shareViewListener or context or shareUi is " +
              "null");
      return;
    }

    if (null != activity) {
      AndroidUtils.startSharingActivityForResult(activity, shareViewListener.getIntentOnShareClicked
              (shareUi));
    } else {
      context.startActivity(shareViewListener.getIntentOnShareClicked(shareUi));
    }
  }

  @NonNull
  public static ShareUi getShareUiForFloatingIcon() {
    String sharefloatingIconType =
        PreferenceManager.getPreference(GenericAppStatePreference.FLOATING_ICON_TYPE,
            Constants.EMPTY_STRING);
    ShareUi shareUi = ShareUi.fromName(sharefloatingIconType);
    if (shareUi == null) {
      shareUi = ShareUi.FLOATING_ICON;
    }
    return shareUi;
  }

  @NonNull
  public static ShareUi getShareUiForWebFloatingIcon() {
    String sharefloatingIconType =
        PreferenceManager.getPreference(GenericAppStatePreference.FLOATING_ICON_TYPE,
            Constants.EMPTY_STRING);

    ShareUi shareUi = ShareUi.WEB;

    if (ShareUi.FLOATING_ICON_BENT_ARROW.getShareUiName().equals(sharefloatingIconType)) {
      shareUi = ShareUi.WEB_BENT_ARROW;
    } else if (ShareUi.FLOATING_ICON_W_STRING.getShareUiName().equals(sharefloatingIconType)) {
      shareUi = ShareUi.WEB_W_STRING;
    }
    return shareUi;
  }

  public static boolean appInstalledOrNot(Context context, String uri) {
    PackageManager pm = context.getPackageManager();
    try {
      pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
    }

    return false;
  }
}
