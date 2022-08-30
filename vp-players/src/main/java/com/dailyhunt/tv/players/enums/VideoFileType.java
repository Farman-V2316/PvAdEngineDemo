package com.dailyhunt.tv.players.enums;

/**
 * Video type enum
 *
 * @author ankit
 */
public enum VideoFileType {

  M3U8(1, "M3U8"),
  MP4(2, "MP4"),
  YOUTUBE(3, "YOUTUBE"),
  FACEBOOK(4, "FACEBOOK"),
  DAILYMOTION(5, "DAILYMOTION"),
  DH_EMBED_WEBPLAYER(6, "DH_EMBED_WEBPLAYER"),
  DH_WEBPLAYER(7, "DH_WEBPLAYER");

  private int index;
  private String name;

  VideoFileType(int index, String name) {
    this.index = index;
    this.name = name;
  }

  public static VideoFileType fromName(String name) {
    if (name == null) {
      return MP4;
    }

    for (VideoFileType assetType : VideoFileType.values()) {
      if (assetType.name().equalsIgnoreCase(name)) {
        return assetType;
      }
    }
    return MP4;
  }

  public static VideoFileType fromIndex(int index) {
    for (VideoFileType cardType : VideoFileType.values()) {
      if (cardType.index == index) {
        return cardType;
      }
    }
    return null;
  }

  /**
   * returns cardtype matching all params.
   */
  public static VideoFileType thatMatches(String name) {
    for (VideoFileType cardType : VideoFileType.values()) {
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
