/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.model.ImageDownloadTask;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Helper to download Images. This takes set of Urls to download and a folder oath to cave the
 * files. Calls  onSuccess only if all images are downloaded. Else calls
 * onFailure()
 *
 * @author: bedprakash on 11/1/17.
 */

public class ImageDownloadManager {

  private static final String TAG = "ImageDownloadManager";

  public enum Task {
    DOWNLOAD, DELETE
  }

  public interface Callback {
    void onSuccess(ImageDownloadTask task);

    void onFailure(ImageDownloadTask task, ImageSaveFailureReason reason);
  }

  private static final String LOG_TAG = ImageDownloadManager.class.getSimpleName();
  private static final Object LOCK = new Object();
  private static ImageDownloadManager sInstance;

  private Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
  private ThreadPoolExecutor threadPoolExecutor;
  private HashMap<ImageDownloadTask, Callback> callbacks = new HashMap<>();

  private ImageDownloadManager() {

    if (sInstance != null) {
      throw new RuntimeException(
          "Use getInstance() method to get the single instance of this class.");
    }

    int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    threadPoolExecutor = new ThreadPoolExecutor(
        NUMBER_OF_CORES * 2,
        NUMBER_OF_CORES * 2,
        60L,
        TimeUnit.SECONDS,
        new LinkedBlockingQueue<>());
  }

  public static ImageDownloadManager getInstance() {
    if (sInstance == null) {
      synchronized (LOCK) {
        if (sInstance == null) {
          sInstance = new ImageDownloadManager();
        }
      }
    }
    return sInstance;
  }

  public void addTask(ImageDownloadTask task) {
    synchronized (LOCK) {
      if (callbacks.containsKey(task)) {
        Logger.e(LOG_TAG, "Have another task to process with same Tag. Rejecting");
        return;
      }

      threadPoolExecutor.execute(new ImageDownloadRunnable(task));
      callbacks.put(task, task.getCallback().get());
    }
  }

  private class ImageDownloadRunnable implements Runnable {

    ImageDownloadTask imageDownloadTask;

    ImageDownloadRunnable(ImageDownloadTask task) {
      this.imageDownloadTask = task;
      if (task == null) {
        throw new InvalidParameterException("Task is null");
      }
    }

    @Override
    public void run() {
      switch (imageDownloadTask.getTask()) {
        case DELETE: {
          FileUtil.deleteFolder(new File(imageDownloadTask.getFolderPath()));
          postSuccess(imageDownloadTask);
          break;
        }
        case DOWNLOAD: {
          downloadImages(imageDownloadTask);
          break;
        }
      }
    }
    @Override
    public boolean equals(Object o) {
      if (o instanceof ImageDownloadRunnable) {
        return imageDownloadTask.equals(((ImageDownloadRunnable) o).imageDownloadTask);
      }
      return super.equals(o);
    }

  }

  public Bitmap startDownload(String url) {
    try {
      return Glide.with(CommonUtils.getApplication()).asBitmap().load(Uri.parse(url)).submit().get();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private void downloadImages(final ImageDownloadTask task) {
    Set<Map.Entry<String, String>> entries = task.getUrls().entrySet();

    ImageSaveFailureReason reason = null;
    for (Map.Entry<String, String> entry : entries) {
      Bitmap image = startDownload(entry.getKey());
      if (null == image) {
        reason = ImageSaveFailureReason.NETWORK;
        Logger.d(TAG,"Download from url : " + entry.getKey() + " failed");
        if (task.isAllMandatory()) {
          Logger.d(TAG,"since download of all is not mandatory on failure of one url, stopping " +
              "the task");
          break;
        } else {
          continue;
        }
      }
      String filePath = FileUtil.saveBitmapImage(image, task.getFolderPath(), entry
          .getKey());
      task.getUrls().put(entry.getKey(), filePath);
      if (null == filePath) {
        Logger.d(TAG,"file save for url : " + entry.getKey() + " failed");
        reason = ImageSaveFailureReason.FILE;
        if (task.isAllMandatory()) {
          Logger.d(TAG,"since download of all is not mandatory on failure of one url, stopping " +
              "the task");
          break;
        } else {
          continue;
        }
      }
    }
    if (reason == null) {
      postSuccess(task);
    } else {
      postFailure(task, reason);
    }
  }

  private void postSuccess(final ImageDownloadTask task) {
    final Callback callback = task.getCallback().get();
    if (callback != null) {
      MAIN_HANDLER.post(new Runnable() {
        @Override
        public void run() {
          callback.onSuccess(task);
        }
      });
    }
    callbacks.remove(task);
  }

  private void postFailure(ImageDownloadTask task, final ImageSaveFailureReason error) {
    final Callback callback = task.getCallback().get();
    if (callback != null) {
      MAIN_HANDLER.post(new Runnable() {
        @Override
        public void run() {
          callback.onFailure(task, error);
        }
      });
    }

    callbacks.remove(task);
  }

  public void removeTask(ImageDownloadTask task) {
    threadPoolExecutor.remove(new ImageDownloadRunnable(task));
    synchronized (LOCK) {
      callbacks.remove(task);
    }
  }
}
