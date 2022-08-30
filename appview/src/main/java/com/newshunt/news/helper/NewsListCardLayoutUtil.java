/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package com.newshunt.news.helper;

import android.view.View;

import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;

import com.newshunt.appview.R;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.news.model.entity.DisplayCardType;
import com.newshunt.dhutil.helper.preference.UserPreferenceUtil;
import com.newshunt.helper.ImageUrlReplacer;

import java.util.ArrayList;
import java.util.List;

/**
 * Common Utility class for news list card layout
 *
 * @author santhosh.kc
 */
public class NewsListCardLayoutUtil {

  // This constant is used only for ImageUrlReplacement for purpose of bucketting and it is not
  // actual device width, please dont expose this variable to other class.
  private static final int DEVICE_WIDTH_IN_DP = 360;
  private static final int DEVICE_WIDTH_WITHOUT_PADDING_IN_DP =
      DEVICE_WIDTH_IN_DP - (2 * CommonUtils.getDimensionInDp(R.dimen.story_card_padding_left));

  /**
   * Helper function to get qualified url for only News Card
   *
   * @param url    - url
   * @param isLite - true if lite mode else false
   * @return - replaced contentImage Url
   */
  public static String getNewsContentImageUrl(String url, boolean isLite) {
    if (CommonUtils.isEmpty(url)) {
      return null;
    }

    if (isLite) {
      return ImageUrlReplacer.getQualifiedUrl(url, ImageUrlReplacer.getEmbeddedImageSlow());
    }

    return ImageUrlReplacer.getQualifiedUrl(url, getNewsDetailMastHeadImageDimension());
  }

  /**
   * Function to return Dimension pair for individual thumb of {@link DisplayCardType}.TILE_3 card
   *
   * @return - returns pair of <width,height>
   */
  public static Pair<Integer, Integer> getGalleryTile3ThumbnailImageDimensions() {
    int gapBetweenPhotos = CommonUtils.getDimensionInDp(R.dimen.gallery_image_content_margin);
    int availableWidth = DEVICE_WIDTH_WITHOUT_PADDING_IN_DP - (2 * gapBetweenPhotos);
    int eachPhotoWidth = Math.round(availableWidth / 3.0f);
    int eachPhotoHeight = CommonUtils.getDimensionInDp(R.dimen.tile_3_image_size);
    return Pair.create(eachPhotoWidth, eachPhotoHeight);
  }

  /*
   * ---------------------------------News Detail Mast Head Image dimension ------------------------
   */
  public static Pair<Integer, Integer> getNewsDetailMastHeadImageDimension() {
    final int imageWidth = DEVICE_WIDTH_IN_DP;
    float aspectRatio = ImageUrlReplacer.getContentImageAspectRatio();
    int imageHeight;
    if (Float.compare(aspectRatio, 1.0f) == 0) {
      imageHeight = CommonUtils.getDimensionInDp(R.dimen.news_detail_image_height);
    } else {
      imageHeight = Math.round(imageWidth / ImageUrlReplacer.getContentImageAspectRatio());
    }

    return Pair.create(imageWidth, imageHeight);
  }

  /*
   * ------------------------ New Details buzz association preview item dimensions -------------------
   */
  public static List<Pair<Integer, Integer>> getNewsPaperIconImageDimension() {
    List<Pair<Integer, Integer>> pairs = new ArrayList<>();
    pairs.add(Pair.create(CommonUtils.getDimensionInDp(R.dimen.source_icon_width_height),
        CommonUtils.getDimensionInDp(R.dimen.source_icon_width_height)));
    pairs.add(Pair.create(CommonUtils.getDimensionInDp(R.dimen.autoplay_info_marginRight),
        CommonUtils.getDimensionInDp(R.dimen.newspaper_appbar_height)));
    return pairs;
  }

  public static void manageLayoutDirection(View v){
    if (UserPreferenceUtil.isUserNaviLangRtl()) {
      ViewCompat.setLayoutDirection(v, ViewCompat.LAYOUT_DIRECTION_RTL);
    }
    else{
      ViewCompat.setLayoutDirection(v, ViewCompat.LAYOUT_DIRECTION_LTR);
    }
  }
}
