/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.listener;

import com.newshunt.common.helper.common.ImageSaveFailureReason;

/**
 * call back for image saving
 *
 * @author santhosh.kc
 */
public interface ImageSaveCallBack {
  /**
   * callback on image save success
   */
  void onImageSaveSuccess();

  /**
   * callback on image save failure
   */
  void onImageSaveFailure(ImageSaveFailureReason reason);
}
