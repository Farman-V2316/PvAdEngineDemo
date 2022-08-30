/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dailyhunt.tv.exolibrary.entities;

import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.util.Util;
import com.newshunt.dataentity.common.asset.ItemCacheType;

/** Representation of a media item. */
public final class MediaItem extends BaseMediaItem {

  public  Boolean isLocalFile;
  //public  boolean isRawFile;
  public  boolean isVideoPlayLocally;
  private boolean play;
  private MediaSource mediaSource;
  private boolean isLive;
  private ItemCacheType cacheType;
  private float prefetchCachePercentage;
  private float streamCachedPercentage;
  private float prefetchDuration;
  private float streamCacheDuration;
  private int contentDuration;
  private String streamCachedUrl = null;
  private Boolean forceVariant = false;
  private int itemIndex = -1;
  private boolean currentlyPlaying = false;
  private boolean isNlfcItem = false;
  private String title = null;

  private String url;

  //Field added for Logging
  public Long byteDownloaded;
  public String selectedConnectionQuality;
  public String networkType;

//  1. Content Id
//2. Network type
//3. Network Quality
//4. Quality selected
//5. Stream URL
//6. %age downloaded
//7. Bytes downloaded
//8. CurrentTimestamp



  public MediaItem(Uri uri) {
    init(uri, null, false, false, false);
  }

  public MediaItem(Uri uri, String contentId, boolean play) {
    init(uri, contentId, play, false, false);
  }

  public MediaItem(Uri uri, String contentId, boolean play,
                   boolean isLocalFile, boolean isLive) {
    init(uri, contentId, play, isLocalFile, isLive);
  }

  public void init(Uri uri, String contentId, boolean play,
              boolean isLocalFile, boolean isLive) {
    this.uri = uri;
    if (uri != null) {
      this.url = uri.toString();
    }
    this.contentId = contentId;
    this.play = play;
    this.isLocalFile = isLocalFile;
    this.isLive = isLive;
  }

  public String getContentId() {
    return contentId;
  }

  public boolean isPlay() {
    return play;
  }

  public boolean isLive() {
    return isLive;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    MediaItem other = (MediaItem) obj;
    return uri.equals(other.uri)
        && Util.areEqual(contentId, other.contentId);
  }

  public MediaSource getMediaSource() {
    return mediaSource;
  }

  public void setMediaSource(MediaSource mediaSource) {
    this.mediaSource = mediaSource;
  }

  public String getStreamCachedUrl() {
    return streamCachedUrl;
  }

  public void setCacheType(ItemCacheType cacheType, float prefetchCachePercentage) {
    this.cacheType = cacheType;
    this.prefetchCachePercentage = prefetchCachePercentage;
  }

  public void setStreamCachedUrl(String streamCachedUrl, float streamCachedPercentage,
                                 int variantIndex,
                                 boolean forceVariant) {
    this.streamCachedUrl = streamCachedUrl;
    this.streamCachedPercentage = streamCachedPercentage;
    this.variantIndex = variantIndex;
    this.forceVariant = forceVariant;
  }

  public int getVariantIndex() {
    return variantIndex;
  }

  public void setVariantIndex(int variantIndex) {
    this.variantIndex = variantIndex;
  }

  public Boolean getForceVariant() {
    return forceVariant;
  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + (contentId == null ? 0 : contentId.hashCode());
    return result;
  }

  public int getItemIndex() {
    return itemIndex;
  }

  public void setItemIndex(int itemIndex) {
    this.itemIndex = itemIndex;
  }

  public float getStreamCachedPercentage() {
    return streamCachedPercentage;
  }

  public void setStreamCachedPercentage(float streamCachedPercentage) {
    this.streamCachedPercentage = streamCachedPercentage;
  }

  public ItemCacheType getCacheType() {
    return cacheType;
  }

  public float getPrefetchCachePercentage() {
    return prefetchCachePercentage;
  }

  public boolean isCurrentlyPlaying() {
    return currentlyPlaying;
  }

  public void setCurrentlyPlaying(boolean currentlyPlaying) {
    this.currentlyPlaying = currentlyPlaying;
  }

  public boolean isNlfcItem() {
    return isNlfcItem;
  }

  public void setNlfcItem(boolean nlfcItem) {
    isNlfcItem = nlfcItem;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public float getPrefetchDuration() {
    return prefetchDuration;
  }

  public void setPrefetchDuration(float prefetchDuration) {
    this.prefetchDuration = prefetchDuration;
  }

  public float getStreamCacheDuration() {
    return streamCacheDuration;
  }

  public void setStreamCacheDuration(float streamCacheDuration) {
    this.streamCacheDuration = streamCacheDuration;
  }

  public int getContentDuration() {
    return contentDuration;
  }

  public void setContentDuration(int contentDuration) {
    this.contentDuration = contentDuration;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
