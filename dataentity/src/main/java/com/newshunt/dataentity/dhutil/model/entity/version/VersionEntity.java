/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */

package com.newshunt.dataentity.dhutil.model.entity.version;

/**
 * Represents all available entities and its info
 *
 * @author shanmugam.c
 */
public enum VersionEntity {
  LANGUAGE(null, null),
  EDITION(null, null),
  COMMUNICATION_EVENTS(null, null),
  LIKE_DISLIKE(null, null),
  DISILKE_OPTIONS(null, null),
  VIRAL_LIKE_DISLIKE(null, null),
  APP_SECTIONS(null, null),
  APP_LAUNCH_CONFIG(null, null),
  DNS_CONFIG(null, null),
  CHINESE_DEVICE_INFO(null, null),
  APP_JS(null, null),
  SHARE_TEXT_MAPPING_INFO(null, null),
  PLAYERS_CONFIG(null, null),
  APPSFLYER_EVENT_CONFIG(null, null),
  FOLLOW_SECTIONS(null, null),
  FOLLOW_SYNC(null, null),
  SEARCH_HINT(null, null),
  SEARCH_TRENDING(null, null),
  MULTI_PROCESS_CONFIG(null, null),
  SERVER_OPT_IN(null, null),
  ABOUT_US(null,null),
  NOTIFICATION_CHANNEL(null, null),
  PAGEABLE_TOPICS(null, null),
  PAGE_ENTITY(null, null),
  ENTITY_INFO(null,null),
  INVITE_CONFIG(null, null),
  GROUP_APPROVAL_CONFIG(null, null),
  HANDSHAKE_CONFIG(null, null),
  PARTNER_SERVICE_CONFIG(null, null),
  DETAIL_WIDGET_ORDERING(null, null),
  NOTIFICATION_CTA_CONFIG(null, null),
  LOCATION(null, null),
  LOCATION_DETAIL(LOCATION, null),
  ACTIONABLE_PAYLOAD(null,  null),
  ADJUNCT_LANG(null,null),
  ALL_LOCATION(null,  null),
  NEWS_STICKY_OPTIN(null, null),
  NOTIFICATION_TEMPLATE(null,null);

  private VersionEntity parent;
  private VersionEntity grandParent;

  VersionEntity(VersionEntity parent, VersionEntity grandParent) {
    this.parent = parent;
    this.grandParent = grandParent;
  }

  public static VersionEntity fromName(String entityName) {
    if (entityName == null) {
      return null;
    }

    for (VersionEntity entity : values()) {
      if (entity.name().equalsIgnoreCase(entityName)) {
        return entity;
      }
    }
    return null;
  }

  public VersionEntity getParent() {
    return parent;
  }

  public boolean hasParent() {
    return parent != null;
  }

  public boolean hasGrandParent() {
    return grandParent != null;
  }

  public VersionEntity getGrandParent() {
    return grandParent;
  }
}
