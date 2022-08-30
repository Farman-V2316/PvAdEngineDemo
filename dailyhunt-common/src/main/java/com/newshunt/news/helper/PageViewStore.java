/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.news.helper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.newshunt.common.helper.common.Constants;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AppStatePreference;

import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Stores all stories are read
 *
 * @author satosh.dhanymaraju
 */
public class PageViewStore {
  private static final String LOG_TAG = "PageViewStore";
  private static final Gson gson = new Gson();
  private static final int MAX_SIZE = 500;
  private static LruCache<String, MapVal> store = null;
  private static final MutableLiveData<Set<String>> liveData = new MutableLiveData<Set<String>>();

  public static void init() {
    if (store != null) {
      return;
    }
    final String storeStr =
        PreferenceManager.getPreference(AppStatePreference.PAGE_VIEW_STORE, Constants.EMPTY_STRING);
    if (storeStr.isEmpty()) {
      Logger.d(LOG_TAG, "init: empty prefs. creating new");
      newStore();
    } else {
      try {
        store = gson.fromJson(storeStr, new TypeToken<LruCache<String, MapVal>>() {
        }.getType());
        store.get(Constants.EMPTY_STRING); // Fail fast and assign new store.
        Logger.d(LOG_TAG, "init: read from prefs. size= " + store.size());
        if (store == null) {
          Logger.e(LOG_TAG, "init: json parsing returned null. Initializing");
          newStore();
        }
      } catch (Exception e) { // maybe JsonSyntaxException; but catch all.
        Logger.e(LOG_TAG, "init: couldn't deserialize from prefs", e);
        newStore();
        PreferenceManager.remove(AppStatePreference.PAGE_VIEW_STORE);
      }
    }
  }

  public static LiveData<Set<String>> state() {
    return liveData;
  }

  public static void markRead(@NonNull String storyId, @Nullable String groupType) {
    Logger.d(LOG_TAG,
        "markRead() called with: storyId = [" + storyId + "], groupType = [" + groupType + "]");
    store.put(key(storyId, groupType), new MapVal(true));
    sendCurrentState();
  }

  public static boolean isRead(@NonNull String id, @Nullable String groupType) {
    try {
      //TODO::Its crashing as store object is Null
      MapVal val = store.get(key(id, groupType));
      return val != null && val.read;
    } catch (Exception e) {
      return false;
    }
  }

  public static Disposable save() {
    // can be non-blocking operation
    return Completable.fromCallable(() -> {
      String s = gson.toJson(store);
      PreferenceManager.savePreference(AppStatePreference.PAGE_VIEW_STORE, s);
      Logger.d(LOG_TAG, "save: " + s);
      return 1;
    }).subscribeOn(Schedulers.io()).subscribe();
  }

  @NonNull
  private static String key(@NonNull String storyId, @Nullable String groupType) {
    return storyId + Constants.HASH_CHARACTER + groupType;
  }

  private static void newStore() {
    store = new LruCache<>(MAX_SIZE);
  }

  public static void sendCurrentState() {
    liveData.postValue(store.snapshot().keySet());
  }

  /* Append-only. Will be persisted. */
  private static class MapVal {
    final boolean read;

    MapVal(boolean read) {
      this.read = read;
    }
  }

  public static void clear() {
    if (store != null) {
      store.evictAll();
      PreferenceManager.remove(AppStatePreference.PAGE_VIEW_STORE);
    }
  }
}