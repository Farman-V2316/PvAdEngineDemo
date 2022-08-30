/**
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.dataentity.news.model.util;

import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

/**
 * @author shrikant.agrawal
 */
public enum NewsPageLayout {

  ARTICLE_LISTING("articles_listing"),
  WEB_ITEMS("web_items"),
  INBOX_ARTICLES_LISTING("inbox_articles_listing"),
  CHRONO_INBOX_ARTICLES_LISTING("chrono_inbox_articles_listing"),
  ENTITY_LISTING("entity_listing"),
  PHOTO_GRID("photo_grid");

  private String layout;

  NewsPageLayout(String layout) {
    this.layout = layout;
  }

  public String getLayout() {
    return layout;
  }

  public static NewsPageLayout fromName(String name) {
    for (NewsPageLayout layoutType : NewsPageLayout.values()) {
      if (CommonUtils.equals(layoutType.layout, name)) {
        return layoutType;
      }
    }
    return null;
  }
}
