/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.entity.server.asset;

/**
 * Represents the possible list of asset types
 *
 * @author amarjit
 */
public enum AssetType {
  STORY, VIDEO, PHOTO, LINK, ASTROLOGY, ASTROCARD, ALBUM, HTML, TOPIC, SOURCE, TICKER, TICKERNODE,
  PGI_ARTICLE_AD, PLACE_HOLDER, RELATED_STORIES, BANNER,
  STORY_DETAIL_WEB_ERROR/*client-only, for showing UI*/,
  GIF, QUESTION_2_CHOICES, ASSOCIATION, MEME, MEMETEXT, TEXT, COLLECTION, QUESTION_MULTI_CHOICES,
  CAROUSEL_SAVED_ITEMS,
  VHGIF, CHANNEL, SHOW, GROUP, POLL, LANGUAGE_SELECTION_CARD, COLD_START_HEADER_CARD,FOLLOWS,WEB,
  LOCATION, LIST_GROUP_HEADER, ACTIVITY,VIRAL;

  public static AssetType fromName(String name) {
    if (name == null) {
      return null;
    }

    for (AssetType assetType : AssetType.values()) {
      if (assetType.name().equalsIgnoreCase(name)) {
        return assetType;
      }
    }

    return null;
  }
}
