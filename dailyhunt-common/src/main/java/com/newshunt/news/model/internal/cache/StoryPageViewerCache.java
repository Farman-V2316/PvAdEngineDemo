/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.model.internal.cache;

import android.os.Build;
import android.util.LruCache;

import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.DateFormatter;
import com.newshunt.common.helper.common.JsonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.news.helper.StoryPageViewerCacheValue;
import com.newshunt.pref.NewsPreference;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kotlin.Pair;

/**
 * A cache to store the time spent of recently viewed ids
 *
 * @author santhosh.kc
 */
public class StoryPageViewerCache {
  private static final String LOG_TAG = "StoryPageViewerCache";
  private static final int DEFAULT_MAX_SIZE = 5;
  private static final long MINIMUM_TIME_SPENT = 0;// to add the id of story viewed always

  private InternalCache cache;

  private static StoryPageViewerCache sInstance;

  private StoryPageViewerCache() {
    int maxViewedCount = PreferenceManager.getPreference(NewsPreference.STORY_PAGE_MAX_VIEWED_COUNT,
        DEFAULT_MAX_SIZE);
    cache = createFromPreferences(maxViewedCount);
  }

  public static StoryPageViewerCache getInstance() {
    if (sInstance == null) {
      synchronized (StoryPageViewerCache.class) {
        if (sInstance == null) {
          sInstance = new StoryPageViewerCache();
        }
      }
    }
    return sInstance;
  }

  public void put(String key, String groupType, String contentType, long timeSpent) {
    if (CommonUtils.isEmpty(key) || timeSpent < getMinimumTimeSpent()) {
      return;
    }
    StoryPageViewerCacheValue value = new StoryPageViewerCacheValue(key, groupType, contentType);
    cache.put(key, value);
    Logger.d(LOG_TAG, "put: size= "+cache.size());
  }

  public boolean onTimeSpent(Map<String, Object> params) {
    Pair<String, StoryPageViewerCacheValue> keyAndVal =
        StoryPageViewerCacheUtilKt.parseEventParams(params);
    if (keyAndVal == null) {
      Logger.e(LOG_TAG, "failed to parse timespent params");
      return false;
    }
    StoryPageViewerCacheValue storedValue = cache.get(keyAndVal.getFirst());
    if (storedValue == null) {
      Logger.e(LOG_TAG, "got timespent for "+keyAndVal.getFirst()+"; but not in cache");
      return false;
    }
    // calls to cache.put should be synchronized. this method (onTimeSpent) is invoked from multiple
    // threads. So, post it to main thread handler. cache.put also called from put method, which is
    // called only from UI thread (so no need to post through handler.
    AndroidUtils.getMainThreadHandler().post(()-> {
      cache.put(keyAndVal.getFirst(), keyAndVal.getSecond());
    });
    return true;
  }

  @Override
  public String toString() {
    Map<String, StoryPageViewerCacheValue> snapshot = cache.snapshot();

    StringBuilder sb = new StringBuilder();
    //snapshot always gives in reverse access order, i.e. most accessed will be last and least
    // accessed will be at first
    List<String> ids = new ArrayList<>(snapshot.keySet());
    for (int loop = ids.size() - 1; loop >= 0; loop--) {
      String key = ids.get(loop);
      StoryPageViewerCacheValue val = snapshot.get(key);
      if (val != null) {
        sb.append(key)
            .append(Constants.COLLON)
            .append(CommonUtils.firstNonNull(val.getGroupType(), Constants.EMPTY_STRING))
            .append(loop > 0 ? Constants.COMMA_CHARACTER : Constants.EMPTY_STRING);
      }
    }
    return sb.toString();
  }

  public void trim(int resize) {
    if (resize == cache.maxSize()) {
      return;
    }

    cache.resize(resize);
  }

  private void trimForLowerVersions(int resize) {
    if (cache.size() > resize) {
      Map<String, StoryPageViewerCacheValue> snapshot = cache.snapshot();
      for (Map.Entry<String, StoryPageViewerCacheValue> entry : snapshot.entrySet()) {
        if (cache.size() <= resize) {
          break;
        }
        cache.remove(entry.getKey());
      }
    }
    writeToPreferences();
    // Have to recreate the cache because, maxSize of a LruCache is updated only in the
    // constructor, so only removing as above is not sufficient as still maxSize will be old value.
    cache = createFromPreferences(resize);
  }

  private InternalCache createFromPreferences(int maxSize) {
    InternalCache newCache = new InternalCache(maxSize);
    String recentlyViewedString = PreferenceManager.getPreference(NewsPreference
        .STORY_PAGE_IDS_RECENTLY_VIEWED, Constants.EMPTY_STRING);
    Type type = new TypeToken<LinkedHashMap<String, StoryPageViewerCacheValue>>() {

    }.getType();
    Map<String, StoryPageViewerCacheValue> map = JsonUtils.fromJson(recentlyViewedString, type);
    if (CommonUtils.isEmpty(map)) {
      return newCache;
    }

    for (Map.Entry<String, StoryPageViewerCacheValue> stringLongEntry : map.entrySet()) {
      newCache.put(stringLongEntry.getKey(), stringLongEntry.getValue());
    }
    return newCache;
  }

  private long getMinimumTimeSpent() {
    long minTimeSpent =
        PreferenceManager.getPreference(NewsPreference.STORY_PAGE_MIN_TIME_SPENT,
            MINIMUM_TIME_SPENT);
    return minTimeSpent * DateFormatter.SECOND_MILLIS < 0 /* to handle overflow, if server decides to
     never add to cache */ ? Long.MAX_VALUE : (minTimeSpent * DateFormatter.SECOND_MILLIS);
  }

  public void saveState() {
    writeToPreferences();
  }

  @Override
  protected void finalize() {
    writeToPreferences();
  }

  private void writeToPreferences() {
    String jsonString = JsonUtils.toJson(cache.snapshot());
    PreferenceManager.savePreference(NewsPreference.STORY_PAGE_IDS_RECENTLY_VIEWED, jsonString);
  }

  /**
   * evict all entries
   * @param persist - write to preferences, if true
   */
  public void clear(boolean persist) {
    Logger.d(LOG_TAG, "clear: "+persist+"currsize="+cache.size());
    cache.evictAll();
    if (persist) {
      writeToPreferences();
    }
  }

  public Collection<StoryPageViewerCacheValue> entries() {
    return cache.snapshot().values();
  }

  private static class InternalCache extends LruCache<String, StoryPageViewerCacheValue> {

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public InternalCache(int maxSize) {
      super(maxSize);
    }

    @Override
    protected int sizeOf(String key, StoryPageViewerCacheValue value) {
      return 1;
    }
  }
}
