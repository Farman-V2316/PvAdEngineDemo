/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper.handler;

import java.util.HashSet;

/**
 * An Helper class for Gallery story page basically used in lite mode.
 * If a photo has been chosen by the user to be viewed (in lite mode), then next time
 * revisiting the photo in a scroll list, then we don't option of asking the user view, instead
 * directly download the photo
 *
 * @author santhosh.kc
 */
public class GalleryPhotoViewStatusHelper {

  private final static HashSet<String> viewAttemptedPhotos = new HashSet<>();

  /**
   * Function to check if user has attempted to view the photo before
   *
   * @param imageUrl - input photo url
   * @return - true if user had attempted to view photo else false
   */
  public static boolean isPhotoAttemptedToBeDownloaded(String imageUrl) {
    return viewAttemptedPhotos.contains(imageUrl);
  }

  /**
   * Function to mention that this photo of given imageUrl is marked as to be viewed by the user
   *
   * @param imageUrl - input photo url
   */
  public static void setPhotoViewAttempted(String imageUrl) {
    viewAttemptedPhotos.add(imageUrl);
  }

  /**
   * Function to remove the photo viewed status from hash set.
   * The idea is whenever photo is downloaded, it means, photo is readily available in cache, no
   * point in giving view option for the user. Also to avoid the set growing, we remove it from set
   *
   * @param imageUrl - input photo url
   */
  public static void onPhotoDownloaded(String imageUrl) {
    if (viewAttemptedPhotos.contains(imageUrl)) {
      viewAttemptedPhotos.remove(imageUrl);
    }
  }
}
