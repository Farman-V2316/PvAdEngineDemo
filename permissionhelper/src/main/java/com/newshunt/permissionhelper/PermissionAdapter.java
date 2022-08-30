/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.permissionhelper;

import android.app.Activity;
import android.app.Dialog;
import androidx.annotation.NonNull;

import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.permissionhelper.Callbacks.DialogActionCallback;
import com.newshunt.permissionhelper.Callbacks.PermissionRationaleProvider;
import com.newshunt.permissionhelper.entities.PermissionRationale;
import com.newshunt.permissionhelper.utilities.Permission;
import com.newshunt.permissionhelper.utilities.PermissionDialogBuilder;
import com.newshunt.permissionhelper.utilities.PermissionUtils;

import java.util.List;
import java.util.Map;

/**
 * Adapter for permission helper
 *
 * @author bedprakash.rout on 8/9/2016.
 */

public abstract class PermissionAdapter {

  private static final String LOG_TAG = PermissionAdapter.class.getSimpleName();
  protected final Activity activity;
  private final int requestId;
  private final PermissionRationaleProvider rationaleProvider;

  public PermissionAdapter(int requestId, Activity activity,
                           PermissionRationaleProvider rationaleProvider) {
    this.activity = activity;
    this.requestId = requestId;
    this.rationaleProvider = rationaleProvider;
  }

  public Activity getActivity() {
    return activity;
  }

  public int getRequestId() {
    return requestId;
  }

  public abstract List<Permission> getPermissions();

  public abstract void onPermissionResult(@NonNull List<Permission> grantedPermissions,
                                          @NonNull List<Permission> deniedPermissions,
                                          @NonNull List<Permission> blockedPermissions);

  public PermissionRationale getRationaleString(Permission permission) {
    return rationaleProvider.getRationaleString(permission.getPermissionGroup());
  }

  public String getRationaleDesc() {
    return rationaleProvider.getRationaleDesc();
  }

  public String getRationaleTitle() {
    return rationaleProvider.getRationaleTitle();
  }

  public String getOpenSettingsMessage() {
    return rationaleProvider.getOpenSettingsMessage();
  }

  public String getOpenSettingsAction() {
    return rationaleProvider.getOpenSettingsAction();
  }

  public boolean shouldShowRationale() {
    return false;
  }

  public Map<NhAnalyticsEventParam, Object> getExtraParamsMap() {
    return null;
  }

  public Dialog showRationale(List<Permission> permissions, DialogActionCallback callback,
                              PageReferrer referrer) {
    if (rationaleProvider == null) {
      return null;
    }

    return PermissionDialogBuilder.showAppDialogForPermissions(activity, this, rationaleProvider,
        permissions, callback, referrer);
  }

  public void showAppSettingsSnackbar(final String message, final String action) {
    PermissionUtils.showAppSettingsSnackbar(getActivity(), message, action);
  }
}
