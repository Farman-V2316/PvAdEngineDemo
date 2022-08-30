/*
 * Copyright (c) 2020 . All rights reserved.
 */
package com.newshunt.news.model.sqlite.legacy;

import com.newshunt.dataentity.model.entity.OfflineArticle;

import java.util.List;

/**
 * @author satosh.dhanyamraju
 */
@Deprecated
public interface OfflineArticleDao {
  void open();

  void close();

  List<OfflineArticle> getOfflineArticles(int maxCount);

  void clearAll();
}