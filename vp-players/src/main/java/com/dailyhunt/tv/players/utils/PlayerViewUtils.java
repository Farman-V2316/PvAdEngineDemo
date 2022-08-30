package com.dailyhunt.tv.players.utils;

import android.widget.Toast;

import com.dailyhunt.tv.players.R;
import com.newshunt.dataentity.common.helper.common.CommonUtils;
import com.newshunt.common.helper.common.Logger;
import com.newshunt.common.helper.font.FontHelper;


/**
 * CommonUtils for Views in TV Module
 *
 * @author ranjith
 */

public class PlayerViewUtils {

  /**
   * Method for showing No N/w Toast
   */
  public static void showNoNetworkToast() {
    try {
      FontHelper.showCustomFontToast(CommonUtils.getApplication(),
          CommonUtils.getString(com.newshunt.common.util.R.string.no_connection_error),
          Toast.LENGTH_SHORT);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }


  /**
   * Method for showing media play Error Toast
   */
  public static void showMediaPlayErrorToast() {
    try {
      FontHelper.showCustomFontToast(CommonUtils.getApplication(),
          CommonUtils.getString(R.string.tv_media_player_error),
          Toast.LENGTH_SHORT);
    } catch (Exception e) {
      Logger.caughtException(e);
    }
  }
}
