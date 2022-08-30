/*
 * Copyright (c) 2017 Newshunt. All rights reserved.
 */
package dailyhunt.com.livetv.helpers;

/**
 * Constants used across Live TV section
 *
 * @author vinod.bc
 */
public interface LiveTVConstants {

  String LIVETV_VIDEO_COUNT_PREFIX = "LIVETV_VIDEO_COUNT_PREFIX";
  String GROUP = "group";

  String LIVETV_PROGRAM_TYPE = "programType";
  String LIVE_TV_LASTLOGGEDTIME = "LIVE_TV_LASTLOGGEDTIME";

  long DEFAULT_LIST_REFRESH_TIME = 60000L * 5; // 5mins
  long MINIMUM_LIST_REFRESH_TIME = 60000L * 2; // 2mins
  String PREF_LAST_PLAYED_ASSET = "PREF_LAST_PLAYED_ASSET";
  String LiveTV_HIGHLIGHTER_IDS_MAP = "livetv_highlighter_ids_map";

  interface PROGRAM_TYPE {
    String CURRENT = "CURRENT";
    String LATER = "LATER";
    String NEXT = "NEXT";
  }
}
