/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.permissionhelper.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.permissionhelper.PermissionAdapter;
import com.newshunt.permissionhelper.R;
import com.newshunt.permissionhelper.Callbacks.DialogActionCallback;
import com.newshunt.permissionhelper.Callbacks.PermissionRationaleProvider;

/**
 * Default rationale dialog for permission helper
 *
 * @author by satyanarayana.avv on 03-08-2016.
 */
public class PermissionDialogBuilder {

  public static Dialog showAppDialogForPermissions(@NonNull final Activity activity,
                                                   @NonNull final PermissionAdapter permissionAdapter,
                                                   @NonNull final PermissionRationaleProvider rationaleProvider,
                                                   @NonNull final List<Permission> permissions,
                                                   @NonNull final DialogActionCallback callback,
                                                   @NonNull final PageReferrer referrer) {

    final Dialog permissionDialog = new Dialog(activity);

    LayoutInflater inflater = activity.getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.layout_permissions_dialog, null);

    TextView titleText = dialogView.findViewById(R.id.permission_dialog_title);
    titleText.setText(rationaleProvider.getRationaleTitle());
    TextView descText = dialogView.findViewById(R.id.permission_dialog_desc);
    descText.setText(rationaleProvider.getRationaleDesc());


    ListView listView = dialogView.findViewById(R.id.list_permissions);
    PermissionListAdapter adapter =
        new PermissionListAdapter(activity, permissions, rationaleProvider);
    listView.setAdapter(adapter);

    TextView positiveButton = dialogView.findViewById(R.id.dialog_positive_button);
    positiveButton.setText(rationaleProvider.getPositiveBtn());
    positiveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        callback.onPositiveButtonClick();
        permissionDialog.dismiss();
        PermissionAnalytics.logPermissionDialogBoxActionEvent(permissions, referrer, true,
            false, Constants.DIALOG_ACCEPT, permissionAdapter.getExtraParamsMap());
      }
    });

    TextView negativeButton = dialogView.findViewById(R.id.dialog_negative_button);
    negativeButton.setText(rationaleProvider.getNegativeBtn());
    negativeButton.setOnClickListener((v) -> {
      callback.onNegativeButtonClick();
      permissionDialog.cancel();
      PermissionAnalytics.logPermissionDialogBoxActionEvent(permissions, referrer, true,
          false, Constants.DIALOG_LATER, permissionAdapter.getExtraParamsMap());
      try {
        BusProvider.getUIBusInstance().unregister(permissionAdapter);
      } catch (Exception ex) {
        // Do nothing
      }
    });

    permissionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    permissionDialog.setContentView(dialogView);
    Window win = permissionDialog.getWindow();
    setDialogWidth(activity, win, 0.9);
    try {
      permissionDialog.show();
    } catch (WindowManager.BadTokenException ex) {
      // Activity got closed before dialog can be shown
      return null;
    }

    permissionDialog.setCancelable(false);
    PermissionAnalytics.logPermissionDialogBoxViewedEvent(permissions, referrer, true, permissionAdapter.getExtraParamsMap());

    return permissionDialog;
  }

  private static void setDialogWidth(Activity activity, Window dialogWindow, double
      percentageOfScreenHeight) {
    WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    DisplayMetrics metrics = new DisplayMetrics();
    display.getMetrics(metrics);
    Double width = metrics.widthPixels * percentageOfScreenHeight;
    int height = WindowManager.LayoutParams.WRAP_CONTENT;
    dialogWindow.setLayout(width.intValue(), height);
  }
}
