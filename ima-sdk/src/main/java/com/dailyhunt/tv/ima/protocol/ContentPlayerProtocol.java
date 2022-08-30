package com.dailyhunt.tv.ima.protocol;

import android.widget.RelativeLayout;

/***
 * Wrapper for Content Player .
 *
 * @author ranjith
 */

public interface ContentPlayerProtocol {

  /**
   * Method to get the Ad Protocol Instance ..
   *
   * @return AdPlayerProtocol
   */
  AdPlayerProtocol getAdProtocol();

  /**
   * Method to get the Video protocol Instance
   *
   * @return -- Video player Protocol
   */
  VideoPlayerProtocol getVideoProtocol();

  /**
   * Whether we want to show or hide the Intermediate progress
   *
   * @param show -- show/hide
   */
  void showOrHideIntermediateProgress(boolean show);

  /**
   * Set Layout params
   *
   * @param params -- params..
   */
  void setViewParams(RelativeLayout.LayoutParams params);
}
