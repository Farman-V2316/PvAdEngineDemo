package com.dailyhunt.tv.ima.player.exo;

import com.dailyhunt.tv.ima.entity.state.VideoState;

/**
 * CommonUtils for Exo Player View
 *
 * @author ranjith
 */

public class ExoPlayerViewUtils {

  /**
   * Is Video State Unknown or Error
   *
   * @param videoState -- videoState
   * @return -- true / false
   */
  public static boolean isVideoUnKnownOrError(VideoState videoState) {
    if (videoState == null) {
      return true;
    }

    switch (videoState) {
      case VIDEO_UNKNOWN:
      case VIDEO_ERROR:
        return true;

      case VIDEO_PREPARE_IN_PROGRESS:
      case VIDEO_COMPLETE:
      case VIDEO_PREPARED:
      case VIDEO_QUALITY_CHANGE:
        return false;
      default:
        return false;
    }
  }
}
