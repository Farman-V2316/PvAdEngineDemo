/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */

package com.newshunt.common.helper.cachedapi;

import com.jakewharton.disklrucache.DiskLruCache;
import com.newshunt.common.helper.common.AndroidUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.dataentity.common.helper.common.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author arun.babu
 */
public class CachedApiCache {
  private static final String LOG_TAG = "CachedApiCache";
  private static final String CACHE_DIR = "http_api_cache";
  private static final int APP_VER = 1;
  private static final int VALUE_COUNT = 1; // LruCache entry field count. Only one entry for us
  private static final int VALUE_INDEX = 0; // Only one entry. So taking zeroth index.
  private static final int MAX_SIZE = 1 * 1024 * 1024;

  private static CachedApiCache instance;

  private DiskLruCache diskLruCache;
  private String dirName;

  private CachedApiCache() {
    DiskLruCache temp = null;
    try {
      temp = DiskLruCache.open(getCacheDir(), APP_VER, VALUE_COUNT, MAX_SIZE);
    } catch (Exception e) {
      Logger.caughtException(e);
      temp = null;
    }

    diskLruCache = temp;
  }

  public CachedApiCache(String dirName) {
    diskLruCache = createInstance(dirName);
    this.dirName = dirName;
  }

  private DiskLruCache createInstance(String dirName) {
    DiskLruCache temp = null;
    try {
      temp = DiskLruCache.open(CommonUtils.getCacheDir(dirName), APP_VER, VALUE_COUNT, MAX_SIZE);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
    return temp;
  }

  public static CachedApiCache getInstance() {
    if (null == instance) {
      synchronized (CachedApiCache.class) {
        if (null == instance) {
          instance = new CachedApiCache();
        }
      }
    }

    return instance;
  }

  public void addOrUpdate(String key, byte[] data) {
    if (null == diskLruCache) {
      return;
    }
    Logger.d(LOG_TAG, "addOrUpdate: " + toKb(data.length));
    OutputStream outputStream = null;
    try {
      DiskLruCache.Editor editor = diskLruCache.edit(key);
      outputStream = editor.newOutputStream(VALUE_INDEX);
      outputStream.write(data);
      editor.commit();
      diskLruCache.flush();
    } catch (IOException e) {
      Logger.caughtException(e);
    } finally {
      AndroidUtils.close(outputStream);
      printNumItems();
    }
  }

  private void printNumItems() {
    if (diskLruCache.getDirectory() != null && diskLruCache.getDirectory().listFiles() != null) {
      Logger.d(LOG_TAG, "printNumItems: " + diskLruCache.getDirectory().listFiles().length);
    } else {
      Logger.e(LOG_TAG, "printNumItems: dir is empty");
    }
  }

  public byte[] get(String key) {
    if (null == diskLruCache) {
      return null;
    }

    InputStream inputStream = null;
    try {
      DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
      inputStream = snapshot.getInputStream(VALUE_INDEX);
      byte[] data = new byte[(int) snapshot.getLength(VALUE_INDEX)];
      inputStream.read(data);
      Logger.d(LOG_TAG, "get: " + toKb(data.length));
      return data;
    } catch (IOException e) {
      Logger.caughtException(e);
    } finally {
      AndroidUtils.close(inputStream);
    }

    return null;
  }

  public void remove(String key) {
    if (null == diskLruCache) {
      return;
    }

    try {
      diskLruCache.remove(key);
      diskLruCache.flush();
    } catch (IOException e) {
      Logger.caughtException(e);
    } finally {
      printNumItems();
    }
  }

  public void clear() {
    if (null == diskLruCache) {
      return;
    }

    try {
      diskLruCache.delete();
      diskLruCache = createInstance(dirName);
    } catch (IOException e) {
      Logger.caughtException(e);
    } finally {
      printNumItems();
    }
  }

  /**
   * To close the diskLruCache
   */
  public void close() {
    if (diskLruCache == null || diskLruCache.isClosed()) {
      return;
    }
    try {
      diskLruCache.close();
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }

  public static File getCacheDir() {
    return CommonUtils.getCacheDir(CACHE_DIR);
  }

  public long size() {
    return diskLruCache != null ? diskLruCache.size() : 0;
  }

  public boolean isFull() {
    // Lru is never full. If it was, it will resize itself. 1MB limit is large enough to store
    // more than thousand stories. Keeping this method for future use.
    return false;
  }

  double toKb(long b) {
    return b / 1024.0;
  }
}
