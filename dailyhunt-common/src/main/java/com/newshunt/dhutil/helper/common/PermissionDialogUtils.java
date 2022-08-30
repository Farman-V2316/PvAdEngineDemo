/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dhutil.helper.common;

import android.app.Dialog;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dataentity.common.model.entity.PermissionResult;
import com.newshunt.dhutil.helper.preference.AppStatePreference;
import com.newshunt.permissionhelper.Callbacks.PermissionRationaleProvider;
import com.newshunt.permissionhelper.PermissionAdapter;
import com.newshunt.permissionhelper.PermissionHelper;
import com.newshunt.permissionhelper.utilities.Permission;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

/**
 * Utility to check app launch count and initiate permission request.
 * <p>
 * Created by karthik on 13/12/17.
 */
public class PermissionDialogUtils {

  // Duration after which subsequent requests are made
  public static final int PERMISSION_DIALOG_REPETITION_COUNT = 7;

  private static final int REQUEST_ID = 101;

  public static Dialog promptPermissionOnLaunch(FragmentActivity activity, PageReferrer referrer,
                                                int appLaunchCount, PermissionRationaleProvider
                                                    permissionRationaleProvider) {
    PreferenceManager.savePreference(AppStatePreference.LAST_PERMISSION_DIALOG_COUNT,
        appLaunchCount);
    PermissionHelper permissionHelper = new PermissionHelper();
    permissionHelper.setReferrer(referrer);
    PermissionAdapter adapter =
        new PermissionAdapter(REQUEST_ID, activity, permissionRationaleProvider) {

          @Override
          public List<Permission> getPermissions() {
            return Arrays.asList(Permission.ACCESS_FINE_LOCATION);
          }

          @Override
          public void onPermissionResult(@NonNull List<Permission> grantedPermissions,
                                         @NonNull List<Permission> deniedPermissions,
                                         @NonNull List<Permission> blockedPermissions) {
          }

          @Override
          public boolean shouldShowRationale() {
            return true;
          }

          @Override
          public void showAppSettingsSnackbar(String message, String action) {
            // Do nothing
          }

          @Subscribe
          public void onPermissionResult(PermissionResult permissionResult) {
            permissionHelper.handlePermissionCallback(permissionResult.activity,
                permissionResult.permissions);
            BusProvider.getUIBusInstance().unregister(this);
          }
        };

    BusProvider.getUIBusInstance().register(adapter);
    permissionHelper.setAdapter(adapter);
    return permissionHelper.requestPermissions();
  }

}
