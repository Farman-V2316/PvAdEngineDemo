package com.dailyhunt.tv.ima.entity.state;

/**
 * Different States that can be possible for an AD
 * This state is managed or updated with help of the IMA SDK Call back requests / not Actual States
 * <p>
 *
 * @author ranjith
 */

public enum AdState implements ContentState {

  AD_UNKNOWN(0),          // We don't know the current state [i.e it is not either 0 -- 4]
  AD_LOADED(1),           // When an Ad request is loaded
  AD_PLAY_STARTED(2),     // When an Ad Play is started after loading it..
  AD_RESUMED(3),          // When an Ad is resumed
  AD_PAUSED(4),           //When an Ad is paused
  AD_PLAY_ENDED(5),       // When an Ad Play is ended
  AD_ERROR(6),            // When an Ad play back / loading cause an Error..
  ALL_ADS_COMPLETE(7),    // When all Ad's are completed..
  AD_TAPPED(8),           // When the ad is tapped. It may or maynot lead to clickthrough.
  AD_CLICKED(9),          // When the ad is clicked i.e. clickthrough event.
  AD_COMPANION_CLICKED(10); //When the companion ad is clicked i.e. clickthrough event.

  private int index;

  AdState(int index) {
    this.index = index;
  }

  @Override
  public int getStateIndex() {
    return index;
  }

  @Override
  public ContentType getContentType() {
    return ContentType.AD;
  }
}
