/*
 * Copyright (c) 2022 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.entities;

import android.net.Uri;

import java.io.Serializable;

public class BaseMediaItem implements Serializable {
  /** The media {@link Uri}. */
  public transient Uri uri;

  /** The title of the item, or {@code null} if unspecified. */
  public String contentId;

  public int variantIndex = 0;

  public Uri getUri() {
    return uri;
  }

  public void setUri(Uri uri) {
    this.uri = uri;
  }

  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  public int getVariantIndex() {
    return variantIndex;
  }

  public void setVariantIndex(int variantIndex) {
    this.variantIndex = variantIndex;
  }
}
