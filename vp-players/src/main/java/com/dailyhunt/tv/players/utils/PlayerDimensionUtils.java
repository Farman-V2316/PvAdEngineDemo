package com.dailyhunt.tv.players.utils;

import com.dailyhunt.tv.players.helpers.PlayerNetworkType;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerDimensions;
import com.newshunt.dataentity.dhutil.model.entity.players.PlayerItemQuality;
import com.newshunt.dataentity.news.model.entity.server.asset.ExoPlayerAsset;
import com.newshunt.dataentity.news.model.entity.server.asset.PlayerType;
import com.newshunt.dhutil.helper.PlayerDataProvider;

import java.util.ArrayList;
import java.util.List;

public class PlayerDimensionUtils {

  private static List<PlayerItemQuality> videoItemQualities;

  public static String getVideoQuality(PlayerNetworkType networkType, String playerType) {
    PlayerDimensions dimensions = PlayerDataProvider.getInstance().getPlayerDimensions();

    if (dimensions != null && PlayerType.M3U8.name().equalsIgnoreCase(playerType)) {
      //For Auto m3u8, its should return adaptivePlaceHolder
      if (!CommonUtils.isEmpty(dimensions.getAdaptivePlaceHolder())) {
        return dimensions.getAdaptivePlaceHolder();
      }
    }

    PlayerItemQuality networkQuality = getQualityBasedOnNetwork(getVideoItemQuality(), networkType);
    return getMinQualityAvailableForItem(networkQuality).getRequestParam();
  }

  private static PlayerItemQuality getMinQualityAvailableForItem(PlayerItemQuality itemQuality) {
    PlayerUtils.setItemQuality(itemQuality);
    return itemQuality;
  }

  private static PlayerItemQuality getTheMinQuality(int qualityIndex,
                                                    PlayerNetworkType playerNetworkType) {
    PlayerDimensions dimensions = PlayerDataProvider.getInstance().getPlayerDimensions();
    if (dimensions != null) {
      for (PlayerItemQuality itemQuality : dimensions.getVideoQualities()) {
        if (itemQuality.getQualityIndex() == qualityIndex) {
          itemQuality.setNetworkType(playerNetworkType.getIndex());
          PlayerUtils.setItemQuality(itemQuality);
          return itemQuality;
        }
      }
    }

    return getDefaultQuality();
  }

  public static List<PlayerItemQuality> getVideoItemQuality() {
    PlayerDimensions dimensions = PlayerDataProvider.getInstance().getPlayerDimensions();
    if (videoItemQualities != null) {
      return videoItemQualities;
    }
    videoItemQualities = new ArrayList<>();
    if (dimensions != null) {
      for (PlayerItemQuality itemQuality : dimensions.getVideoQualities()) {
        videoItemQualities.add(itemQuality);
      }
    }

    return videoItemQualities;
  }

  private static String getGifQualityMappingToVideoQuality(PlayerItemQuality userSetting,
                                                           ExoPlayerAsset item) {
    //For Gif, low, medium & high options are shown to user
    if (userSetting.getRequestParam().equalsIgnoreCase("l")) {
      //Push for Very low Video quality available
      PlayerItemQuality networkQuality =
          getQualityBasedOnNetwork(getVideoItemQuality(), PlayerNetworkType.NETWORK_TYPE_EDGE);
      return getMinQualityAvailableForItem(networkQuality).getRequestParam();
    } else if (userSetting.getRequestParam().equalsIgnoreCase("m")) {
      //Push for Very medium Video quality available
      PlayerItemQuality networkQuality =
          getQualityBasedOnNetwork(getVideoItemQuality(), PlayerNetworkType.NETWORK_TYPE_EVDO_A);
      return getMinQualityAvailableForItem(networkQuality).getRequestParam();
    } else if (userSetting.getRequestParam().equalsIgnoreCase("h")) {
      //Push for Very High Video available
      PlayerItemQuality networkQuality =
          getQualityBasedOnNetwork(getVideoItemQuality(), PlayerNetworkType.NETWORK_TYPE_WIFI);
      return getMinQualityAvailableForItem(networkQuality).getRequestParam();
    }

    PlayerUtils.setItemQuality(userSetting);
    return userSetting.getRequestParam();
  }


  public static String getImageQuality(PlayerNetworkType networkType) {
    PlayerDimensions dimensions = PlayerDataProvider.getInstance().getPlayerDimensions();
    return getQualityBasedOnNetwork(dimensions.getImageQualities(), networkType).getRequestParam();
  }

  /**
   * Used only for buzz cards in news section
   *
   * @param networkType
   * @return
   */
  public static String getThumbnailImageQuality(PlayerNetworkType networkType) {
    String settings;
    PlayerDimensions dimensions = PlayerDataProvider.getInstance().getPlayerDimensions();
    return getQualityBasedOnNetwork(dimensions.getThumbnailQualities(), networkType).getRequestParam();
  }


  public static PlayerItemQuality getQualityBasedOnNetwork(List<PlayerItemQuality> itemQualities,
                                                           PlayerNetworkType networkType) {
    PlayerItemQuality returnQuaity = getDefaultQuality();
    if (itemQualities == null || itemQualities.size() == 0) {
      PlayerUtils.setItemQuality(returnQuaity);
      return returnQuaity;
    }

    String value = PlayerUtils.DEFAULT_QUALITY_BUCKET_VALUE;
    for (PlayerItemQuality itemQuality : itemQualities) {
      if (itemQuality.getNetworkType() == networkType.getIndex()) {
        returnQuaity = itemQuality;
        break;
      }
    }
    PlayerUtils.setItemQuality(returnQuaity);
    return returnQuaity;
  }

  public static PlayerItemQuality getDefaultQuality() {
    PlayerItemQuality quality = new PlayerItemQuality();
    quality.setDisplayString("default");
    quality.setNetworkType(PlayerNetworkType.NETWORK_TYPE_UNKNOWN.getIndex());
    quality.setRequestParam(PlayerUtils.DEFAULT_QUALITY_BUCKET_VALUE);

    return quality;
  }
}
