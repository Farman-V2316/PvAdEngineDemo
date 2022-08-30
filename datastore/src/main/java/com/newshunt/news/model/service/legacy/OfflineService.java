/*
 * Copyright (c) 2020 . All rights reserved.
 */
package com.newshunt.news.model.service.legacy;

import com.newshunt.dataentity.model.entity.OfflineArticle;

import java.util.List;

/**
 * @author satosh.dhanyamraju
 */
@Deprecated
public interface OfflineService {
  List<OfflineArticle> getSavedArticles();
  void clearAll();
}
