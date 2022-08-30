/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */

package com.newshunt.permissionhelper.utilities;

import com.newshunt.analytics.client.AnalyticsClient;
import com.newshunt.analytics.entity.DialogBoxType;
import com.newshunt.analytics.entity.NhAnalyticsDialogEvent;
import com.newshunt.analytics.entity.NhAnalyticsDialogEventParam;
import com.newshunt.dataentity.analytics.entity.NhAnalyticsEventParam;
import com.newshunt.dataentity.analytics.section.NhAnalyticsEventSection;
import com.newshunt.dataentity.analytics.referrer.PageReferrer;
import com.newshunt.common.helper.common.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * Created by karthik on 10/01/18.
 */
public class PermissionAnalytics {

  public static void logPermissionDialogBoxActionEvent(List<Permission> permissions,
                                                       PageReferrer referrer,
                                                       boolean isRationale,
                                                       boolean isBlocked,
                                                       String action, @Nullable Map<NhAnalyticsEventParam, Object> extraParamsMap) {
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    String type = getPermissionDialogBoxType(permissions, isRationale);
    paramsMap.put(NhAnalyticsDialogEventParam.TYPE, type);
    if (!isRationale) {
      paramsMap.put(NhAnalyticsDialogEventParam.NEVERSHOW, isBlocked);
    }
    if(extraParamsMap != null) {
      paramsMap.putAll(extraParamsMap);
    }
    paramsMap.put(NhAnalyticsDialogEventParam.ACTION, action);
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_ACTION, NhAnalyticsEventSection.APP,
        paramsMap, referrer);
  }

  public static void logPermissionDialogBoxViewedEvent(List<Permission> permissions,
                                                       PageReferrer referrer,
                                                       boolean isRationale,
                                                       @Nullable Map<NhAnalyticsEventParam, Object> extraParamsMap) {
    Map<NhAnalyticsEventParam, Object> paramsMap = new HashMap<>();
    String type = getPermissionDialogBoxType(permissions, isRationale);
    paramsMap.put(NhAnalyticsDialogEventParam.TYPE, type);
    if(extraParamsMap != null) {
      paramsMap.putAll(extraParamsMap);
    }
    AnalyticsClient.log(NhAnalyticsDialogEvent.DIALOGBOX_VIEWED, NhAnalyticsEventSection.APP,
        paramsMap, referrer);
  }

  private static String getPermissionDialogBoxType(List<Permission> permissions,
                                                   boolean isRationale) {
    StringBuilder type = new StringBuilder();

    for (Permission permission : permissions) {
      String permissionType = null;
      switch (permission.getPermissionGroup()) {
        case LOCATION:
          if (isRationale) {
            permissionType = DialogBoxType.MPERMISSION_DH_LOCATION.getType();
          } else {
            permissionType = DialogBoxType.MPERMISSION_ANDROID_LOCATION.getType();
          }
          break;

        case STORAGE:
          if (isRationale) {
            permissionType = DialogBoxType.MPERMISSION_DH_STORAGE_IMAGE.getType();
          } else {
            permissionType = DialogBoxType.MPERMISSION_ANDROID_STORAGE_IMAGE.getType();
          }
          break;
        case CAMERA:
          if (isRationale) {
            permissionType = DialogBoxType.MPERMISSION_DH_CAMERA.getType();
          } else {
            permissionType = DialogBoxType.MPERMISSION_ANDROID_CAMERA.getType();
          }
          break;
        case CONTACTS:
          if(isRationale) {
            permissionType = DialogBoxType.MPERMISSION_DH_CONTACTS.getType();
          }
          else {
            permissionType = DialogBoxType.MPERMISSION_ANDROID_CONTACTS.getType();
          }
          break;
        case CALENDAR:
          permissionType = DialogBoxType.MPERMISSION_ANDROID_CALENDAR.getType();
          break;
        case MICROPHONE:
          permissionType = DialogBoxType.MPERMISSION_ANDROID_MICROPHONE.getType();
          break;
        default:
          throw new IllegalArgumentException(
              "Unknown permission added " + permission.getPermission());
      }

      if (permissionType != null) {
        if (type.length() == 0) {
          type.append(permissionType);
        } else {
          type.append(Constants.COMMA_CHARACTER).append(permissionType);
        }
      }
    }

    return type.toString();
  }

}
