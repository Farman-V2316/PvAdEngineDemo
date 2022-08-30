/*
 * Copyright (c) 2020 . All rights reserved.
 */
package com.newshunt.news.model.service.legacy;

import android.content.Context;

import com.newshunt.dataentity.model.entity.OfflineArticle;
import com.newshunt.news.model.sqlite.legacy.OfflineArticleDao;
import com.newshunt.news.model.sqlite.legacy.OfflineArticleSQLiteDao;

import java.util.List;

/**
 * @author satosh.dhanyamraju
 */
@Deprecated
public class OfflineServiceImpl implements OfflineService{
  private OfflineArticleDao offlineArticleDao;

  public OfflineServiceImpl(Context context){
    offlineArticleDao = new OfflineArticleSQLiteDao(context);
  }

  @Override
  public List<OfflineArticle> getSavedArticles() {
    offlineArticleDao.open();
    List<OfflineArticle> offlineArticles = offlineArticleDao.getOfflineArticles(Integer.MAX_VALUE);
    offlineArticleDao.close();
    return offlineArticles;
  }

  @Override
  public void clearAll() {
    offlineArticleDao.open();
    offlineArticleDao.clearAll();
    offlineArticleDao.close();
  }
}
