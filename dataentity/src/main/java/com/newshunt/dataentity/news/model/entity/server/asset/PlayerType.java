package com.newshunt.dataentity.news.model.entity.server.asset;

/**
 * Created by vinod on 23/04/18.
 */

public enum PlayerType {

  M3U8(3, "M3U8"),
  MP4(4, "MP4"),
  DASH(5, "DASH"),
  YOUTUBE(6, "YOUTUBE"),
  FACEBOOK(7, "FACEBOOK"),
  DAILYMOTION(8, "DAILYMOTION"),
  DH_EMBED_WEBPLAYER(9, "DH_EMBED_WEBPLAYER"),
  DH_WEBPLAYER(10, "DH_WEBPLAYER");

  private int index;
  private String name;

  PlayerType(int index, String name) {
    this.index = index;
    this.name = name;
  }

  public static PlayerType fromName(String name) {
    if (name == null) {
      return M3U8;
    }

    for (PlayerType assetType : PlayerType.values()) {
      if (assetType.name().equalsIgnoreCase(name)) {
        return assetType;
      }
    }
    return M3U8;
  }

  public static PlayerType fromIndex(int index) {
    for (PlayerType cardType : PlayerType.values()) {
      if (cardType.index == index) {
        return cardType;
      }
    }
    return null;
  }

  /**
   * returns cardtype matching all params.
   */
  public static PlayerType thatMatches(String name) {
    for (PlayerType cardType : PlayerType.values()) {
      if (cardType.name.equalsIgnoreCase(name)) {
        return cardType;
      }
    }
    return null;
  }

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }

}