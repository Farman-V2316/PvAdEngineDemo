package com.dailyhunt.tv.players.model.entities.server;

/**
 * Gif File type enum
 *
 * @author ankit
 */
public enum GifFileType {

  GIF, MP4, M3U8;

  public static GifFileType fromName(String name) {
    if (name == null) {
      return MP4;
    }

    for (GifFileType assetType : GifFileType.values()) {
      if (assetType.name().equalsIgnoreCase(name)) {
        return assetType;
      }
    }
    return MP4;
  }
}