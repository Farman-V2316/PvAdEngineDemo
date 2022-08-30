/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.common;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.appconfig.AppConfig;

public class DHConstants {

  public static final String PACKAGE_NAME = AppConfig.getInstance().getPackageName();

  public static final String PROFILE_OPEN_ACTION = AppConfig.getInstance().getPackageName() +
      ".openProfile";

  public static final String PROFILE_EDIT_ACTION = AppConfig.getInstance().getPackageName() +
      ".editProfile";

  public static final String SETTINGS_OPEN_ACTION = AppConfig.getInstance().getPackageName() +
      ".openSettingsActivity";

  public static final String OPEN_APP_LANGUAGE = AppConfig.getInstance().getPackageName() +
          ".openAppLanguageActivity";


  public static final String OPEN_FOLLOW_ENTITIES_SCREEN = AppConfig.getInstance().getPackageName()+
    ".openFollowEntitiesScreen";

  public static final String OPEN_LOCAL_SCREEN = AppConfig.getInstance().getPackageName()+
    ".openLocalScreen";
  public static final String OPEN_LOCAL_VIDEO = AppConfig.getInstance().getPackageName()+
      ".openLocalVideo";

  public static final String INTENT_STICKY_AUDIO_COMMENTARY_STATE_CHANGED =
      CommonUtils.getApplication().getPackageName() + ".audioCommentaryStateChanged";
  public static final String INTENT_STICKY_AUDIO_STARTED =
      CommonUtils.getApplication().getPackageName() + ".audioCommentaryStarted";

  public static final String GROUP_DETAIL_OPEN_ACTION =
      AppConfig.getInstance().getPackageName() + ".openGroupDetail";

  public static final String GROUP_CREATE_EDIT_ACTION =
      AppConfig.getInstance().getPackageName() + ".editCreateGroup";

  public static final String GROUP_SETTINGS_ACTION =
      AppConfig.getInstance().getPackageName() + ".groupSetting";

  public static final String GROUP_MEMBERS_ACTION = AppConfig.getInstance().getPackageName() +
      ".groupMembersList";

  public static final String GROUP_INVITATION_ACTION =
      AppConfig.getInstance().getPackageName() + ".groupInvitation";
  public static final String APPROVALS_ACTION = AppConfig.getInstance().getPackageName() +
      ".pendingApprovals";
  public static final String CONTACT_LIST_ACTION = AppConfig.getInstance().getPackageName() +
      ".openContactList";
  public static final String LAUNCH_RUNTIME_PERMISSION_DP =
      AppConfig.getInstance().getPackageName() + ".openRuntimePermissionActivity";

  public static final String INTENT_ACTION_ACCOUNT_LINK =
      AppConfig.getInstance().getPackageName() + ".accountsLink";

  public static final String OPEN_LOCATION_SELECTION =
      AppConfig.getInstance().getPackageName() + ".locationSelection";

  public static final String OPEN_NOTIFICATION_ACTIVITY =
          AppConfig.getInstance().getPackageName() + ".notificationSettingsActivity";
}
