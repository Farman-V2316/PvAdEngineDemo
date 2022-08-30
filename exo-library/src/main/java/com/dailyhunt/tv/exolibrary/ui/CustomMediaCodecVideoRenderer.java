/*
 * Copyright (c) 2018 Newshunt. All rights reserved.
 */
package com.dailyhunt.tv.exolibrary.ui;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;


/**
 * Custom video renderer to avoid https://github.com/google/ExoPlayer/issues/5370.
 * Internal bug : https://bugzilla.newshunt.com/eterno/show_bug.cgi?id=24568
 * <p>
 * To be checked and modified while updating Exoplayer library.
 */
public class CustomMediaCodecVideoRenderer extends MediaCodecVideoRenderer {

  private static boolean evaluatedDeviceNeedsSetOutputSurfaceWorkaround;
  private static boolean deviceNeedsSetOutputSurfaceWorkaround;

  public CustomMediaCodecVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector,
                                       long allowedJoiningTimeMs,
                                       @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                       boolean playClearSamplesWithoutKeys,
                                       @Nullable Handler eventHandler,
                                       @Nullable VideoRendererEventListener eventListener, int
                                           maxDroppedFramesToNotify) {
    super(context, mediaCodecSelector, allowedJoiningTimeMs, drmSessionManager,
        playClearSamplesWithoutKeys, eventHandler, eventListener, maxDroppedFramesToNotify);
  }

  /**
   * This code is taken from Exoplayer v2.9.4 to have latest list of devices that have this bug.
   *
   * Overriding this method to return false i.e. DummySurfaceView will be used for all devices
   * while switching surfaces. This is required to get smooth transition when a view is
   * attached/detached.
   * <p>
   * Check super class method to see the list of devices that have issues like ANRs, native crashes
   * when calling setOutputSurface method and hence need to return true from this method.
   */
  @Override
  protected boolean codecNeedsSetOutputSurfaceWorkaround(String name) {
    if (name.startsWith("OMX.google")) {
      // Google OMX decoders are not known to have this issue on any API level.
      return false;
    }
    synchronized (MediaCodecVideoRenderer.class) {
      if (!evaluatedDeviceNeedsSetOutputSurfaceWorkaround) {
        if (Util.SDK_INT <= 27 && "dangal".equals(Util.DEVICE)) {
          // Dangal is affected on API level 27: https://github.com/google/ExoPlayer/issues/5169.
          deviceNeedsSetOutputSurfaceWorkaround = true;
        } else if (Util.SDK_INT >= 27) {
          // In general, devices running API level 27 or later should be unaffected. Do nothing.
        } else {
          // Enable the workaround on a per-device basis. Works around:
          // https://github.com/google/ExoPlayer/issues/3236,
          // https://github.com/google/ExoPlayer/issues/3355,
          // https://github.com/google/ExoPlayer/issues/3439,
          // https://github.com/google/ExoPlayer/issues/3724,
          // https://github.com/google/ExoPlayer/issues/3835,
          // https://github.com/google/ExoPlayer/issues/4006,
          // https://github.com/google/ExoPlayer/issues/4084,
          // https://github.com/google/ExoPlayer/issues/4104,
          // https://github.com/google/ExoPlayer/issues/4134,
          // https://github.com/google/ExoPlayer/issues/4315,
          // https://github.com/google/ExoPlayer/issues/4419,
          // https://github.com/google/ExoPlayer/issues/4460,
          // https://github.com/google/ExoPlayer/issues/4468,
          // https://github.com/google/ExoPlayer/issues/5312.
          switch (Util.DEVICE) {
            case "1601":
            case "1713":
            case "1714":
            case "A10-70F":
            case "A1601":
            case "A2016a40":
            case "A7000-a":
            case "A7000plus":
            case "A7010a48":
            case "A7020a48":
            case "AquaPowerM":
            case "ASUS_X00AD_2":
            case "Aura_Note_2":
            case "BLACK-1X":
            case "BRAVIA_ATV2":
            case "BRAVIA_ATV3_4K":
            case "C1":
            case "ComioS1":
            case "CP8676_I02":
            case "CPH1609":
            case "CPY83_I00":
            case "cv1":
            case "cv3":
            case "deb":
            case "E5643":
            case "ELUGA_A3_Pro":
            case "ELUGA_Note":
            case "ELUGA_Prim":
            case "ELUGA_Ray_X":
            case "EverStar_S":
            case "F3111":
            case "F3113":
            case "F3116":
            case "F3211":
            case "F3213":
            case "F3215":
            case "F3311":
            case "flo":
            case "fugu":
            case "GiONEE_CBL7513":
            case "GiONEE_GBL7319":
            case "GIONEE_GBL7360":
            case "GIONEE_SWW1609":
            case "GIONEE_SWW1627":
            case "GIONEE_SWW1631":
            case "GIONEE_WBL5708":
            case "GIONEE_WBL7365":
            case "GIONEE_WBL7519":
            case "griffin":
            case "htc_e56ml_dtul":
            case "hwALE-H":
            case "HWBLN-H":
            case "HWCAM-H":
            case "HWVNS-H":
            case "HWWAS-H":
            case "i9031":
            case "iball8735_9806":
            case "Infinix-X572":
            case "iris60":
            case "itel_S41":
            case "j2xlteins":
            case "JGZ":
            case "K50a40":
            case "kate":
            case "le_x6":
            case "LS-5017":
            case "M5c":
            case "manning":
            case "marino_f":
            case "MEIZU_M5":
            case "mh":
            case "mido":
            case "MX6":
            case "namath":
            case "nicklaus_f":
            case "NX541J":
            case "NX573J":
            case "OnePlus5T":
            case "p212":
            case "P681":
            case "P85":
            case "panell_d":
            case "panell_dl":
            case "panell_ds":
            case "panell_dt":
            case "PB2-670M":
            case "PGN528":
            case "PGN610":
            case "PGN611":
            case "Phantom6":
            case "Pixi4-7_3G":
            case "Pixi5-10_4G":
            case "PLE":
            case "PRO7S":
            case "Q350":
            case "Q4260":
            case "Q427":
            case "Q4310":
            case "Q5":
            case "QM16XE_U":
            case "QX1":
            case "santoni":
            case "Slate_Pro":
            case "SVP-DTV15":
            case "s905x018":
            case "taido_row":
            case "TB3-730F":
            case "TB3-730X":
            case "TB3-850F":
            case "TB3-850M":
            case "tcl_eu":
            case "V1":
            case "V23GB":
            case "V5":
            case "vernee_M5":
            case "watson":
            case "whyred":
            case "woods_f":
            case "woods_fn":
            case "X3_HK":
            case "XE2X":
            case "XT1663":
            case "Z12_PRO":
            case "Z80":
              deviceNeedsSetOutputSurfaceWorkaround = true;
              break;
            default:
              // Do nothing.
              break;
          }
          switch (Util.MODEL) {
            case "AFTA":
            case "AFTN":
              deviceNeedsSetOutputSurfaceWorkaround = true;
              break;
            default:
              // Do nothing.
              break;
          }
        }
        evaluatedDeviceNeedsSetOutputSurfaceWorkaround = true;
      }
    }
    return deviceNeedsSetOutputSurfaceWorkaround;
  }
}
