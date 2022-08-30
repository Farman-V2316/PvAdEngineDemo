package com.newshunt.dhutil.view;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

import com.newshunt.analytics.entity.DialogBoxType;
import com.newshunt.common.helper.appconfig.AppConfig;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper;

/**
 * Created by shashikiran.nr on 11/3/2016.
 */

public class BatteryOptimizationDialogHelper {

  private static final long MIN_DELAY_FOR_TOAST = 1000L;

  public static void handleAutoStartDialogOnHome(Activity activity, String packageName,
                                                 String activityName, PageReferrer referrer) {
    if (checkToShowAutoStartDialogOnHome()) {
      showAutoStartDialog(activity, packageName, activityName, referrer);
    }
  }

  private static boolean checkToShowAutoStartDialogOnHome() {
    /**
     * check for is dialog shown before and 6th launch
     */
    return !getAutoStartEnableDialogShown();
  }

  public static void handleAutoStartDialogOnInboxOrSetting(Activity activity, String packageName,
                                                           String activityName,
                                                           PageReferrer referrer) {
    if (checkToShowAutoStartDialogOnInboxOrSetting()) {
      showAutoStartDialog(activity, packageName, activityName, referrer);
    }
  }

  private static boolean checkToShowAutoStartDialogOnInboxOrSetting() {
    /**
     * check for is dialog shown before.
     */
    return !getAutoStartEnableDialogShown();
  }

  // If the device is running Android Marshmallow and above, then check if the battery
  // optimization is already ignored. If yes, then don't show the dialog.
  private static void showAutoStartDialog(Activity activity, String packageName,
                                          String activityName,
                                          PageReferrer referrer) {
    if (activity.isFinishing() || activity.isDestroyed()) {
      return;
    }

    PowerManager pm = (PowerManager) CommonUtils.getApplication().getSystemService(Context.POWER_SERVICE);
    if (pm != null && pm.isIgnoringBatteryOptimizations(AppConfig.getInstance().getPackageName())) {
      setAutoStartEnableDialogShown(true);
      return;
    }
    new BatteryOptimizationDialog(activity, packageName, activityName, referrer).show();
    setAutoStartEnableDialogShown(true);
    DialogAnalyticsHelper.logDialogBoxViewedEvent(DialogBoxType.AUTOSTART_NOTIFICATIONS,
        referrer, NhAnalyticsEventSection.NOTIFICATION, null);
  }

  private static boolean getAutoStartEnableDialogShown() {
    return PreferenceManager.getPreference(
        GenericAppStatePreference.IS_AUTOSTART_ENABLE_DIALOG_SHOWN, false);
  }

  public static void setAutoStartEnableDialogShown(Boolean shown) {
    PreferenceManager.savePreference(GenericAppStatePreference.IS_AUTOSTART_ENABLE_DIALOG_SHOWN,
        shown);
  }


  public static void showAutoStartEnableToast(Activity activity) {

    // Handler which will run after 1 seconds.
    new Handler().postDelayed(new Runnable() {

      @Override
      public void run() {
        FontHelper.showCustomFontToast(activity,
            CommonUtils.getApplication().getString(R.string.auto_start_toast_text),
            Toast.LENGTH_SHORT);
      }
    }, MIN_DELAY_FOR_TOAST);
  }
}
