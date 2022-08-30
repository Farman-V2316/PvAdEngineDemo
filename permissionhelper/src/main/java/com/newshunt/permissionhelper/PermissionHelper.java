/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.permissionhelper;

import android.app.Activity;
import android.app.Dialog;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.BusProvider;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.permissionhelper.Callbacks.DialogActionCallback;
import com.newshunt.permissionhelper.utilities.Permission;
import com.newshunt.permissionhelper.utilities.PermissionAnalytics;
import com.newshunt.permissionhelper.utilities.PermissionUtils;
import com.newshunt.permissionhelper.utilities.PrefUtils;

import static com.newshunt.permissionhelper.utilities.PermissionUtils.checkForBlockedPermission;


/**
 * Permission Helper class
 *
 * @author bedprakash.rout on 7/29/2016.
 */
public class PermissionHelper implements DialogActionCallback {

  private PermissionAdapter adapter;
  private final ArrayList<Permission> deniedPermissions = new ArrayList<>();
  private final ArrayList<Permission> blockedPermissions = new ArrayList<>();
  private PageReferrer referrer;

  public PermissionHelper() {
  }

  public PermissionHelper(@NonNull PermissionAdapter adapter) {
    this.adapter = adapter;
  }

  public void setReferrer(PageReferrer referrer) {
    this.referrer = referrer;
  }

  public Dialog requestPermissions() {

    List<Permission> permissions = adapter.getPermissions();
    deniedPermissions.clear();
    deniedPermissions.addAll(
        PermissionUtils.checkForDeniedPermissions(adapter.getActivity(), permissions));
    if (deniedPermissions.isEmpty()) {
      publishResult();
      BusProvider.getUIBusInstance().unregister(adapter);
      return null;
    }

    blockedPermissions.clear();
    blockedPermissions.addAll(checkForBlockedPermission(adapter.getActivity(), deniedPermissions));

    if (blockedPermissions.size() == deniedPermissions.size()) {
      adapter.showAppSettingsSnackbar(adapter.getOpenSettingsMessage(),
          adapter.getOpenSettingsAction());
      publishResult();
      BusProvider.getUIBusInstance().unregister(adapter);
    } else {
      if (adapter.shouldShowRationale()) {
        return adapter.showRationale(deniedPermissions, this, referrer);
      } else {
        askPermission(adapter.getActivity(), deniedPermissions, adapter.getRequestId());
      }
    }

    return null;
  }

  private void askPermission(Activity activity, List<Permission> permissions, int requestId) {
    if (permissions == null || permissions.isEmpty()) {
      throw new IllegalArgumentException("permissions array null");
    }

    PermissionAnalytics.logPermissionDialogBoxViewedEvent(permissions, referrer, false, adapter.getExtraParamsMap());
    ActivityCompat.requestPermissions(activity, PermissionUtils.getPermissionStrings(permissions),
        requestId);
  }

  private void publishResult() {
    ArrayList<Permission> granted = new ArrayList<>();
    granted.addAll(adapter.getPermissions());
    granted.removeAll(deniedPermissions);
    granted.removeAll(blockedPermissions);
    deniedPermissions.removeAll(blockedPermissions);
    adapter.onPermissionResult(granted, deniedPermissions, blockedPermissions);
  }

  public void handlePermissionCallback(Activity activity, String[] stringPermissions) {
    PrefUtils.markPermissionsAskedOnce(activity, stringPermissions);

    deniedPermissions.clear();
    deniedPermissions.addAll(
        PermissionUtils.checkForDeniedPermissions(activity, adapter.getPermissions()));

    blockedPermissions.clear();
    blockedPermissions.addAll(checkForBlockedPermission(activity, deniedPermissions));

    PermissionAnalytics.logPermissionDialogBoxActionEvent(adapter.getPermissions(), referrer, false,
        !blockedPermissions.isEmpty(), deniedPermissions.isEmpty() ? Constants.DIALOG_ACCEPT :
            Constants.DIALOG_REJECT, adapter.getExtraParamsMap());

    publishResult();

    if (blockedPermissions.size() > 0 &&
        blockedPermissions.size() == adapter.getPermissions().size()) {
      adapter.showAppSettingsSnackbar(adapter.getOpenSettingsMessage(),
          adapter.getOpenSettingsAction());
      return;
    }
  }

  @Override
  public void onPositiveButtonClick() {
    if (deniedPermissions.size() == blockedPermissions.size()) {
      adapter.showAppSettingsSnackbar(adapter.getOpenSettingsMessage(),
          adapter.getOpenSettingsAction());
    } else {
      askPermission(adapter.getActivity(), deniedPermissions, adapter.getRequestId());
    }
  }

  @Override
  public void onNegativeButtonClick() {
    publishResult();
  }

  public void setAdapter(PermissionAdapter adapter) {
    this.adapter = adapter;
  }
}
