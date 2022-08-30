/*
 * Copyright (c) 2019 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.ima.exo;

import android.net.Uri;

import com.dailyhunt.tv.exolibrary.util.MediaSourceUtil;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.offline.StreamKey;
import java.util.List;
/**
 * Factory to define and create media sources supported by ads.
 *
 * @author raunak.yadav
 */
public class AdsMediaSourceFactory implements MediaSourceFactory {

  @Override
  public MediaSourceFactory setStreamKeys(List<StreamKey> streamKeys) {
    return null;
  }

  @Override
  public MediaSourceFactory setDrmSessionManager(DrmSessionManager<?> drmSessionManager) {
    return null;
  }

  @Override
  public MediaSource createMediaSource(Uri uri) {
    return MediaSourceUtil.getMappedSource(CommonUtils.getApplication(), uri, false);
  }

  @Override
  public int[] getSupportedTypes() {
    // IMA does not support Smooth Streaming ads.
    return new int[]{C.TYPE_DASH, C.TYPE_HLS, C.TYPE_OTHER};
  }
}
