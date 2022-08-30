/*
 * Copyright (c) 2020 . All rights reserved.
 */

package com.newshunt.news.model.service.legacy;

import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.common.model.entity.NewsArticleState;
import com.newshunt.dataentity.model.entity.OfflineArticle;

import java.util.Iterator;
import java.util.List;

import androidx.collection.SimpleArrayMap;

/**
 * @author heena.arora
 */
@Deprecated
public class OfflineStoriesProvider {
  private static OfflineStoriesProvider offlineStoriesProvider;
  private SimpleArrayMap<String, NewsArticleState> stateArrayMap;

  private OfflineServiceImpl offlineService;
  public static final String TAG = "OfflineStoriesProvider";

  private OfflineStoriesProvider() {
    offlineService = new OfflineServiceImpl(CommonUtils.getApplication());
    stateArrayMap = new SimpleArrayMap<>();
    getSavedArticles();// to initialize map
    Logger.d(TAG, TAG + " created. Map=" + stateArrayMap.size());

  }

  public static OfflineStoriesProvider getOfflineStoriesProvider() {
    if (offlineStoriesProvider == null) {
      synchronized (OfflineStoriesProvider.class) {
        if (offlineStoriesProvider == null) {
          offlineStoriesProvider = new OfflineStoriesProvider();
        }
      }
    }
    return offlineStoriesProvider;
  }

  public List<OfflineArticle> getSavedArticles() {
    List<OfflineArticle> offlineArticles = offlineService.getSavedArticles();
    if (offlineArticles == null) {
      return null;
    }
    Iterator<OfflineArticle> iterator = offlineArticles.iterator();
    while (iterator.hasNext()) {
      OfflineArticle offlineArticle = iterator.next();
      stateArrayMap.put(offlineArticle.getId(), NewsArticleState.COMPLETED);

    }
    return offlineArticles;
  }

  public void clearAll() {
    stateArrayMap.clear();
    offlineService.clearAll();
  }
}
