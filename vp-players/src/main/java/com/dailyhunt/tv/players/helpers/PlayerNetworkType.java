package com.dailyhunt.tv.players.helpers;

/**
 * Created by Jayanth on 09/05/18.
 */
public enum PlayerNetworkType {

  NETWORK_TYPE_UNKNOWN(0, "NETWORK_TYPE_UNKNOWN"),  // Unknown or default\
  NETWORK_TYPE_GPRS(1, "NETWORK_TYPE_GPRS"),         // ~ 100 kbps
  NETWORK_TYPE_EDGE(2, "NETWORK_TYPE_EDGE"),         // ~ 50-100 kbps
  NETWORK_TYPE_UMTS(3, "NETWORK_TYPE_UMTS"),         // ~ 400-7000 kbps
  NETWORK_TYPE_CDMA(4, "NETWORK_TYPE_CDMA"),         // ~ 14-64 kbps
  NETWORK_TYPE_EVDO_0(5, "NETWORK_TYPE_EVDO_0"),     // ~ 400-1000 kbps
  NETWORK_TYPE_EVDO_A(6, "NETWORK_TYPE_EVDO_A"),     // ~ 600-1400 kbps
  NETWORK_TYPE_1xRTT(7, "NETWORK_TYPE_1xRTT"),       // ~ 50-100 kbps
  NETWORK_TYPE_HSDPA(8, "NETWORK_TYPE_HSDPA"),       // ~ 2-14 Mbps
  NETWORK_TYPE_HSUPA(9, "NETWORK_TYPE_HSUPA"),       // ~ 1-23 Mbps
  NETWORK_TYPE_HSPA(10, "NETWORK_TYPE_HSPA"),        // ~ 700-1700 kbps
  NETWORK_TYPE_IDEN(11, "NETWORK_TYPE_IDEN"),        // ~25 kbps
  NETWORK_TYPE_EVDO_B(12, "NETWORK_TYPE_EVDO_B"),    // ~ 5 Mbps
  NETWORK_TYPE_LTE(13, "NETWORK_TYPE_LTE"),          // ~ 10+ Mbps
  NETWORK_TYPE_EHRPD(14, "NETWORK_TYPE_EHRPD"),      // ~ 1-2 Mbps
  NETWORK_TYPE_HSPAP(15, "NETWORK_TYPE_HSPAP"),      // ~ 10-20 Mbps
  NETWORK_TYPE_WIFI(99, "NETWORK_TYPE_WIFI");        // ~ wifi

  private int index;
  private String name;

  PlayerNetworkType(int index, String name) {
    this.index = index;
    this.name = name;
  }

  public int getIndex() {
    return index;
  }

  public String getName() {
    return name;
  }

  public static PlayerNetworkType fromName(String name) {
    for (PlayerNetworkType newtworkType : PlayerNetworkType.values()) {
      if (newtworkType.name().equalsIgnoreCase(name)) {
        return newtworkType;
      }
    }
    return NETWORK_TYPE_UNKNOWN;
  }

  public static PlayerNetworkType fromIndex(int index) {
    for (PlayerNetworkType newtworkType : PlayerNetworkType.values()) {
      if (newtworkType.index == index) {
        return newtworkType;
      }
    }
    return NETWORK_TYPE_UNKNOWN;
  }
}
