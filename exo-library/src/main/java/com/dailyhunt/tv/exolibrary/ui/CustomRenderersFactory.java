/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.ui;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Custom Renderer factory to be use {@link CustomMediaCodecVideoRenderer}
 * <p>
 * To be checked while updating Exoplayer library.
 */
public class CustomRenderersFactory extends DefaultRenderersFactory {
  public CustomRenderersFactory(Context context) {
    this(context, EXTENSION_RENDERER_MODE_OFF);
  }

  public CustomRenderersFactory(Context context,
                                @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode) {
    super(context, extensionRendererMode);
  }

  @Override
  protected void buildVideoRenderers(Context context, int extensionRendererMode,
                                     MediaCodecSelector mediaCodecSelector,
                                     @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                     boolean playClearSamplesWithoutKeys, boolean enableDecoderFallback,
                                     Handler eventHandler, VideoRendererEventListener eventListener,
                                     long allowedVideoJoiningTimeMs, ArrayList<Renderer> out) {
    out.add(new CustomMediaCodecVideoRenderer(
        context,
        MediaCodecSelector.DEFAULT,
        allowedVideoJoiningTimeMs,
        drmSessionManager,
        /* playClearSamplesWithoutKeys= */ false,
        eventHandler,
        eventListener,
        MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY));

    if (extensionRendererMode == EXTENSION_RENDERER_MODE_OFF) {
      return;
    }
    int extensionRendererIndex = out.size();
    if (extensionRendererMode == EXTENSION_RENDERER_MODE_PREFER) {
      extensionRendererIndex--;
    }

    try {
      // Full class names used for constructor args so the LINT rule triggers if any of them move.
      // LINT.IfChange
      Class<?> clazz = Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer");
      Constructor<?> constructor =
          clazz.getConstructor(
              boolean.class,
              long.class,
              android.os.Handler.class,
              com.google.android.exoplayer2.video.VideoRendererEventListener.class,
              int.class);
      // LINT.ThenChange(../../../../../../../proguard-rules.txt)
      Renderer renderer =
          (Renderer)
              constructor.newInstance(
                  true,
                  allowedVideoJoiningTimeMs,
                  eventHandler,
                  eventListener,
                  MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY);
      out.add(extensionRendererIndex++, renderer);
      Log.i("CustomRenderersFactory", "Loaded LibvpxVideoRenderer.");
    } catch (ClassNotFoundException e) {
      // Expected if the app was built without the extension.
    } catch (Exception e) {
      // The extension is present, but instantiation failed.
      throw new RuntimeException("Error instantiating VP9 extension", e);
    }
  }
}
