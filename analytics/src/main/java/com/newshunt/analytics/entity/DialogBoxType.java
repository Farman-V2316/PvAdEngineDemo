package com.newshunt.analytics.entity;

/**
 * Created by DattaV on 29-08-2016.
 */
public enum DialogBoxType {
  RATEUS("rate"),
  REPORT_STORY("report_story"),
  REPORT_SPAM_COMMENT("report_spam_comment"),
  AUTOSTART_NOTIFICATIONS("autostart_notifications"),
  ASTRO_ONBOARDING_PROMPT("astro_onboarding_prompt"),
  ASTRO_ONBOARDING_FORM("astro_onboarding_form"),
  FI_OPTIN("fi_optin"),
  FI_OPTOUT("fi_optout"),
  CARD_MENU("card_menu"),
  MPERMISSION_DH_LOCATION("permission_dh_location"),
  MPERMISSION_ANDROID_LOCATION("permission_android_location"),
  MPERMISSION_DH_STORAGE_IMAGE("permission_dh_storage_cache"),
  MPERMISSION_ANDROID_STORAGE_IMAGE("permission_android_storage_cache"),
  MPERMISSION_DH_CAMERA("permission_dh_camera"),
  MPERMISSION_ANDROID_CAMERA("permission_android_camera"),
  MPERMISSION_DH_CONTACTS("permission_dh_contacts"),
  MPERMISSION_ANDROID_CONTACTS("permission_android_contacts"),
  PRIVACY("permission_dh_privacy"),
  UNQUALIFIED_FEEDBACK("unqualified_feedback"),
  QUALIFIED_FEEDBACK("qualified_feedback"),
  GROUP_SETTINGS("group_settings"),
  GROUP_INVITE("group_invite"),
  GROUP_SHARE("group_share"),
  LEAVE_GROUP("leave_group"),
  CREATE_GROUP("create_group"),
  REPORT_GROUP("report_group"),
  MAKE_ADMIN("make_admin"),
  REMOVE_ADMIN("remove_admin"),
  REMOVE_USER("remove_user"),
  REPORT_USER("report_user"),
  MPERMISSION_ANDROID_MICROPHONE("permission_dh_record_audio"),
  MPERMISSION_ANDROID_CALENDAR("permission_dh_read_calendar"),
  DELETE_COMMENT("delete_comment"),
  PUSH_NOTIFICATION_CONFIRMATION("push_notification_disable_confirmation"),
  PERMISSION_APPS_ON_DEVICES("permission_apps_on_devices"),
  IMPLICIT_FOLLOW_PROMPT("follow_prompt"),
  IMPLICIT_BLOCK_PROMPT("block_prompt");

  private final String type;

  DialogBoxType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
