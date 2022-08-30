/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.permissionhelper.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.TextView;

import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.font.FontHelper;
import com.newshunt.common.helper.font.FontType;
import com.newshunt.common.helper.font.FontWeight;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File contains utility methods for the module
 *
 * @author: bedprakash.rout on 8/4/2016.
 */

public class PermissionUtils {

  public static String[] getPermissionStrings(List<Permission> permissions) {

    String[] names = new String[permissions.size()];
    for (int i = 0; i < permissions.size(); i++) {
      names[i] = permissions.get(i).getPermission();
    }
    return names;
  }

  public static boolean hasPermission(Context context, String permission) {
    return ContextCompat.checkSelfPermission(context, permission) ==
        PackageManager.PERMISSION_GRANTED;
  }

  public static ArrayList<Permission> checkForDeniedPermissions(Context context,
                                                                List<Permission> permissions) {
    ArrayList<Permission> result = new ArrayList<>(0);
    if (permissions == null) {
      return result;
    }
    for (Permission permission : permissions) {
      if (PermissionUtils.hasPermission(context, permission.getPermission())) {
        // PrefUtils.removePermissionAskedEntry(context, permission.getPermission());
        continue;
      }
      result.add(permission);
    }
    return result;
  }

  public static boolean isPermissionBlocked(Activity activity, Permission permission) {
    return isPermissionBlocked(activity, permission.getPermission());
  }

  public static boolean isPermissionBlocked(Activity activity, String permission) {
    boolean alreadyAsked = PrefUtils.checkIfAlreadyAsked(activity.getApplication(), permission);
    boolean shouldRequest = ActivityCompat.shouldShowRequestPermissionRationale(activity,
        permission);
    return alreadyAsked && !shouldRequest;
  }

  public static ArrayList<Permission> checkForBlockedPermission(Activity activity,
                                                                List<Permission> permissions) {
    ArrayList<Permission> result = new ArrayList<>(0);
    if (permissions == null) {
      return result;
    }
    for (Permission permission : permissions) {
      if (isPermissionBlocked(activity, permission)) {
        result.add(permission);
      }
    }
    return result;
  }

  public static void showAppSettingsSnackbar(final Activity activity, final String message,
                                             final String action) {
    Snackbar snackbar =
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setAction(action, view -> PermissionUtils.openAppSettingActivity(activity));
    TextView tv = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
    if (tv != null) {
      tv.setPadding(0, 10, 0, 10);
      tv.setText(message);
      tv.setMaxLines(10);
      FontHelper.setupTextView(tv, FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);
      FontHelper.setSpannableTextWithFont(tv, tv.getText().toString(), FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);
    }
    TextView actionView =
        snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
    if (actionView != null) {
      actionView.setPadding(0, 10, 0, 10);
      actionView.setText(action);
      FontHelper.setupTextView(actionView, FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);
      FontHelper.setSpannableTextWithFont(actionView, actionView.getText().toString(),
          FontType.NEWSHUNT_REGULAR, FontWeight.NORMAL);
    }
    snackbar.show();
  }

  public static void openAppSettingActivity(final Activity context) {
    if (context == null) {
      return;
    }
    Intent i = new Intent();
    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    i.addCategory(Intent.CATEGORY_DEFAULT);
    i.setData(Uri.parse("package:" + context.getPackageName()));
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    context.startActivity(i);
  }
  public static String getUsesPermission() {
    Map<String, Boolean> usesPermission = new HashMap<>();
    for (Permission permission : Permission.values()) {
      if(Permission.INVALID == permission){
        continue;
      }
      usesPermission.put(permission.name(),
          PermissionUtils.hasPermission(CommonUtils.getApplication(), permission.getPermission()));
    }
    return JsonUtils.toJson(usesPermission);
  }
}