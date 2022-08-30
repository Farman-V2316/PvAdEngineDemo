/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.widget.Toast;

import com.newshunt.analytics.entity.DialogBoxType;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.ViewUtils;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.view.customview.fontview.NHTextView;
import com.newshunt.dhutil.R;
import com.newshunt.dhutil.analytics.DialogAnalyticsHelper;

/**
 * @author by anshul.jain on 11/3/2016.
 */

public class BatteryOptimizationDialog extends Dialog {

  private String device_Security_App_Packagename;
  private String device_Security_App_Activityname;
  public PageReferrer referrer;
  private NHTextView dialogTitle, dialogHeader, positiveButton;
  private Activity activity;

  public BatteryOptimizationDialog(Activity activity, String packageName, String activtyName,
                                   PageReferrer referrer) {
    super(activity);
    this.activity = activity;
    this.device_Security_App_Packagename = packageName;
    this.device_Security_App_Activityname = activtyName;
    this.referrer = referrer;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.battery_optimization_prompt);


    dialogTitle = findViewById(R.id.autostart_dialogTitletext);
    dialogHeader = findViewById(R.id.autostart_dialogHeadertext);
    positiveButton = findViewById(R.id.positive_button);
    positiveButton.setText(CommonUtils.getString(com.newshunt.common.util.R.string.ok_text));
    openBatteryOptimizationScreen();
  }

  private void openBatteryOptimizationScreen() {
    ViewUtils.setTextWithFontSpacing(dialogTitle, CommonUtils.getString(com.newshunt.common.util.R.string
        .disable_battery_optimization_prompt_text), 1f);
    ViewUtils.setTextWithFontSpacing(dialogHeader, CommonUtils.getString(com.newshunt.common.util.R.string
        .disable_battery_optimization_prompt_text), 1f);

    positiveButton.setOnClickListener(v -> {
      DialogAnalyticsHelper.deployDialogUsActionEvent(DialogAnalyticsHelper.DIALOG_ACTION_OK,
          DialogBoxType.AUTOSTART_NOTIFICATIONS, referrer, NhAnalyticsEventSection.NOTIFICATION,
          false);
      dismiss();
      try {
        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        if (intent.resolveActivity(CommonUtils.getApplication().getPackageManager()) != null) {
          CommonUtils.getApplication().startActivity(intent);
        }
      } catch (Exception e) {
        Logger.e(BatteryOptimizationDialog.class.getSimpleName(), "Error launching activity", e);
      }
    });
  }

  private void autoStartAction() {
    ViewUtils.setTextWithFontSpacing(dialogTitle, CommonUtils.getString(com.newshunt.common.util.R.string
        .auto_start_dialog_title_text), 1f);
    ViewUtils.setTextWithFontSpacing(dialogHeader, CommonUtils.getString(com.newshunt.common.util.R.string
        .auto_start_dialog_header_text), 1f);

    positiveButton.setOnClickListener(v -> {
      DialogAnalyticsHelper.deployDialogUsActionEvent(DialogAnalyticsHelper.DIALOG_ACTION_OK,
          DialogBoxType.AUTOSTART_NOTIFICATIONS, referrer, NhAnalyticsEventSection.NOTIFICATION,
          false);

      Intent intent = new Intent();
      intent.setComponent(
          new ComponentName(device_Security_App_Packagename, device_Security_App_Activityname));
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      try {
        getContext().startActivity(intent);
      } catch (Exception e) {
        Logger.e(BatteryOptimizationDialog.class.getSimpleName(), "Error launching activity", e);
        FontHelper.showCustomFontToast(getContext(), CommonUtils.getString(com.newshunt.common.util.R.string.error_generic),
            Toast.LENGTH_SHORT);
      }
      BatteryOptimizationDialogHelper.showAutoStartEnableToast(activity);
      dismiss();
    });
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    DialogAnalyticsHelper.deployDialogUsActionEvent(DialogAnalyticsHelper.DIALOG_ACTION_DISMISS,
        DialogBoxType.AUTOSTART_NOTIFICATIONS, referrer, NhAnalyticsEventSection.NOTIFICATION,
        false);
  }
}
