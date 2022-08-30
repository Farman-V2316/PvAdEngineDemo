/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dhutil.helper.common;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dhutil.R;
import com.newshunt.permissionhelper.Callbacks.PermissionRationaleProvider;
import com.newshunt.permissionhelper.entities.PermissionRationale;
import com.newshunt.permissionhelper.utilities.PermissionGroup;

/**
 * Class to be used for default texts for permission rationale
 * Created by bedprakash.rout on 8/12/2016.
 */
public class DefaultRationaleProvider implements PermissionRationaleProvider {

  private String locationSubtitle = CommonUtils.getString(com.newshunt.common.util.R.string.permission_location_access);
  private String storageSubtitle = CommonUtils.getString(com.newshunt.common.util.R.string.permission_storage_access);
  private String locationRationale = CommonUtils.getString(com.newshunt.common.util.R.string.permission_location_rationale);
  private String storageRationale = CommonUtils.getString(com.newshunt.common.util.R.string.permission_storage_rationale);
  private String permissionTitle = CommonUtils.getString(com.newshunt.common.util.R.string.permission_title);
  private String permissionDesc = CommonUtils.getString(com.newshunt.common.util.R.string.permission_desc);
  private String permissionSettings = CommonUtils.getString(com.newshunt.common.util.R.string.permission_settings);
  private String permissionSettingsAction = CommonUtils.getString(com.newshunt.common.util.R.string.action_settings);
  private String contactsSubTitle = CommonUtils.getString(com.newshunt.common.util.R.string.contacts_perm_subtitle);
  private String contactsRationale = CommonUtils.getString(com.newshunt.common.util.R.string.contacts_perm_rationale);
  private String cameraSubTitle = CommonUtils.getString(com.newshunt.common.util.R.string.cp_camera_permission_title);
  private String cameraRationale = CommonUtils.getString(com.newshunt.common.util.R.string.cp_permission_storage_rationale);
  private String positiveBtn = CommonUtils.getString(com.newshunt.common.util.R.string.permission_btn_allow);
  private String negativeBtn = CommonUtils.getString(com.newshunt.common.util.R.string.permission_btn_later);

  /**
   * {@inheritDoc}
   */
  @Override
  public PermissionRationale getRationaleString(PermissionGroup permissionGroup) {

    PermissionRationale permissionRationale = null;
    switch (permissionGroup) {

      case LOCATION:
        permissionRationale =
            new PermissionRationale(locationSubtitle, locationRationale, 0);
        break;
      case STORAGE:
        permissionRationale = new PermissionRationale(storageSubtitle, storageRationale, 0);
        break;
      case CONTACTS:
        permissionRationale = new PermissionRationale(contactsSubTitle, contactsRationale, 0);
        break;
      case CAMERA:
        permissionRationale = new PermissionRationale(cameraSubTitle, cameraRationale, 0);
        break;
    }
    return permissionRationale;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRationaleDesc() {
    return permissionDesc;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getOpenSettingsMessage() {
    return permissionSettings;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRationaleTitle() {
    return permissionTitle;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getOpenSettingsAction() {
    return permissionSettingsAction;
  }

  @Override
  public String getPositiveBtn() {
    return positiveBtn;
  }

  @Override
  public String getNegativeBtn() {
    return negativeBtn;
  }

  public void setPermissionTitle(String value) {
    this.permissionTitle = value;
  }

  public void setPermissionDesc(String value) {
    this.permissionDesc = value;
  }

  public void setOpenSettings(String value) {
    this.permissionSettings = value;
  }

  public void setSettingsAction(String value) {
    this.permissionSettingsAction = value;
  }

  public void setLocationSubtitle(String value) {
    this.locationSubtitle = value;
  }

  public void setStorageSubtitle(String value) {
    this.storageSubtitle = value;
  }

  public void setLocationDesc(String value) {
    this.locationRationale = value;
  }

  public void setStorageDesc(String value) {
    this.storageRationale = value;
  }

  public void setPositiveBtn(String positiveBtn) {
    this.positiveBtn = positiveBtn;
  }

  public void setNegativeBtn(String negativeBtn) {
    this.negativeBtn = negativeBtn;
  }
}
