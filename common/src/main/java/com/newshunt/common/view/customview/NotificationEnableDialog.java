/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.common.view.customview;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.newshunt.common.util.R;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.preference.GenericAppStatePreference;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.common.view.customview.fontview.NHTextView;


/**
 * Dialog class used to show the Notification enabling feature
 *
 * @author Shashikiran.nr
 */
public class NotificationEnableDialog extends Dialog {

  public NotificationEnableDialog(Context context) {
    super(context);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.app_notification_enable_layout);

    NHTextView dialogTitle = (NHTextView) findViewById(R.id.notification_dialogTitletext);
    NHTextView dialogHeader = (NHTextView) findViewById(R.id.notification_dialogHeaderText);
    NHTextView positiveButton = (NHTextView) findViewById(R.id.notification_positive_button);
    NHTextView negativeButton = (NHTextView) findViewById(R.id.notification_negative_button);

    dialogTitle.setText(CommonUtils.getApplication().getString(R.string.notification_title_text));
    dialogHeader.setText(CommonUtils.getApplication().getString(R.string.notification_header_text_pre_marshmallow));
    positiveButton.setText(CommonUtils.getApplication().getString(R.string.notification_button_enable));
    negativeButton.setText(CommonUtils.getApplication().getString(R.string.dialog_cancel));

    dialogHeader.setText(CommonUtils.getString(R.string.notification_header_text_marshmallow));

    positiveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getContext().startActivity(new Intent(android.provider.Settings
            .ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", getContext().getPackageName(), null)));
        PreferenceManager.savePreference(
            GenericAppStatePreference.SYSTEM_NOTIFICATION_ENABLE_DIALOG_SHOWN, false);
        dismiss();
      }
    });

    negativeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });
  }
}
