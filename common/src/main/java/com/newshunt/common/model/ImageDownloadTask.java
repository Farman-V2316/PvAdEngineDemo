/*
 * Copyright (c) 2015 Newshunt. All rights reserved.
 */
package com.newshunt.common.model;

import com.newshunt.common.helper.common.ImageDownloadManager;

import java.lang.ref.WeakReference;
import java.util.Map;

/**
 * This class holds info for ImageDownload
 *
 * @author: bedprakash on 13/1/17.
 */

public class ImageDownloadTask {
  private final Object tag;
  private final ImageDownloadManager.Task task;
  private final Map<String, String> urls;
  private final String folderPath;
  private final WeakReference<ImageDownloadManager.Callback> callback;
  private boolean isAllMandatory = false;

  private ImageDownloadTask(Object tag, ImageDownloadManager.Task task, Map<String, String> urls,
                            String folderPath, ImageDownloadManager.Callback callback,
                            boolean isAllMandatory) {
    this.tag = tag;
    this.task = task;
    this.urls = urls;
    this.folderPath = folderPath;
    this.callback = new WeakReference<>(callback);
    this.isAllMandatory = isAllMandatory;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ImageDownloadTask) {
      return this.tag.equals(((ImageDownloadTask) o).tag);
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }

  public Object getTag() {
    return tag;
  }

  public ImageDownloadManager.Task getTask() {
    return task;
  }

  public Map<String, String> getUrls() {
    return urls;
  }

  public String getFolderPath() {
    return folderPath;
  }

  public WeakReference<ImageDownloadManager.Callback> getCallback() {
    return callback;
  }

  public boolean isAllMandatory() {
    return isAllMandatory;
  }

  public static class Builder {
    private Object tag;
    private ImageDownloadManager.Task task;
    private Map<String, String> urls;
    private String folderPath;
    private ImageDownloadManager.Callback callback;
    private boolean isAllMandatory;

    public Builder setTag(Object tag) {
      this.tag = tag;
      return this;
    }

    public Builder setTask(ImageDownloadManager.Task task) {
      this.task = task;
      return this;
    }

    public Builder setUrls(Map<String, String> urls) {
      this.urls = urls;
      return this;
    }

    public Builder setFolderPath(String folderPath) {
      this.folderPath = folderPath;
      return this;
    }

    public Builder setCallback(ImageDownloadManager.Callback callback) {
      this.callback = callback;
      return this;
    }

    public Builder setAllMandatory(boolean isMandatory) {
      this.isAllMandatory = isMandatory;
      return this;
    }

    public ImageDownloadTask createImageDownloadTask() {
      return new ImageDownloadTask(tag, task, urls, folderPath, callback, isAllMandatory);
    }
  }
}
