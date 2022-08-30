package com.dailyhunt.tv.players.helpers;

import com.dailyhunt.tv.ima.IMALogger;
import com.newshunt.common.helper.preference.PreferenceManager;
import com.newshunt.dhutil.helper.preference.AdsPreference;

/**
 * Ads Distance helper for managing the Ads distance based on the distance property from Open-X
 *
 * @author ranjith
 */
public class PlayerAdsDistanceHelper {

  private final String TAG = PlayerAdsDistanceHelper.class.getSimpleName();
  private static PlayerAdsDistanceHelper adsDistanceHelper;

  private final int minAdDistance;
  private int videoPlayCount;
  private int videoInitialOffset;

  private PlayerAdsDistanceHelper() {
    IMALogger.d(TAG, "Private Constructor");
    minAdDistance = PreferenceManager.getPreference(AdsPreference.VIDEO_AD_DISTANCE, 4);
    videoInitialOffset = PreferenceManager.getPreference(AdsPreference.VIDEO_INITIAL_AD_OFFSET, -1);
    videoPlayCount = 0;
  }

  public static PlayerAdsDistanceHelper getInstance() {
    if (adsDistanceHelper == null) {
      synchronized (PlayerAdsDistanceHelper.class) {
        if (adsDistanceHelper == null) {
          adsDistanceHelper = new PlayerAdsDistanceHelper();
        }
      }
    }
    return adsDistanceHelper;
  }

  public void adStarted() {
    IMALogger.d(TAG, "adStarted reset variables: ");
    resetVariables();
  }

  public void updateVideosPlayedCount() {
    videoPlayCount = videoPlayCount + 1;
    IMALogger.d(TAG, "videoPlayCount : " + videoPlayCount);
  }

  private void resetVariables() {
    IMALogger.d(TAG, "reset variables");
    videoPlayCount = 0;
    videoInitialOffset = -1;
  }

  /**
   * Method to decide , when an Ad is going to be played
   * a) If videoPlayCount >= minAdDistance
   * <p>
   * d) if not (a) ,(b) ,(c)  [FALSE]
   *
   * @return -- whether we can show ad or not
   */
  public boolean canShowAd() {
    IMALogger.d(TAG, " videoPlayedCount " + videoPlayCount +
        " minAdDistance to play ad : " + minAdDistance);
    if (videoInitialOffset > 0 && videoInitialOffset >= videoPlayCount) {
      return true;
    } else if (videoPlayCount >= minAdDistance) {
      return true;
    }
    return false;
  }
}
